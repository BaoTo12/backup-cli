package com.chibao.backup_ulity_cli.cli;

import com.chibao.backup_ulity_cli.service.DatabaseConnectionService;
import com.chibao.backup_ulity_cli.service.BackupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class BackupCommands {

    private final DatabaseConnectionService databaseConnectionService;
    private final BackupService backupService;

    @Autowired
    public BackupCommands(DatabaseConnectionService databaseConnectionService, BackupService backupService) {
        this.databaseConnectionService = databaseConnectionService;
        this.backupService = backupService;
    }

    @ShellMethod(key = "test-connection", value = "Test database connection")
    public String testConnection(
            @ShellOption(help = "Database type (mysql, postgres, sqlite, mongodb)") String dbtype,
            @ShellOption(help = "Database host", defaultValue = "localhost") String host,
            @ShellOption(help = "Database port", defaultValue = "") String port,
            @ShellOption(help = "Database username", defaultValue = "") String username,
            @ShellOption(help = "Database password", defaultValue = "") String password,
            @ShellOption(help = "Database name") String dbname) {
        return databaseConnectionService.testConnection(dbtype, host, port, username, password, dbname);
    }

    @ShellMethod(key = "backup", value = "Backup database")
    public String backup(
            @ShellOption(help = "Database type (mysql, postgres, sqlite, mongodb)") String dbtype,
            @ShellOption(help = "Database host", defaultValue = "localhost") String host,
            @ShellOption(help = "Database username", defaultValue = "") String username,
            @ShellOption(help = "Database name") String dbname,
            @ShellOption(help = "Database password", defaultValue = "") String password,
            @ShellOption(help = "Output directory", defaultValue = "./backups") String output,
            @ShellOption(help = "Compression type (gzip, zip, none)", defaultValue = "gzip") String compress) {
        return backupService.performBackup(dbtype, host, username, password, dbname, output, compress);
    }

    @ShellMethod(key = "restore", value = "Restore database")
    public String restore(
            @ShellOption(help = "Backup file path") String file,
            @ShellOption(help = "Database type (mysql, postgres, sqlite, mongodb)") String dbtype,
            @ShellOption(help = "Database host", defaultValue = "localhost") String host,
            @ShellOption(help = "Database username", defaultValue = "") String username,
            @ShellOption(help = "Database name") String dbname) {
        // TODO: Implement restore logic
        return "Restore started for " + dbname + " from " + file;
    }

    @ShellMethod(key = "status", value = "Check status of operations")
    public String status(
            @ShellOption(help = "Log file path", defaultValue = "./backups/logs/backup.log") String log) {
        // TODO: Implement status logic
        return "Checking status from " + log;
    }
}
