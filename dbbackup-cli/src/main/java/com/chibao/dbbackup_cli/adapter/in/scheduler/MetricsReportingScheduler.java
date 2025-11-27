package com.chibao.dbbackup_cli.adapter.in.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Metrics Reporting Scheduler
 * Periodically logs metrics summary
 */
@Component
@ConditionalOnProperty(name = "backup.metrics.reporting.enabled", havingValue = "true")
@Slf4j
class MetricsReportingScheduler {

    /**
     * Log metrics summary
     * Runs every hour
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void reportMetrics() {
        log.info("=== Metrics Summary (Last Hour) ===");
        // TODO: Query metrics from Prometheus/Micrometer and log summary
        log.info("Total backups: N/A (not implemented)");
        log.info("Success rate: N/A (not implemented)");
        log.info("Average duration: N/A (not implemented)");
    }
}