package com.chibao.dbbackup_cli.adapter.out.metadata;

import com.chibao.dbbackup_cli.domain.port.out.MetadataPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Metadata Builder Utility
 * Helps construct BackupMetadataDto with validation
 */
@Component
@Slf4j
class MetadataBuilderUtility {

    /**
     * Build metadata from backup information
     */
    public MetadataPort.BackupMetadataDto buildMetadata(
            String filename,
            String dbType,
            String dbHost,
            int dbPort,
            String dbName,
            java.time.Instant startTime,
            java.time.Instant endTime,
            long sizeBytes,
            String sha256,
            String compressAlgo,
            String encryptAlgo,
            String status
    ) {

        long durationMs = java.time.Duration.between(startTime, endTime).toMillis();

        return MetadataPort.BackupMetadataDto.builder()
                .filename(filename)
                .version(getApplicationVersion())
                .dbType(dbType)
                .dbHost(dbHost)
                .dbPort(dbPort)
                .dbName(dbName)
                .startTime(startTime.toString())
                .endTime(endTime.toString())
                .durationMs(durationMs)
                .sizeBytes(sizeBytes)
                .sha256(sha256)
                .compressAlgo(compressAlgo)
                .encryptAlgo(encryptAlgo != null ? encryptAlgo : "NONE")
                .chunks(null) // Set if multipart upload was used
                .status(status)
                .build();
    }

    /**
     * Get application version from properties
     */
    private String getApplicationVersion() {
        // In real implementation, read from application.properties or manifest
        return "1.0.0";
    }

    /**
     * Validate metadata completeness
     */
    public boolean isComplete(MetadataPort.BackupMetadataDto metadata) {
        return metadata.getFilename() != null &&
                metadata.getDbType() != null &&
                metadata.getDbName() != null &&
                metadata.getStartTime() != null &&
                metadata.getEndTime() != null &&
                metadata.getSizeBytes() > 0 &&
                metadata.getSha256() != null &&
                metadata.getStatus() != null;
    }
}