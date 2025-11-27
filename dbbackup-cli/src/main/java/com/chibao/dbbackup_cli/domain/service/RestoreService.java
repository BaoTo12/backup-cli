package com.chibao.dbbackup_cli.domain.service;

import com.chibao.dbbackup_cli.config.DatabaseDumpFactory;
import com.chibao.dbbackup_cli.domain.exception.RestoreFailedException;
import com.chibao.dbbackup_cli.domain.model.Backup;
import com.chibao.dbbackup_cli.domain.port.in.RestoreUseCase;
import com.chibao.dbbackup_cli.domain.port.out.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestoreService implements RestoreUseCase {

    private final BackupRecordPort backupRecordPort;
    private final StoragePort storagePort;
    private final ChecksumPort checksumPort;
    private final DatabaseDumpFactory databaseDumpFactory;
    // private final EncryptionPort encryptionPort; // Assuming it exists and will be used

    @Override
    public RestoreResult execute(RestoreCommand command) {
        log.info("Starting restore for backupId: {}", command.getBackupId());
        Instant startTime = Instant.now();
        Path downloadedFile = null;

        try {
            // 1. Find backup metadata from database
            Backup backup = backupRecordPort.findById(command.getBackupId())
                    .orElseThrow(() -> new RestoreFailedException("Backup with ID '" + command.getBackupId() + "' not found."));

            log.debug("Found backup record: {}", backup);

            // 2. Download backup file from storage
            log.info("Downloading backup file from storage location: {}", backup.getStorageLocation());
            try (InputStream backupStream = storagePort.download(backup.getStorageLocation())) {
                downloadedFile = Files.createTempFile("restore_", backup.getStorageLocation());
                Files.copy(backupStream, downloadedFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            log.info("Download complete. File saved to temporary path: {}", downloadedFile);

            // 3. Verify checksum
            log.info("Verifying checksum...");
            boolean checksumVerified = checksumPort.verify(downloadedFile, backup.getChecksum());
            if (!checksumVerified) {
                throw new RestoreFailedException("Checksum verification failed. The backup file may be corrupt.");
            }
            log.info("Checksum verification successful.");

            // 4. Decrypt file (if encrypted)
            // Path fileToRestore = downloadedFile;
            // if (backup.isEncrypted()) {
            //     log.info("Decrypting backup file...");
            //     fileToRestore = encryptionPort.decrypt(downloadedFile);
            //     log.info("Decryption complete.");
            // }

            // 5. Get correct DB adapter and perform restore
            log.info("Performing restore to target database: {}", command.getTargetDatabase());
            DatabaseDumpPort databaseDumpPort = databaseDumpFactory.getAdapter(backup.getDatabaseType());

            DatabaseDumpPort.RestoreInput restoreInput = DatabaseDumpPort.RestoreInput.builder()
                    .dumpFilePath(downloadedFile) // Use 'fileToRestore' if decryption is implemented
                    .targetHost(command.getTargetHost())
                    .targetPort(command.getTargetPort())
                    .targetDatabase(command.getTargetDatabase())
                    .username(command.getUsername())
                    .password(command.getPassword())
                    .build();

            databaseDumpPort.performRestore(restoreInput);
            log.info("Database restore completed successfully.");

            long durationMs = Duration.between(startTime, Instant.now()).toMillis();
            return RestoreResult.builder()
                    .success(true)
                    .message("Restore completed successfully.")
                    .durationMs(durationMs)
                    .build();

        } catch (Exception e) {
            long durationMs = Duration.between(startTime, Instant.now()).toMillis();
            log.error("Restore failed: {}", e.getMessage(), e);
            return RestoreResult.builder()
                    .success(false)
                    .message("Restore failed: " + e.getMessage())
                    .durationMs(durationMs)
                    .build();
        } finally {
            // 6. Cleanup temporary files
            if (downloadedFile != null) {
                try {
                    Files.delete(downloadedFile);
                    log.debug("Cleaned up temporary file: {}", downloadedFile);
                } catch (Exception e) {
                    log.warn("Failed to delete temporary file: {}", downloadedFile, e);
                }
            }
        }
    }
}