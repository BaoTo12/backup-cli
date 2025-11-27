package com.chibao.dbbackup_cli.domain.port.in;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * INBOUND PORT: Restore Use Case
 */
public interface RestoreUseCase {

    RestoreResult execute(RestoreCommand command);

    @Value
    @Builder
    class RestoreCommand {
        String backupId;
        String targetHost;
        int targetPort;
        String targetDatabase;
        String username;
        String password;

        @Builder.Default
        boolean skipIfExists = false;

        List<String> tables;  // For selective restore
    }

    @Value
    @Builder
    class RestoreResult {
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
}
