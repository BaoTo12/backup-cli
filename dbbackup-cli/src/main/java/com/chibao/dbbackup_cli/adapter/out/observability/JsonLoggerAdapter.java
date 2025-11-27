package com.chibao.dbbackup_cli.adapter.out.observability;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * JSON Logger Adapter
 * Logs structured events in JSON format for easy parsing.
 * Useful for log aggregation systems (ELK, Splunk, etc.)
 */
@Component
@Slf4j
class JsonLoggerAdapter {

    /**
     * Log backup started event
     */
    public void logBackupStarted(String backupId, String dbType, String dbName) {
        LogEvent event = LogEvent.builder()
                .timestamp(java.time.Instant.now().toString())
                .level("INFO")
                .event("BACKUP_STARTED")
                .backupId(backupId)
                .metadata(java.util.Map.of(
                        "dbType", dbType,
                        "dbName", dbName
                ))
                .build();

        log.info("BACKUP_STARTED: {}", toJson(event));
    }

    /**
     * Log backup completed event
     */
    public void logBackupCompleted(
            String backupId,
            String dbType,
            long sizeBytes,
            long durationMs,
            String storageLocation
    ) {
        LogEvent event = LogEvent.builder()
                .timestamp(java.time.Instant.now().toString())
                .level("INFO")
                .event("BACKUP_COMPLETED")
                .backupId(backupId)
                .metadata(java.util.Map.of(
                        "dbType", dbType,
                        "sizeBytes", String.valueOf(sizeBytes),
                        "durationMs", String.valueOf(durationMs),
                        "storageLocation", storageLocation
                ))
                .message("Backup completed successfully")
                .build();

        log.info("BACKUP_COMPLETED: {}", toJson(event));
    }

    /**
     * Log backup failed event
     */
    public void logBackupFailed(String backupId, String dbType, String error) {
        LogEvent event = LogEvent.builder()
                .timestamp(java.time.Instant.now().toString())
                .level("ERROR")
                .event("BACKUP_FAILED")
                .backupId(backupId)
                .metadata(java.util.Map.of(
                        "dbType", dbType,
                        "error", error
                ))
                .message("Backup failed")
                .build();

        log.error("BACKUP_FAILED: {}", toJson(event));
    }

    /**
     * Log upload started event
     */
    public void logUploadStarted(String backupId, String provider, long sizeBytes) {
        LogEvent event = LogEvent.builder()
                .timestamp(java.time.Instant.now().toString())
                .level("INFO")
                .event("UPLOAD_STARTED")
                .backupId(backupId)
                .metadata(java.util.Map.of(
                        "provider", provider,
                        "sizeBytes", String.valueOf(sizeBytes)
                ))
                .build();

        log.info("UPLOAD_STARTED: {}", toJson(event));
    }

    /**
     * Convert to JSON string
     */
    private String toJson(LogEvent event) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper =
                    new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            return mapper.writeValueAsString(event);
        } catch (Exception e) {
            return event.toString();
        }
    }

    /**
     * Log event structure
     */
    @lombok.Builder
    @lombok.Value
    static class LogEvent {
        String timestamp;
        String level;
        String event;
        String backupId;
        java.util.Map<String, String> metadata;
        String message;
    }
}
