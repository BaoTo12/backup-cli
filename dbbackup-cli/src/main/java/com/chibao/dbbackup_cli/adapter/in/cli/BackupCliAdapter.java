package com.chibao.dbbackup_cli.adapter.in.cli;

import com.chibao.dbbackup_cli.adapter.in.cli.service.ConsoleService;
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
@ShellComponent // --> → Chỉ định class này là một Spring Shell component. Spring sẽ quét và
                // biến nó thành một tập lệnh CLI.
@RequiredArgsConstructor
public class BackupCliAdapter {

    // ===== DEPENDENCIES: INBOUND PORTS =====

    private final BackupUseCase backupUseCase;
    private final RestoreUseCase restoreUseCase;
    private final TestConnectionUseCase testConnectionUseCase;
    private final ConsoleService consoleService;

    /**
     * * @ShellMethod → Đánh dấu một method là CLI command.
     * value: mô tả command
     * key: tên lệnh khi gọi trong CLI (ví dụ: backup)
     * * @ShellOption → Định nghĩa tham số CLI.
     * Backup command
     * Example:
     * backup --dbType postgres --host localhost --port 5555 --database testdb
     * --username user --password secret
     */
    @ShellMethod(value = "Backup a database", key = "backup")
    public void backup(@ShellOption(help = "Database type (postgres, mysql, mongodb)") String dbType,
            @ShellOption(help = "Database host") String host,
            @ShellOption(help = "Database port") int port,
            @ShellOption(help = "Database name") String database,
            @ShellOption(help = "Username") String username,
            @ShellOption(help = "Password") String password,
            @ShellOption(help = "Compression type (NONE, GZIP, ZIP)", defaultValue = "GZIP") String compression,
            @ShellOption(help = "Enable encryption", defaultValue = "false") boolean encrypt,
            @ShellOption(help = "Storage provider (local, s3, minio)", defaultValue = "local") String storage,
            @ShellOption(help = "Tables to backup (comma-separated)", defaultValue = ShellOption.NULL) String tables) {

        try {
            consoleService.animateProgress("Starting backup...");

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
                printSuccessOutput(result);
            } else {
                printFailureOutput(result);
            }
        } catch (Exception e) {
            consoleService.printError("Error: " + e.getMessage());
        }
    }

    /**
     * Restore command
     * <p>
     * Example:
     * restore --backup-id abc123 --host localhost --port 5432 --database mydb
     * --username postgres --password secret
     */
    @ShellMethod(value = "Restore a database from backup", key = "restore")
    public void restore(@ShellOption(help = "Backup ID to restore") String backupId,
            @ShellOption(help = "Target database host") String host,
            @ShellOption(help = "Target database port", defaultValue = "5432") int port,
            @ShellOption(help = "Target database name") String database,
            @ShellOption(help = "Username") String username,
            @ShellOption(help = "Password") String password,
            @ShellOption(help = "Skip if database exists", defaultValue = "false") boolean skipIfExists,
            @ShellOption(help = "Tables to restore (comma-separated)", defaultValue = ShellOption.NULL) String tables) {

        try {
            consoleService.animateProgress("Starting restore...");

            // Convert CLI args → Domain command
            RestoreUseCase.RestoreCommand command = RestoreUseCase.RestoreCommand.builder().backupId(backupId)
                    .targetHost(host).targetPort(port).targetDatabase(database).username(username).password(password)
                    .skipIfExists(skipIfExists).tables(parseTables(tables)).build();

            // Execute use case
            RestoreUseCase.RestoreResult result = restoreUseCase.execute(command);

            // Format output
            if (result.isSuccess()) {
                consoleService.printSuccess("Restore completed successfully!");
                System.out.println(consoleService.formatKey("Backup ID: ") + result.getBackupId());
                System.out.println(consoleService.formatKey("Duration: ") + result.getDurationMs() + " ms");
                System.out.println(consoleService.formatKey("Message: ") + result.getMessage());
            } else {
                consoleService.printError("Restore failed!");
                System.out.println(consoleService.formatKey("Backup ID: ") + result.getBackupId());
                System.out.println(consoleService.formatKey("Error: ") + result.getMessage());
            }

        } catch (Exception e) {
            consoleService.printError("Error: " + e.getMessage());
        }
    }

    /**
     * Test database connection
     * <p>
     * Example:
     * test-connection --db-type postgres --host localhost --port 5432 --database
     * mydb --username postgres --password secret
     */
    @ShellMethod(value = "Test database connection", key = "test-connection")
    public void testConnection(@ShellOption(help = "Database type (postgres, mysql, mongodb)") String dbType,
            @ShellOption(help = "Database host") String host,
            @ShellOption(help = "Database port", defaultValue = "5432") int port,
            @ShellOption(help = "Database name") String database,
            @ShellOption(help = "Username") String username,
            @ShellOption(help = "Password") String password) {

        try {
            consoleService.animateProgress("Testing connection...");

            // Convert CLI args → Domain command
            TestConnectionUseCase.TestConnectionCommand command = TestConnectionUseCase.TestConnectionCommand.builder()
                    .databaseType(dbType.toLowerCase())
                    .host(host)
                    .port(port)
                    .database(database)
                    .username(username)
                    .password(password)
                    .build();

            // Execute use case
            TestConnectionUseCase.TestConnectionResult result = testConnectionUseCase.testConnection(command);

            // Format output
            if (result.isSuccess()) {
                consoleService.printSuccess("Connection successful!");
                System.out.println(consoleService.formatKey("Response time: ") + result.getDurationMs() + " ms");
                System.out.println(consoleService.formatKey("Message: ") + result.getMessage());
            } else {
                consoleService.printError("Connection failed!");
                System.out.println(consoleService.formatKey("Error: ") + result.getMessage());
            }

        } catch (Exception e) {
            consoleService.printError("Error: " + e.getMessage());
        }
    }

    // ===== PRIVATE HELPER METHODS =====

    private List<String> parseTables(String tables) {
        if (tables == null || tables.trim().isEmpty()) {
            return null;
        }
        return Arrays.stream(tables.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    private void printSuccessOutput(BackupUseCase.BackupResult result) {
        BackupUseCase.BackupMetadata metadata = result.getMetadata();

        consoleService.printSuccess("Backup completed successfully!");
        System.out.println();
        System.out.println(consoleService.formatKey("Backup ID: ") + result.getBackupId());
        System.out.println(consoleService.formatKey("Storage Location: ") + metadata.getStorageLocation());
        System.out.println(consoleService.formatKey("Size: ") + formatBytes(metadata.getSizeBytes()));
        System.out.println(consoleService.formatKey("Checksum (SHA-256): ") + metadata.getChecksum());
        System.out.println(consoleService.formatKey("Duration: ") + metadata.getDurationMs() + " ms");
        System.out.println();
        System.out.println(consoleService.formatKey("Message: ") + result.getMessage());
    }

    private void printFailureOutput(BackupUseCase.BackupResult result) {
        consoleService.printError("Backup failed!");
        System.out.println();
        System.out.println(consoleService.formatKey("Backup ID: ") + result.getBackupId());
        System.out.println(consoleService.formatKey("Error: ") + result.getMessage());
        System.out.println();
        consoleService.printWarning("Please check logs for details.");
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