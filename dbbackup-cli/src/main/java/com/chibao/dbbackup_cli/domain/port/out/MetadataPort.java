package com.chibao.dbbackup_cli.domain.port.out;

import lombok.Builder;
import lombok.Value;

import java.nio.file.Path;

/**
 * OUTBOUND PORT: Metadata Port
 * Core cần persist backup metadata
 */
public interface MetadataPort {
    /**
     * Save metadata to file
     * @param metadata backup metadata
     * @param outputPath path to save metadata file
     */
    void save(BackupMetadataDto metadata, Path outputPath);

    /**
     * Load metadata from file
     * @param metadataPath path to metadata file
     * @return backup metadata
     */
    BackupMetadataDto load(Path metadataPath);

    /**
     * Validate metadata file
     * @param metadataPath path to validate
     * @return true if valid
     */
    boolean validate(Path metadataPath);

    @Value
    @Builder
    class BackupMetadataDto {
        String filename;
        String version;
        String dbType;
        String dbHost;
        int dbPort;
        String dbName;
        String startTime;
        String endTime;
        long durationMs;
        long sizeBytes;
        String sha256;
        String compressAlgo;
        String encryptAlgo;
        Integer chunks;
        String status;
    }
}
