package com.chibao.dbbackup_cli.adapter.in.rest.dto;

import com.chibao.dbbackup_cli.domain.model.Backup;
import com.chibao.dbbackup_cli.domain.model.BackupStatus;
import lombok.Value;

import java.time.Instant;

/**
 * A DTO (Data Transfer Object) representing a simplified view of a backup record
 * for display purposes (e.g., in a CLI table).
 */
@Value
public class BackupInfo {
    String id;
    String databaseName;
    String databaseType;
    BackupStatus status;
    Instant createdAt;
    Long sizeBytes;
    String storageLocation;

    /**
     * Factory method to create a BackupInfo DTO from a Backup domain entity.
     * @param backup The source Backup entity.
     * @return A new BackupInfo DTO.
     */
    public static BackupInfo from(Backup backup) {
        return new BackupInfo(
                backup.getId(),
                backup.getDatabaseName(),
                backup.getDatabaseType(),
                backup.getStatus(),
                backup.getCreatedAt(),
                backup.getSizeBytes(),
                backup.getStorageLocation()
        );
    }
}
