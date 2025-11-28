package com.chibao.dbbackup_cli.adapter.out.observability;

import com.chibao.dbbackup_cli.domain.port.out.MetricsPort;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Prometheus Metrics Adapter
 *
 * OUTBOUND ADAPTER - implements MetricsPort
 *
 * Records metrics using Micrometer (Prometheus-compatible).
 * Exposes metrics via /actuator/prometheus endpoint.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PrometheusMetricsAdapter implements MetricsPort {

        private final MeterRegistry meterRegistry;

        // Metric name prefix
        private static final String METRIC_PREFIX = "dbbackup";

        @Override
        public void recordBackupDuration(String dbType, long durationMs, boolean success) {

                String result = success ? "success" : "failure";

                // Record as Timer metric
                // Metric name: dbbackup_backup_duration_seconds
                Timer.builder(METRIC_PREFIX + ".backup.duration")
                                .description("Duration of backup operations in seconds")
                                .tag("dbtype", dbType.toLowerCase())
                                .tag("result", result)
                                .register(meterRegistry)
                                .record(durationMs, TimeUnit.MILLISECONDS);

                log.debug("Recorded backup duration metric: dbType={}, duration={}ms, result={}",
                                dbType, durationMs, result);
        }

        @Override
        public void recordBackupSize(String dbType, long sizeBytes) {

                // Record as Gauge metric
                // Metric name: dbbackup_backup_size_bytes
                meterRegistry.gauge(
                                METRIC_PREFIX + ".backup.size.bytes",
                                io.micrometer.core.instrument.Tags.of("dbtype", dbType.toLowerCase()),
                                sizeBytes);

                log.debug("Recorded backup size metric: dbType={}, size={} bytes", dbType, sizeBytes);
        }

        @Override
        public void incrementBackupCount(String dbType, String status) {

                // Record as Counter metric
                // Metric name: dbbackup_backup_total
                Counter.builder(METRIC_PREFIX + ".backup.total")
                                .description("Total number of backup operations")
                                .tag("dbtype", dbType.toLowerCase())
                                .tag("status", status.toLowerCase())
                                .register(meterRegistry)
                                .increment();

                log.debug("Incremented backup count metric: dbType={}, status={}", dbType, status);
        }

        @Override
        public void recordUploadDuration(String provider, long durationMs) {

                // Record as Timer metric
                // Metric name: dbbackup_upload_duration_seconds
                Timer.builder(METRIC_PREFIX + ".upload.duration")
                                .description("Duration of storage upload operations in seconds")
                                .tag("provider", provider.toLowerCase())
                                .register(meterRegistry)
                                .record(durationMs, TimeUnit.MILLISECONDS);

                log.debug("Recorded upload duration metric: provider={}, duration={}ms", provider, durationMs);
        }

        @Override
        public void recordRetryCount(String operation, int retryCount) {

                // Record as Counter metric
                // Metric name: dbbackup_retry_total
                Counter.builder(METRIC_PREFIX + ".retry.total")
                                .description("Total number of retry attempts")
                                .tag("operation", operation)
                                .register(meterRegistry)
                                .increment(retryCount);

                log.debug("Recorded retry count metric: operation={}, count={}", operation, retryCount);
        }

        /**
         * Custom method: Record restore duration
         * (Can add to MetricsPort interface if needed)
         */
        public void recordRestoreDuration(String dbType, long durationMs, boolean success) {
                String result = success ? "success" : "failure";

                Timer.builder(METRIC_PREFIX + ".restore.duration")
                                .description("Duration of restore operations in seconds")
                                .tag("dbtype", dbType.toLowerCase())
                                .tag("result", result)
                                .register(meterRegistry)
                                .record(durationMs, TimeUnit.MILLISECONDS);
        }

        /**
         * Custom method: Record compression ratio
         */
        public void recordCompressionRatio(String algorithm, double ratio) {
                meterRegistry.gauge(
                                METRIC_PREFIX + ".compression.ratio",
                                io.micrometer.core.instrument.Tags.of("algorithm", algorithm),
                                ratio);
        }
}