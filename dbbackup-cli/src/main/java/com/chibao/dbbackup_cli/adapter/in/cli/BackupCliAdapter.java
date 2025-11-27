package com.chibao.dbbackup_cli.adapter.in.cli;

import com.chibao.dbbackup_cli.domain.model.CompressionType;
import com.chibao.dbbackup_cli.domain.port.in.BackupUseCase;
import com.chibao.dbbackup_cli.domain.port.in.RestoreUseCase;
import com.chibao.dbbackup_cli.domain.port.in.TestConnectionUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.Arrays;
import java.util.List;

/**
 * CLI Adapter - INBOUND ADAPTER
 * <p>
 * Uses Spring Shell for CLI commands.
 * Depends on INBOUND PORTS (use cases).
 * Converts CLI args → Domain commands.
 * Formats domain results → CLI output.
 */
@ShellComponent // --> → Chỉ định class này là một Spring Shell component. Spring sẽ quét và biến nó thành một tập lệnh CLI.
@RequiredArgsConstructor
public class BackupCliAdapter {

    // ===== DEPENDENCIES: INBOUND PORTS =====

    private final BackupUseCase backupUseCase;
    private final RestoreUseCase restoreUseCase;
    private final TestConnectionUseCase testConnectionUseCase;

    /**
     * * @ShellMethod → Đánh dấu một method là CLI command.
     * value: mô tả command
     * key: tên lệnh khi gọi trong CLI (ví dụ: backup)
     * * @ShellOption → Định nghĩa tham số CLI.
     * Backup command
     * Example:
     * backup --db-type postgres --host localhost --port 5432 --database mydb --username postgres --password secret
     */
    @ShellMethod(value = "Backup a database", key = "backup")
    public String backup(@ShellOption(help = "Database type (postgres, mysql, mongodb)") String dbType,
                         @ShellOption(help = "Database host") String host,
                         @ShellOption(help = "Database port", defaultValue = "5432") int port,
                         @ShellOption(help = "Database name") String database,
                         @ShellOption(help = "Username") String username,
                         @ShellOption(help = "Password") String password,
                         @ShellOption(help = "Compression type (NONE, GZIP, ZIP)", defaultValue = "GZIP") String compression,
                         @ShellOption(help = "Enable encryption", defaultValue = "false") boolean encrypt,
                         @ShellOption(help = "Storage provider (local, s3, minio)", defaultValue = "local") String storage,
                         @ShellOption(help = "Tables to backup (comma-separated)", defaultValue = ShellOption.NULL) String tables) {

        try {
            // Convert CLI args → Domain command
            BackupUseCase.BackupCommand command = BackupUseCase.BackupCommand
                    .builder()
                    .databaseType(dbType.toLowerCase())
                    .host(host)
                    .port(port)
                    .database(database)
                    .username(username)
                    .password(password)
                    .compression(CompressionType.valueOf(compression.toUpperCase()))
                    .encrypt(encrypt)
                    .storageProvider(storage.toLowerCase())
                    .tables(parseTables(tables))
                    .build();

            // Execute use case
            BackupUseCase.BackupResult result = backupUseCase.execute(command);

            // Format output for CLI
            if (result.isSuccess()) {
                return formatSuccessOutput(result);
            } else {
                return formatFailureOutput(result);
            }
        } catch (Exception e) {
            return String.format("❌ Error: %s", e.getMessage());
        }
    }

    /**
     * Restore command
     * <p>
     * Example:
     * restore --backup-id abc123 --host localhost --port 5432 --database mydb --username postgres --password secret
     */
    @ShellMethod(value = "Restore a database from backup", key = "restore")
    public String restore(@ShellOption(help = "Backup ID to restore") String backupId,
                          @ShellOption(help = "Target database host") String host,
                          @ShellOption(help = "Target database port", defaultValue = "5432") int port,
                          @ShellOption(help = "Target database name") String database,
                          @ShellOption(help = "Username") String username,
                          @ShellOption(help = "Password") String password,
                          @ShellOption(help = "Skip if database exists", defaultValue = "false") boolean skipIfExists,
                          @ShellOption(help = "Tables to restore (comma-separated)", defaultValue = ShellOption.NULL) String tables) {

        try {
            // Convert CLI args → Domain command
            RestoreUseCase.RestoreCommand command = RestoreUseCase.RestoreCommand.builder().backupId(backupId).targetHost(host).targetPort(port).targetDatabase(database).username(username).password(password).skipIfExists(skipIfExists).tables(parseTables(tables)).build();

            // Execute use case
            RestoreUseCase.RestoreResult result = restoreUseCase.execute(command);

            // Format output
            if (result.isSuccess()) {
                return String.format("✅ Restore completed successfully!%n" + "Backup ID: %s%n" + "Duration: %d ms%n" + "Message: %s", result.getBackupId(), result.getDurationMs(), result.getMessage());
            } else {
                return String.format("❌ Restore failed!%n" + "Backup ID: %s%n" + "Error: %s", result.getBackupId(), result.getMessage());
            }

        } catch (Exception e) {
            return String.format("❌ Error: %s", e.getMessage());
        }
    }

    /**
     * Test database connection
     * <p>
     * Example:
     * test-connection --db-type postgres --host localhost --port 5432 --database mydb --username postgres --password secret
     */
    @ShellMethod(value = "Test database connection", key = "test-connection")
    public String testConnection(@ShellOption(help = "Database type (postgres, mysql, mongodb)") String dbType,
                                 @ShellOption(help = "Database host") String host,
                                 @ShellOption(help = "Database port", defaultValue = "5432") int port,
                                 @ShellOption(help = "Database name") String database,
                                 @ShellOption(help = "Username") String username,
                                 @ShellOption(help = "Password") String password) {

        try {
            // Convert CLI args → Domain command
            TestConnectionUseCase.ConnectionTestCommand command = TestConnectionUseCase.ConnectionTestCommand.builder().databaseType(dbType.toLowerCase()).host(host).port(port).database(database).username(username).password(password).build();

            // Execute use case
            TestConnectionUseCase.ConnectionTestResult result = testConnectionUseCase.execute(command);

            // Format output
            if (result.isSuccess()) {
                return String.format("✅ Connection successful!%n" + "Database: %s@%s:%d/%s%n" + "Version: %s%n" + "Response time: %d ms%n" + "Message: %s", username, host, port, database, result.getDatabaseVersion(), result.getResponseTimeMs(), result.getMessage());
            } else {
                return String.format("❌ Connection failed!%n" + "Database: %s@%s:%d/%s%n" + "Error: %s", username, host, port, database, result.getMessage());
            }

        } catch (Exception e) {
            return String.format("❌ Error: %s", e.getMessage());
        }
    }

    /**
     * List backups command
     */
    @ShellMethod(value = "List backups", key = "list-backups")
    public String listBackups(@ShellOption(help = "Database name filter", defaultValue = ShellOption.NULL) String database,
                              @ShellOption(help = "Database type filter", defaultValue = ShellOption.NULL) String dbType,
                              @ShellOption(help = "Limit results", defaultValue = "10") int limit) {
        return "📋 List backups feature coming soon...";
    }

    // ===== PRIVATE HELPER METHODS =====

    private List<String> parseTables(String tables) {
        if (tables == null || tables.trim().isEmpty()) {
            return null;
        }
        return Arrays.stream(tables.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    private String formatSuccessOutput(BackupUseCase.BackupResult result) {
        BackupUseCase.BackupMetadata metadata = result.getMetadata();

        return String.format("✅ Backup completed successfully!%n" + "%n" + "Backup ID: %s%n" + "Storage Location: %s%n" + "Size: %s%n" + "Checksum (SHA-256): %s%n" + "Duration: %d ms%n" + "%n" + "Message: %s", result.getBackupId(), metadata.getStorageLocation(), formatBytes(metadata.getSizeBytes()), metadata.getChecksum(), metadata.getDurationMs(), result.getMessage());
    }

    private String formatFailureOutput(BackupUseCase.BackupResult result) {
        return String.format("❌ Backup failed!%n" + "%n" + "Backup ID: %s%n" + "Error: %s%n" + "%n" + "Please check logs for details.", result.getBackupId(), result.getMessage());
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }
}