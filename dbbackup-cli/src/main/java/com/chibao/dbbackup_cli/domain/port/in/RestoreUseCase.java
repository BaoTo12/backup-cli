package com.chibao.dbbackup_cli.domain.port.in;

import lombok.Builder;
import lombok.Value;

/**
 * Inbound Port for restoring a database from a backup.
 */
public interface RestoreUseCase {

    RestoreResult execute(RestoreCommand command);

    @Value
    @Builder
    class RestoreCommand {
        String backupId;

        // Target database connection details
        String targetHost;
        int targetPort;
        String targetDatabase;
        String username;
        String password;
    }

    @Value
    @Builder
    class RestoreResult {
        boolean success;
        String message;
        long durationMs;
    }
}