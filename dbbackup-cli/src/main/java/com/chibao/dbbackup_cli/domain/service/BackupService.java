package com.chibao.dbbackup_cli.domain.service;

import com.chibao.dbbackup_cli.config.DatabaseDumpFactory;
import com.chibao.dbbackup_cli.domain.exception.BackupFailedException;
import com.chibao.dbbackup_cli.domain.model.Backup;
import com.chibao.dbbackup_cli.domain.model.BackupStatus;
import com.chibao.dbbackup_cli.domain.model.CompressionType;
import com.chibao.dbbackup_cli.domain.port.in.BackupUseCase;
import com.chibao.dbbackup_cli.domain.port.out.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * BackupService - Core Business Logic
 * Implements INBOUND PORT (BackupUseCase)
 * Depends on OUTBOUND PORTS (interfaces, not implementations)
 * This is the heart of the application - pure business logic
 * with NO knowledge of:
 * - How database dumps are performed (PostgreSQL? MySQL?)
 * - Where files are stored (S3? Local?)
 * - How encryption works (AES? RSA?)
 * All external concerns are abstracted behind ports (interfaces).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BackupService implements BackupUseCase {
    // ===== OUTBOUND PORTS (Dependencies - Interfaces) =====

    private final DatabaseDumpFactory databaseDumpFactory;
    private final StoragePort storagePort;
    private final ChecksumPort checksumPort;
    private final EncryptionPort encryptionPort;
    private final MetadataPort metadataPort;
    private final MetricsPort metricsPort;
    private final BackupRecordPort backupRecordPort;

    /**
     * Execute backup - Main business logic
     * Flow:
     * 1. Initialize backup entity
     * 2. Dump database via port
     * 3. Compress file
     * 4. Encrypt if needed
     * 5. Calculate checksum
     * 6. Upload to storage
     * 7. Save metadata
     * 8. Record metrics
     * 9. Cleanup temp files
     */
    @Override
    public BackupResult execute(BackupCommand command) {

        Instant startTime = Instant.now();
        String backupId = generateBackupId();

        log.info("Starting backup: backupId={}, database={}, type={}",
                backupId, command.getDatabase(), command.getDatabaseType());

        // Create domain entity and save initial state
        Backup backup = createBackupEntity(backupId, command, startTime);
        backupRecordPort.save(backup);

        Path tempDumpFile = null;
        Path compressedFile = null;
        Path encryptedFile = null;

        try {
            // ===== 1. DUMP DATABASE (via outbound port) =====
            log.debug("Performing database dump: backupId={}", backupId);

            // Get the correct adapter from the factory based on user input
            DatabaseDumpPort databaseDumpPort = databaseDumpFactory.getAdapter(command.getDatabaseType());

            DatabaseDumpPort.DumpConfig dumpConfig = buildDumpConfig(command);
            DatabaseDumpPort.DumpOutput dumpOutput = databaseDumpPort.performDump(dumpConfig);

            tempDumpFile = dumpOutput.getDumpFilePath();
            log.info("Database dump completed: backupId={}, size={} bytes",
                    backupId, dumpOutput.getSizeBytes());

            // ===== 2. COMPRESS FILE (business logic) =====
            compressedFile = compressIfNeeded(tempDumpFile, command.getCompression());

            // ===== 3. ENCRYPT FILE (via outbound port) =====
            Path finalFile = compressedFile;
            if (command.isEncrypt()) {
                log.debug("Encrypting backup: backupId={}", backupId);
                EncryptionPort.EncryptionConfig encryptConfig = buildEncryptionConfig();
                encryptedFile = encryptionPort.encrypt(compressedFile, encryptConfig);
                finalFile = encryptedFile;
            }

            // ===== 4. CALCULATE CHECKSUM (via outbound port) =====
            log.debug("Calculating checksum: backupId={}", backupId);
            String checksum = checksumPort.calculate(finalFile);

            // ===== 5. UPLOAD TO STORAGE (via outbound port) =====
            log.debug("Uploading to storage: backupId={}, provider={}",
                    backupId, command.getStorageProvider());

            String storageLocation = uploadToStorage(finalFile, backupId, command);

            // ===== 6. SAVE METADATA (via outbound port) =====
            Path metadataPath = saveMetadata(
                    backup,
                    checksum,
                    Files.size(finalFile),
                    storageLocation,
                    startTime
            );

            // Upload metadata alongside backup
            uploadMetadata(metadataPath, backupId);

            // ===== 7. UPDATE DOMAIN ENTITY AND SAVE =====
            backup = backup.markAsCompleted(checksum, Files.size(finalFile), storageLocation);
            backupRecordPort.save(backup);

            // ===== 8. RECORD METRICS (via outbound port) =====
            long durationMs = backup.getDurationSeconds() * 1000;
            metricsPort.recordBackupDuration(command.getDatabaseType(), durationMs, true);
            metricsPort.recordBackupSize(command.getDatabaseType(), Files.size(finalFile));
            metricsPort.incrementBackupCount(command.getDatabaseType(), "success");

            log.info("Backup completed successfully: backupId={}, duration={}ms, size={} bytes",
                    backupId, durationMs, Files.size(finalFile));

            return BackupResult.success(
                    backupId,
                    "Backup completed successfully",
                    BackupMetadata.builder()
                            .storageLocation(storageLocation)
                            .sizeBytes(Files.size(finalFile))
                            .checksum(checksum)
                            .durationMs(durationMs)
                            .build()
            );

        } catch (Exception e) {
            log.error("Backup failed: backupId={}, error={}", backupId, e.getMessage(), e);

            // Save failed state to database
            Backup failedBackup = backup.markAsFailed(e.getMessage());
            backupRecordPort.save(failedBackup);

            // Record failure metrics
            long durationMs = (Instant.now().toEpochMilli() - startTime.toEpochMilli());
            metricsPort.recordBackupDuration(command.getDatabaseType(), durationMs, false);
            metricsPort.incrementBackupCount(command.getDatabaseType(), "failure");

            return BackupResult.failure(backupId, "Backup failed: " + e.getMessage());

        } finally {
            // ===== 9. CLEANUP TEMPORARY FILES =====
            cleanupTempFiles(tempDumpFile, compressedFile, encryptedFile);
        }
    }

    // ===== PRIVATE HELPER METHODS (Business Logic) =====

    private String generateBackupId() {
        return UUID.randomUUID().toString();
    }

    private Backup createBackupEntity(String backupId, BackupCommand command, Instant startTime) {
        return Backup.builder()
                .id(backupId)
                .databaseType(command.getDatabaseType())
                .databaseName(command.getDatabase())
                .host(command.getHost())
                .port(command.getPort())
                .status(BackupStatus.IN_PROGRESS)
                .createdAt(startTime)
                .compression(command.getCompression())
                .encrypted(command.isEncrypt())
                .build();
    }

    private DatabaseDumpPort.DumpConfig buildDumpConfig(BackupCommand command) {
        return DatabaseDumpPort.DumpConfig.builder()
                .host(command.getHost())
                .port(command.getPort())
                .database(command.getDatabase())
                .username(command.getUsername())
                .password(command.getPassword())
                .workingDirectory(createTempWorkDir())
                .tables(command.getTables())
                .additionalOptions(command.getAdditionalOptions())
                .build();
    }

    private Path createTempWorkDir() {
        try {
            return Files.createTempDirectory("dbbackup_");
        } catch (Exception e) {
            throw new BackupFailedException("Failed to create temp directory", e);
        }
    }

    /**
     * Compress file based on compression type
     * Business logic for compression
     */
    private Path compressIfNeeded(Path source, CompressionType compressionType) {
        if (compressionType == CompressionType.NONE) {
            return source;
        }

        try {
            // For now, assume gzip compression
            // In real implementation, delegate to compression utility
            Path compressedPath = Files.createTempFile("backup_", ".tar.gz");

            switch (compressionType){
                case GZIP -> {
                    compressedPath = Files.createTempFile("backup_", ".gz");
                    compressGzip(source, compressedPath);
                    break;
                }
                case ZIP -> {
                    compressedPath = Files.createTempFile("backup_", ".zip");
                    compressZip(source, compressedPath);
                    break;
                }
                default ->
                    throw new IllegalArgumentException("Unsupported compression type: " + compressionType);
            }
            log.debug("Compressed file: {} -> {}", source, compressedPath);
            return compressedPath;
        } catch (Exception e) {
            throw new BackupFailedException("Compression failed", e);
        }
    }
    private void compressGzip(Path source, Path target) throws IOException {
        try (OutputStream fileOut = Files.newOutputStream(target)){
            GZIPOutputStream gzipOut = new GZIPOutputStream(fileOut);
            Files.copy(source, gzipOut);
        }
    }
    private void compressZip(Path source, Path target) throws IOException {
        try (OutputStream fileOut = Files.newOutputStream(target);
             ZipOutputStream zipOut = new ZipOutputStream(fileOut)) {

            // Zip cần tạo một entry (tên file bên trong file zip)
            ZipEntry zipEntry = new ZipEntry(source.getFileName().toString());
            zipOut.putNextEntry(zipEntry);

            Files.copy(source, zipOut);

            zipOut.closeEntry();
        }
    }

    private EncryptionPort.EncryptionConfig buildEncryptionConfig() {
        // In real implementation, get key from KMS/Vault
        return EncryptionPort.EncryptionConfig.builder()
                .algorithm("AES-256-GCM")
           // TODO:     .keyId("backup-encryption-key")
                .build();
    }

    /**
     * Upload backup file to storage
     */
    private String uploadToStorage(Path file, String backupId, BackupCommand command) {
        try (FileInputStream fis = new FileInputStream(file.toFile())) {

            String filename = generateFilename(backupId, command);

            StoragePort.UploadRequest uploadRequest = StoragePort.UploadRequest.builder()
                    .data(fis)
                    .filename(filename)
                    .sizeBytes(Files.size(file))
                    .metadata(Map.of(
                            "backupId", backupId,
                            "databaseType", command.getDatabaseType(),
                            "databaseName", command.getDatabase()
                    ))
                    .enableMultipart(Files.size(file) > 100 * 1024 * 1024) // > 100MB
                    .build();

            return storagePort.upload(uploadRequest);

        } catch (Exception e) {
            throw new BackupFailedException("Upload to storage failed", e);
        }
    }

    private String generateFilename(String backupId, BackupCommand command) {
        // Format: dbbackup_postgres_mydb_20251127T120000Z_uuid.tar.gz
        String timestamp = Instant.now().toString().replace(":", "").replace("-", "");
        return String.format(
                "dbbackup_%s_%s_%s_%s.tar.gz",
                command.getDatabaseType(),
                command.getDatabase(),
                timestamp,
                backupId.substring(0, 8)
        );
    }

    private Path saveMetadata(
            Backup backup,
            String checksum,
            long sizeBytes,
            String storageLocation,
            Instant startTime
    ) {
        try {
            Path metadataPath = Files.createTempFile("metadata_", ".json");

            MetadataPort.BackupMetadataDto metadata = MetadataPort.BackupMetadataDto.builder()
                    .filename(storageLocation)
                    .version("1.0.0")
                    .dbType(backup.getDatabaseType())
                    .dbHost(backup.getHost())
                    .dbPort(backup.getPort())
                    .dbName(backup.getDatabaseName())
                    .startTime(startTime.toString())
                    .endTime(Instant.now().toString())
                    .durationMs(backup.getDurationSeconds() * 1000)
                    .sizeBytes(sizeBytes)
                    .sha256(checksum)
                    .compressAlgo(backup.getCompression().name())
                    .encryptAlgo(backup.isEncrypted() ? "AES-256-GCM" : "NONE")
                    .status("COMPLETED")
                    .build();

            metadataPort.save(metadata, metadataPath);

            return metadataPath;

        } catch (Exception e) {
            throw new BackupFailedException("Failed to save metadata", e);
        }
    }

    private void uploadMetadata(Path metadataPath, String backupId) {
        try (FileInputStream fis = new FileInputStream(metadataPath.toFile())) {

            StoragePort.UploadRequest uploadRequest = StoragePort.UploadRequest.builder()
                    .data(fis)
                    .filename(backupId + ".meta.json")
                    .sizeBytes(Files.size(metadataPath))
                    .metadata(Map.of("type", "metadata", "backupId", backupId))
                    .enableMultipart(false)
                    .build();

            storagePort.upload(uploadRequest);

        } catch (Exception e) {
            log.warn("Failed to upload metadata: backupId={}", backupId, e);
            // Non-critical - don't fail backup
        }
    }

    private void cleanupTempFiles(Path... files) {
        for (Path file : files) {
            if (file != null && Files.exists(file)) {
                try {
                    Files.delete(file);
                    log.debug("Cleaned up temp file: {}", file);
                } catch (Exception e) {
                    log.warn("Failed to delete temp file: {}", file, e);
                }
            }
        }
    }
}
