package com.chibao.dbbackup_cli.adapter.in.scheduler;

import com.chibao.dbbackup_cli.domain.model.CompressionType;
import com.chibao.dbbackup_cli.domain.port.in.BackupUseCase;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scheduled Backup Adapter
 *
 * INBOUND ADAPTER - Scheduled jobs for automatic backups
 *
 * Runs periodic backups based on configuration.
 * Can be enabled/disabled via properties.
 * Configuration:
 * - backup.scheduler.enabled=true
 * - backup.scheduler.cron=0 0 2 * * ? (daily at 2 AM)
 * - backup.scheduler.databases=postgres:mydb,mysql:testdb
 */
@Component
@ConditionalOnProperty(name = "backup.scheduler.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class ScheduledBackupAdapter {

    private final BackupUseCase backupUseCase;

    @Value("${backup.scheduler.databases:}")
    private String configuredDatabases;

    @Value("${backup.scheduler.storage-provider:local}")
    private String storageProvider;

    @Value("${backup.scheduler.compression:GZIP}")
    private String compression;

    @Value("${backup.scheduler.encrypt:false}")
    private boolean encrypt;

    private List<DatabaseScheduleConfig> scheduleConfigs;

    @PostConstruct
    public void init() {
        log.info("Scheduled backup adapter initialized");
        log.info("Configured databases: {}", configuredDatabases);
        log.info("Storage provider: {}", storageProvider);

        // Parse configured databases
        scheduleConfigs = parseDatabaseConfigs(configuredDatabases);

        if (scheduleConfigs.isEmpty()) {
            log.warn("No databases configured for scheduled backups!");
        } else {
            log.info("Scheduled backups enabled for {} database(s)", scheduleConfigs.size());
        }
    }

    /**
     * Scheduled backup job
     * Runs based on cron expression in configuration.
     * Default: Daily at 2 AM (0 0 2 * * ?)
     * Cron format: second minute hour day month weekday
     * Examples:
     * - Every hour: 0 0 * * * ?
     * - Every day at 2 AM: 0 0 2 * * ?
     * - Every Sunday at 3 AM: 0 0 3 ? * SUN
     */
    @Scheduled(cron = "${backup.scheduler.cron:0 0 2 * * ?}")
    public void executeScheduledBackups() {
        log.info("=== Starting scheduled backup job ===");

        if (scheduleConfigs.isEmpty()) {
            log.warn("No databases configured for scheduled backups - skipping");
            return;
        }

        int successCount = 0;
        int failureCount = 0;

        for (DatabaseScheduleConfig config : scheduleConfigs) {
            try {
                log.info("Backing up database: {} ({})", config.getDatabase(), config.getDatabaseType());

                BackupUseCase.BackupResult result = backupDatabase(config);

                if (result.isSuccess()) {
                    successCount++;
                    log.info("✓ Backup successful: backupId={}, database={}",
                            result.getBackupId(), config.getDatabase());
                } else {
                    failureCount++;
                    log.error("✗ Backup failed: database={}, error={}",
                            config.getDatabase(), result.getMessage());
                }

            } catch (Exception e) {
                failureCount++;
                log.error("✗ Backup exception: database={}", config.getDatabase(), e);
            }
        }

        log.info("=== Scheduled backup job completed: success={}, failed={} ===",
                successCount, failureCount);
    }

    /**
     * Backup a single database
     */
    private BackupUseCase.BackupResult backupDatabase(DatabaseScheduleConfig config) {

        // Build backup command
        BackupUseCase.BackupCommand command = BackupUseCase.BackupCommand.builder()
                .databaseType(config.getDatabaseType())
                .host(config.getHost())
                .port(config.getPort())
                .database(config.getDatabase())
                .username(config.getUsername())
                .password(config.getPassword())
                .compression(CompressionType.valueOf(compression.toUpperCase()))
                .encrypt(encrypt)
                .storageProvider(storageProvider)
                .build();

        // Execute backup
        return backupUseCase.execute(command);
    }

    /**
     * Parse database configuration string
     *
     * Format: "dbtype:host:port:database:username:password,dbtype:host:port:database:username:password"
     * Example: "postgres:localhost:5432:mydb:postgres:secret,mysql:localhost:3306:testdb:root:pass"
     */
    private List<DatabaseScheduleConfig> parseDatabaseConfigs(String configString) {
        if (configString == null || configString.trim().isEmpty()) {
            return List.of();
        }

        return java.util.Arrays.stream(configString.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(this::parseSingleConfig)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    /**
     * Parse single database configuration
     */
    private DatabaseScheduleConfig parseSingleConfig(String configPart) {
        try {
            String[] parts = configPart.split(":");

            if (parts.length < 6) {
                log.warn("Invalid database config format: {}. Expected format: dbtype:host:port:database:username:password",
                        configPart);
                return null;
            }

            return DatabaseScheduleConfig.builder()
                    .databaseType(parts[0].trim())
                    .host(parts[1].trim())
                    .port(Integer.parseInt(parts[2].trim()))
                    .database(parts[3].trim())
                    .username(parts[4].trim())
                    .password(parts[5].trim())
                    .build();

        } catch (Exception e) {
            log.error("Failed to parse database config: {}", configPart, e);
            return null;
        }
    }

    /**
     * Database Schedule Configuration
     */
    @lombok.Value
    @lombok.Builder
    static class DatabaseScheduleConfig {
        String databaseType;
        String host;
        int port;
        String database;
        String username;
        String password;
    }
}