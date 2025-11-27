package com.chibao.dbbackup_cli.adapter.in.cli;

import com.chibao.dbbackup_cli.domain.port.in.TestConnectionUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
@RequiredArgsConstructor
public class AdminCliAdapter {

    private final TestConnectionUseCase testConnectionUseCase;

    @ShellMethod(value = "Test database connection.", key = "test-connection")
    public String testConnection(
            @ShellOption(help = "Database type (e.g., postgres, mysql, mongodb)") String databaseType,
            @ShellOption(help = "Database host") String host,
            @ShellOption(help = "Database port") int port,
            @ShellOption(help = "Database name") String database,
            @ShellOption(help = "Username for database connection") String username,
            @ShellOption(help = "Password for database connection", value = {"--password", "-p"}) String password
    ) {
        // The adapter is responsible for mapping external input to the domain's command object.
        TestConnectionUseCase.TestConnectionCommand command = TestConnectionUseCase.TestConnectionCommand.builder()
                .databaseType(databaseType)
                .host(host)
                .port(port)
                .database(database)
                .username(username)
                .password(password)
                .build();

        TestConnectionUseCase.TestConnectionResult result = testConnectionUseCase.testConnection(command);

        if (result.isSuccess()) {
            return String.format("✅ Connection successful! (took %dms)", result.getDurationMs());
        } else {
            return String.format("❌ %s (took %dms)", result.getMessage(), result.getDurationMs());
        }
    }

    @ShellMethod(value = "Display version", key = "version")
    public String version() {
        return "DB Backup CLI v1.0.0";
    }

    @ShellMethod(value = "Display help", key = "help")
    public String help() {
        return """
                DB Backup CLI - Multi-Database Backup Utility
                
                Available commands:
                
                backup           - Backup a database
                restore          - Restore a database from backup
                test-connection  - Test database connection
                list-backups     - List available backups
                version          - Display version
                help             - Display this help
                
                For detailed help on a command, type: help <command>
                
                Examples:
                  backup --db-type postgres --host localhost --database mydb --username postgres --password secret
                  restore --backup-id abc123 --host localhost --database mydb_restored --username postgres --password secret
                  test-connection --db-type mysql --host localhost --database mydb --username root --password secret
                """;
    }
}
