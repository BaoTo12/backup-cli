package com.chibao.dbbackup_cli.adapter.in.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Cleanup Scheduler
 * Periodically cleans up:
 * - Expired backups based on retention policy
 * - Temporary files
 * - Failed backup artifacts
 */
@Component
@ConditionalOnProperty(name = "backup.cleanup.enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
class CleanupScheduler {

    @Value("${backup.retention.days:30}")
    private int retentionDays;

    @Value("${backup.local.path:/var/lib/dbbackup}")
    private String backupPath;

    /**
     * Cleanup expired backups
     * Runs daily at 3 AM
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupExpiredBackups() {
        log.info("=== Starting cleanup job: retention={} days ===", retentionDays);

        try {
            java.nio.file.Path backupDir = java.nio.file.Paths.get(backupPath);

            if (!java.nio.file.Files.exists(backupDir)) {
                log.warn("Backup directory does not exist: {}", backupPath);
                return;
            }

            java.time.Instant cutoffDate = java.time.Instant.now()
                    .minus(retentionDays, java.time.temporal.ChronoUnit.DAYS);

            log.info("Deleting backups older than: {}", cutoffDate);

            int deletedCount = 0;

            // Find and delete old backups
            try (java.util.stream.Stream<java.nio.file.Path> paths = java.nio.file.Files.walk(backupDir)) {
                deletedCount = (int) paths
                        .filter(java.nio.file.Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".tar.gz") ||
                                path.toString().endsWith(".meta.json"))
                        .filter(path -> {
                            try {
                                java.nio.file.attribute.FileTime fileTime =
                                        java.nio.file.Files.getLastModifiedTime(path);
                                return fileTime.toInstant().isBefore(cutoffDate);
                            } catch (Exception e) {
                                log.warn("Failed to check file time: {}", path, e);
                                return false;
                            }
                        })
                        .peek(path -> log.debug("Deleting expired backup: {}", path))
                        .filter(path -> {
                            try {
                                java.nio.file.Files.delete(path);
                                return true;
                            } catch (Exception e) {
                                log.error("Failed to delete file: {}", path, e);
                                return false;
                            }
                        })
                        .count();
            }

            log.info("=== Cleanup job completed: deleted {} file(s) ===", deletedCount);

        } catch (Exception e) {
            log.error("Cleanup job failed", e);
        }
    }

    /**
     * Cleanup temporary files
     *
     * Runs every hour
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void cleanupTempFiles() {
        log.debug("Cleaning up temporary files");

        try {
            java.nio.file.Path tempDir = java.nio.file.Paths.get(System.getProperty("java.io.tmpdir"));

            // Delete files older than 24 hours in temp directory
            java.time.Instant cutoff = java.time.Instant.now()
                    .minus(24, java.time.temporal.ChronoUnit.HOURS);

            try (java.util.stream.Stream<java.nio.file.Path> paths = java.nio.file.Files.walk(tempDir, 1)) {
                long deletedCount = paths
                        .filter(java.nio.file.Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().startsWith("dbbackup_") ||
                                path.getFileName().toString().startsWith("backup_") ||
                                path.getFileName().toString().startsWith("metadata_"))
                        .filter(path -> {
                            try {
                                java.nio.file.attribute.FileTime fileTime =
                                        java.nio.file.Files.getLastModifiedTime(path);
                                return fileTime.toInstant().isBefore(cutoff);
                            } catch (Exception e) {
                                return false;
                            }
                        })
                        .peek(path -> log.trace("Deleting temp file: {}", path))
                        .filter(path -> {
                            try {
                                java.nio.file.Files.delete(path);
                                return true;
                            } catch (Exception e) {
                                log.warn("Failed to delete temp file: {}", path, e);
                                return false;
                            }
                        })
                        .count();

                if (deletedCount > 0) {
                    log.info("Cleaned up {} temporary file(s)", deletedCount);
                }
            }

        } catch (Exception e) {
            log.error("Failed to cleanup temp files", e);
        }
    }
}
