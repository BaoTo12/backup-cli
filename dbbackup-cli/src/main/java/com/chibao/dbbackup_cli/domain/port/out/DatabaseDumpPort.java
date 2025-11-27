package com.chibao.dbbackup_cli.domain.port.out;

import com.chibao.dbbackup_cli.domain.model.DatabaseConfig;
import lombok.Builder;
import lombok.Value;

import java.nio.file.Path;
import java.util.Map;

public interface DatabaseDumpPort {
    // Perform database-specific backup/dump
    DumpOutput performDump(DumpConfig config);

    // Restore database from dump file
    void performRestore(RestoreInput input);

    // Test database connection
    boolean testConnection(DatabaseConfig config);

    // Get database type this port handles
    String getSupportedDatabaseType();

    // ===== VALUE OBJECTS =====

    @Value
    @Builder
    class DumpConfig {
        String host;
        int port;
        String database;
        String username;
        String password;
        Path workingDirectory;
        java.util.List<String> tables;  // For selective backup
        Map<String, String> additionalOptions;
    }

    @Value
    @Builder
    class DumpOutput {
        Path dumpFilePath;
        long sizeBytes;
        Map<String, String> metadata;  // Tool version, format, etc.

        public String getMetadata(String key) {
            return metadata != null ? metadata.get(key) : null;
        }
    }

    @Value
    @Builder
    class RestoreInput {
        Path dumpFilePath;
        String targetHost;
        int targetPort;
        String targetDatabase;
        String username;
        String password;
        boolean skipIfExists;
        java.util.List<String> tables;
    }
}
