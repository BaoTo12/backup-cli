package com.chibao.dbbackup_cli.domain.port.in;

import com.chibao.dbbackup_cli.domain.model.CompressionType;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Map;

/**
 * INBOUND PORT: Backup Use Case
 *
 * Interface mà application core CUNG CẤP cho bên ngoài.
 * Được IMPLEMENT bởi BackupService (domain/service).
 * Được SỬ DỤNG bởi adapters (CLI, REST, Scheduler).
 */
public interface BackupUseCase {

    /**
     * Execute full backup of a database
     * @param command contains all parameters needed for backup
     * @return result with backup ID and metadata
     */
    BackupResult execute(BackupCommand command);

    // ===== COMMAND (Input) =====

    @Value
    @Builder
    class BackupCommand {
        String databaseType;      // postgres, mysql, mongodb
        String host;
        int port;
        String database;
        String username;
        String password;

        @Builder.Default
        CompressionType compression = CompressionType.GZIP;

        @Builder.Default
        boolean encrypt = false;

        String storageProvider;   // s3, local, minio

        List<String> tables;      // For selective backup (optional)

        @Builder.Default
        Map<String, String> additionalOptions = Map.of();
    }

    // ===== RESULT (Output) =====

    @Value
    @Builder
    class BackupResult {
        String backupId;
        boolean success;
        String message;
        BackupMetadata metadata;

        public static BackupResult success(String backupId, String message, BackupMetadata metadata) {
            return BackupResult.builder()
                    .backupId(backupId)
                    .success(true)
                    .message(message)
                    .metadata(metadata)
                    .build();
        }

        public static BackupResult failure(String backupId, String message) {
            return BackupResult.builder()
                    .backupId(backupId)
                    .success(false)
                    .message(message)
                    .build();
        }
    }

    @Value
    @Builder
    class BackupMetadata {
        String storageLocation;
        long sizeBytes;
        String checksum;
        long durationMs;
    }
}
