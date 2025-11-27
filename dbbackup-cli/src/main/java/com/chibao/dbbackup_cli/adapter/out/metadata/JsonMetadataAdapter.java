package com.chibao.dbbackup_cli.adapter.out.metadata;

import com.chibao.dbbackup_cli.domain.exception.StorageException;
import com.chibao.dbbackup_cli.domain.port.out.MetadataPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * JSON Metadata Adapter
 * OUTBOUND ADAPTER - implements MetadataPort
 * Saves and loads backup metadata in JSON format.
 * Creates .meta.json files alongside backups.
 * Example metadata file:
 * {
 *   "filename": "backup_postgres_mydb_20251127.tar.gz",
 *   "version": "1.0.0",
 *   "dbType": "postgres",
 *   "dbHost": "localhost",
 *   "dbPort": 5432,
 *   "dbName": "mydb",
 *   "startTime": "2025-11-27T10:00:00Z",
 *   "endTime": "2025-11-27T10:05:30Z",
 *   "durationMs": 330000,
 *   "sizeBytes": 1048576000,
 *   "sha256": "a1b2c3d4...",
 *   "compressAlgo": "GZIP",
 *   "encryptAlgo": "AES-256-GCM",
 *   "chunks": 10,
 *   "status": "COMPLETED"
 * }
 */
@Component
@Slf4j
public class JsonMetadataAdapter implements MetadataPort {

    private final ObjectMapper objectMapper;

    public JsonMetadataAdapter() {
        this.objectMapper = new ObjectMapper();

        // Configure ObjectMapper
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // Pretty print
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void save(BackupMetadataDto metadata, Path outputPath) {
        log.info("Saving metadata to file: {}", outputPath);

        try {
            // Ensure parent directory exists
            if (outputPath.getParent() != null) {
                Files.createDirectories(outputPath.getParent());
            }

            // Write JSON to file
            objectMapper.writeValue(outputPath.toFile(), metadata);

            log.info("Metadata saved successfully: {} ({} bytes)",
                    outputPath, Files.size(outputPath));

        } catch (IOException e) {
            log.error("Failed to save metadata to file: {}", outputPath, e);
            throw new StorageException("Failed to save metadata", e);
        }
    }

    @Override
    public BackupMetadataDto load(Path metadataPath) {
        log.info("Loading metadata from file: {}", metadataPath);

        try {
            // Verify file exists
            if (!Files.exists(metadataPath)) {
                throw new StorageException("Metadata file not found: " + metadataPath);
            }

            // Read JSON from file
            BackupMetadataDto metadata = objectMapper.readValue(
                    metadataPath.toFile(),
                    BackupMetadataDto.class
            );

            log.info("Metadata loaded successfully: {}", metadataPath);

            return metadata;

        } catch (IOException e) {
            log.error("Failed to load metadata from file: {}", metadataPath, e);
            throw new StorageException("Failed to load metadata", e);
        }
    }

    @Override
    public boolean validate(Path metadataPath) {
        log.debug("Validating metadata file: {}", metadataPath);

        try {
            // Check file exists
            if (!Files.exists(metadataPath)) {
                log.warn("Metadata file does not exist: {}", metadataPath);
                return false;
            }

            // Check file is readable
            if (!Files.isReadable(metadataPath)) {
                log.warn("Metadata file is not readable: {}", metadataPath);
                return false;
            }

            // Try to parse JSON
            BackupMetadataDto metadata = load(metadataPath);

            // Validate required fields
            if (metadata.getFilename() == null || metadata.getFilename().isEmpty()) {
                log.warn("Metadata validation failed: filename is missing");
                return false;
            }

            if (metadata.getDbType() == null || metadata.getDbType().isEmpty()) {
                log.warn("Metadata validation failed: dbType is missing");
                return false;
            }

            if (metadata.getStatus() == null || metadata.getStatus().isEmpty()) {
                log.warn("Metadata validation failed: status is missing");
                return false;
            }

            log.debug("Metadata validation passed: {}", metadataPath);
            return true;

        } catch (Exception e) {
            log.warn("Metadata validation failed: {}", metadataPath, e);
            return false;
        }
    }

    /**
     * Generate metadata filename from backup filename
     * Example: backup.tar.gz -> backup.tar.gz.meta.json
     */
    public Path generateMetadataPath(Path backupFilePath) {
        String backupFilename = backupFilePath.getFileName().toString();
        String metadataFilename = backupFilename + ".meta.json";

        return backupFilePath.getParent() != null
                ? backupFilePath.getParent().resolve(metadataFilename)
                : Path.of(metadataFilename);
    }

    /**
     * Save metadata with auto-generated filename
     */
    public void saveWithAutoFilename(BackupMetadataDto metadata, Path backupFilePath) {
        Path metadataPath = generateMetadataPath(backupFilePath);
        save(metadata, metadataPath);
    }
}
