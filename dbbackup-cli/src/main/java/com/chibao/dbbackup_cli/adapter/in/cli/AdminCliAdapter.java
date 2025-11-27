package com.chibao.dbbackup_cli.adapter.in.cli;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
class AdminCliAdapter {

//
//    @ShellMethod(key = "test-connection", value = "Test database connection")
//    public String testConnection(
//            @ShellOption(help = "Database type (mysql, postgres, sqlite, mongodb)") String dbtype,
//            @ShellOption(help = "Database host", defaultValue = "localhost") String host,
//            @ShellOption(help = "Database port", defaultValue = "") String port,
//            @ShellOption(help = "Database username", defaultValue = "") String username,
//            @ShellOption(help = "Database password", defaultValue = "") String password,
//            @ShellOption(help = "Database name") String dbname) {
//        return databaseConnectionService.testConnection(dbtype, host, port, username, password, dbname);
//    }
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
