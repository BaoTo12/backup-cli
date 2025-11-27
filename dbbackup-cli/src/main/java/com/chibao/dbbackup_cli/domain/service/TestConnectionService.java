package com.chibao.dbbackup_cli.domain.service;

import com.chibao.dbbackup_cli.config.DatabaseDumpFactory;
import com.chibao.dbbackup_cli.domain.model.DatabaseConfig;
import com.chibao.dbbackup_cli.domain.port.in.TestConnectionUseCase;
import com.chibao.dbbackup_cli.domain.port.out.DatabaseDumpPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestConnectionService implements TestConnectionUseCase {

    private final DatabaseDumpFactory databaseDumpFactory;

    @Override
    public TestConnectionResult testConnection(TestConnectionCommand command) {
        log.info("Testing connection for database type '{}' on {}:{}",
                command.getDatabaseType(), command.getHost(), command.getPort());

        Instant startTime = Instant.now();
        try {
            // 1. Get the correct adapter from the factory
            DatabaseDumpPort databaseDumpPort = databaseDumpFactory.getAdapter(command.getDatabaseType());

            // 2. Create the database config for the port
            DatabaseConfig dbConfig = DatabaseConfig.builder()
                    .databaseType(command.getDatabaseType())
                    .host(command.getHost())
                    .port(command.getPort())
                    .database(command.getDatabase())
                    .username(command.getUsername())
                    .password(command.getPassword())
                    .build();

            // 3. Delegate the test to the adapter
            boolean success = databaseDumpPort.testConnection(dbConfig);
            long durationMs = Duration.between(startTime, Instant.now()).toMillis();

            if (success) {
                log.info("Connection test successful ({}ms)", durationMs);
                return TestConnectionResult.builder()
                        .success(true)
                        .message("Connection successful.")
                        .durationMs(durationMs)
                        .build();
            } else {
                log.warn("Connection test failed ({}ms)", durationMs);
                return TestConnectionResult.builder()
                        .success(false)
                        .message("Connection failed. Check credentials, host, port, and network access.")
                        .durationMs(durationMs)
                        .build();
            }
        } catch (Exception e) {
            long durationMs = Duration.between(startTime, Instant.now()).toMillis();
            log.error("Connection test threw an exception: {}", e.getMessage(), e);
            return TestConnectionResult.builder()
                    .success(false)
                    .message("Connection failed with an error: " + e.getMessage())
                    .durationMs(durationMs)
                    .build();
        }
    }
}