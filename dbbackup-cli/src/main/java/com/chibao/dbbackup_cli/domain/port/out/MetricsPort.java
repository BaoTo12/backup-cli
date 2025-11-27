package com.chibao.dbbackup_cli.domain.port.out;

/**
 * OUTBOUND PORT: Metrics Port
 * Core cần record metrics cho observability
 */
public interface MetricsPort {

    /**
     * Record backup duration
     * @param dbType database type
     * @param durationMs duration in milliseconds
     * @param success whether backup succeeded
     */
    void recordBackupDuration(String dbType, long durationMs, boolean success);

    /**
     * Record backup size
     * @param dbType database type
     * @param sizeBytes size in bytes
     */
    void recordBackupSize(String dbType, long sizeBytes);

    /**
     * Increment backup counter
     * @param dbType database type
     * @param status backup status (success, failure)
     */
    void incrementBackupCount(String dbType, String status);

    /**
     * Record upload duration
     * @param provider storage provider
     * @param durationMs duration in milliseconds
     */
    void recordUploadDuration(String provider, long durationMs);

    /**
     * Record retry count
     * @param operation operation name
     * @param retryCount number of retries
     */
    void recordRetryCount(String operation, int retryCount);
}