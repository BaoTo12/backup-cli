package com.chibao.dbbackup_cli.domain.port.in.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RestoreResult {
    String backupId;
    boolean success;
    String message;
    long durationMs;

    public static RestoreResult success(String backupId, long durationMs) {
        return RestoreResult.builder()
                .backupId(backupId)
                .success(true)
                .message("Restore completed successfully")
                .durationMs(durationMs)
                .build();
    }

    public static RestoreResult failure(String backupId, String message) {
        return RestoreResult.builder()
                .backupId(backupId)
                .success(false)
                .message(message)
                .durationMs(0)
                .build();
    }
}
