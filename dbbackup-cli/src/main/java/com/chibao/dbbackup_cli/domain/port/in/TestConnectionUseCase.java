package com.chibao.dbbackup_cli.domain.port.in;

import lombok.Builder;
import lombok.Value;

/**
 * Inbound Port for testing database connections.
 */
public interface TestConnectionUseCase {

    TestConnectionResult testConnection(TestConnectionCommand command);

    @Value
    @Builder
    class TestConnectionCommand {
        String databaseType;
        String host;
        int port;
        String database;
        String username;
        String password;
    }

    @Value
    @Builder
    class TestConnectionResult {
        boolean success;
        String message;
        long durationMs;
    }
}
