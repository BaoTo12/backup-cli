package com.chibao.dbbackup_cli.domain.port.in;

import lombok.Builder;
import lombok.Value;

/**
 * INBOUND PORT: Test Connection Use Case
 */
public interface TestConnectionUseCase {

    ConnectionTestResult execute(ConnectionTestCommand command);

    @Value
    @Builder
    class ConnectionTestCommand {
        String databaseType;
        String host;
        int port;
        String database;
        String username;
        String password;
    }

    @Value
    @Builder
    class ConnectionTestResult {
        boolean success;
        String message;
        long responseTimeMs;
        String databaseVersion;

        public static ConnectionTestResult success(long responseTimeMs, String dbVersion) {
            return ConnectionTestResult.builder()
                    .success(true)
                    .message("Connection successful")
                    .responseTimeMs(responseTimeMs)
                    .databaseVersion(dbVersion)
                    .build();
        }

        public static ConnectionTestResult failure(String message) {
            return ConnectionTestResult.builder()
                    .success(false)
                    .message(message)
                    .responseTimeMs(-1)
                    .build();
        }
    }
}
