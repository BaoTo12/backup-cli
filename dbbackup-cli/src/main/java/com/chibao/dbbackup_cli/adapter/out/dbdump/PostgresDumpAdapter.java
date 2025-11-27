package com.chibao.dbbackup_cli.adapter.out.dbdump;

import com.chibao.dbbackup_cli.domain.exception.BackupFailedException;
import com.chibao.dbbackup_cli.domain.model.DatabaseConfig;
import com.chibao.dbbackup_cli.domain.port.out.DatabaseDumpPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * PostgreSQL Database Dump Adapter
 * OUTBOUND ADAPTER - implements DatabaseDumpPort
 * This adapter knows HOW to backup PostgreSQL specifically.
 * Uses pg_dump native tool for best performance and reliability.
 */
@Component("postgresDump")
@Slf4j
public class PostgresDumpAdapter implements DatabaseDumpPort {

    private static final int TIMEOUT_SECONDS = 3600; // 1 hour default
    // flow: USE CASE → DatabaseDumpPort → PostgresDumpAdapter → pg_dump binary → file.dump
    @Override
    public DumpOutput performDump(DumpConfig config) {
        log.info("Starting PostgreSQL dump: database={}, host={}",
                config.getDatabase(), config.getHost());

        try {
            // 1. Prepare output file
            // format file name: {databaseName}_{timestamp}.dump
            Path dumpFile = config.getWorkingDirectory()
                    .resolve(config.getDatabase() + "_" + System.currentTimeMillis() + ".dump");

            // 2. Build pg_dump command
            List<String> command = buildPgDumpCommand(config, dumpFile);

            log.debug("Executing command: {}", String.join(" ", command));

            // 3. Execute pg_dump
            // Tạo một process hệ thống (OS-level) để chạy pg_dump với command đã build từ config.
            // JDBC không thể dump schema, index, sequence, hay BLOBs chuẩn. Chỉ dùng SELECT, cực kỳ chậm và nguy cơ mất dữ liệu.
            ProcessBuilder processBuilder = new ProcessBuilder(command);

            // Set password via environment variable (secure way)
            // truyền password vào pg_dump một cách bảo mật.
            processBuilder.environment().put("PGPASSWORD", config.getPassword());

            // Redirect error stream
            // gộp stderr vào stdout.
            // Dễ log: chỉ cần đọc process.getInputStream()
            // Không bỏ sót lỗi nào từ pg_dump
            // Nếu không gộp: Bạn phải mở 2 stream (stdout và stderr) → dễ treo (deadlock) nếu buffer stderr đầy.
            processBuilder.redirectErrorStream(true);

            // Start process
            Process process = processBuilder.start();

            // Capture output for logging
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    log.debug("pg_dump output: {}", line);
                }
            }

            // Wait for completion
            boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                throw new BackupFailedException("pg_dump timeout after " + TIMEOUT_SECONDS + " seconds");
            }

            int exitCode = process.exitValue();

            if (exitCode != 0) {
                String errorMsg = "pg_dump failed with exit code " + exitCode + ": " + output.toString();
                log.error(errorMsg);
                throw new BackupFailedException(errorMsg);
            }

            // 4. Verify dump file created
            if (!Files.exists(dumpFile)) {
                throw new BackupFailedException("Dump file not created: " + dumpFile);
            }

            long fileSize = Files.size(dumpFile);

            log.info("PostgreSQL dump completed: database={}, size={} bytes, file={}",
                    config.getDatabase(), fileSize, dumpFile);

            // 5. Return output
            return DumpOutput.builder()
                    .dumpFilePath(dumpFile)
                    .sizeBytes(fileSize)
                    .metadata(Map.of(
                            "tool", "pg_dump",
                            "format", "custom",
                            "version", getPgDumpVersion()
                    ))
                    .build();

        } catch (Exception e) {
            log.error("PostgreSQL dump failed: database={}", config.getDatabase(), e);
            throw new BackupFailedException("PostgreSQL dump failed", e);
        }
    }

    @Override
    public void performRestore(RestoreInput input) {
        log.info("Starting PostgreSQL restore: database={}, host={}",
                input.getTargetDatabase(), input.getTargetHost());

        try {
            // Build pg_restore command
            List<String> command = buildPgRestoreCommand(input);

            log.debug("Executing command: {}", String.join(" ", command));

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.environment().put("PGPASSWORD", input.getPassword());
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            // Capture output
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    log.debug("pg_restore output: {}", line);
                }
            }

            boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                throw new BackupFailedException("pg_restore timeout");
            }

            int exitCode = process.exitValue();

            if (exitCode != 0) {
                String errorMsg = "pg_restore failed with exit code " + exitCode + ": " + output.toString();
                log.error(errorMsg);
                throw new BackupFailedException(errorMsg);
            }

            log.info("PostgreSQL restore completed: database={}", input.getTargetDatabase());

        } catch (Exception e) {
            log.error("PostgreSQL restore failed: database={}", input.getTargetDatabase(), e);
            throw new BackupFailedException("PostgreSQL restore failed", e);
        }
    }

    @Override
    public boolean testConnection(DatabaseConfig config) {
        String url = String.format(
                "jdbc:postgresql://%s:%d/%s",
                config.getHost(),
                config.getPort(),
                config.getDatabase()
        );

        log.debug("Testing PostgreSQL connection: {}", url);

        try (Connection conn = DriverManager.getConnection(
                url,
                config.getUsername(),
                config.getPassword()
        )) {
            boolean valid = conn.isValid(5); // 5 seconds timeout
            log.info("PostgreSQL connection test: {} - {}", url, valid ? "SUCCESS" : "FAILED");
            return valid;

        } catch (SQLException e) {
            log.warn("PostgreSQL connection failed: {}, error={}", url, e.getMessage());
            return false;
        }
    }

    @Override
    public String getSupportedDatabaseType() {
        return "postgres";
    }

    // ===== PRIVATE HELPER METHODS =====

    /**
     * Build pg_dump command with options
     */
    private List<String> buildPgDumpCommand(DumpConfig config, Path outputFile) {
        List<String> command = new ArrayList<>();

        command.add("pg_dump");
        command.add("-h");
        command.add(config.getHost());
        command.add("-p");
        command.add(String.valueOf(config.getPort()));
        command.add("-U");
        command.add(config.getUsername());
        command.add("-d");
        command.add(config.getDatabase());

        // Use custom format for best compression and features
        command.add("--format=custom");

        // Exclude ownership and privileges for portability
        command.add("--no-owner");
        command.add("--no-acl");

        // Include large objects
        command.add("--blobs");

        // Selective tables if specified
        if (config.getTables() != null && !config.getTables().isEmpty()) {
            for (String table : config.getTables()) {
                command.add("-t");
                command.add(table);
            }
        }

        // Output file
        command.add("-f");
        command.add(outputFile.toString());

        return command;
    }

    /**
     * Build pg_restore command
     */
    private List<String> buildPgRestoreCommand(RestoreInput input) {
        List<String> command = new ArrayList<>();

        command.add("pg_restore");
        command.add("-h");
        command.add(input.getTargetHost());
        command.add("-p");
        command.add(String.valueOf(input.getTargetPort()));
        command.add("-U");
        command.add(input.getUsername());
        command.add("-d");
        command.add(input.getTargetDatabase());

        // Clean before restore
        command.add("--clean");
        command.add("--if-exists");

        // Exclude ownership
        command.add("--no-owner");
        command.add("--no-acl");

        // Selective tables if specified
        if (input.getTables() != null && !input.getTables().isEmpty()) {
            for (String table : input.getTables()) {
                command.add("-t");
                command.add(table);
            }
        }

        // Input dump file
        command.add(input.getDumpFilePath().toString());

        return command;
    }

    /**
     * Get pg_dump version
     */
    private String getPgDumpVersion() {
        try {
            Process process = new ProcessBuilder("pg_dump", "--version").start();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                return reader.readLine();
            }
        } catch (Exception e) {
            log.warn("Failed to get pg_dump version", e);
            return "unknown";
        }
    }
}

