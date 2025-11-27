package com.chibao.dbbackup_cli.domain.port.in.dto;

import com.chibao.dbbackup_cli.domain.model.BackupMetadata;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BackupResult {
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