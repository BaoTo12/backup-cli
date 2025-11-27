package com.chibao.dbbackup_cli.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "backups")
public class Backup {

    @Id
    @Column(nullable = false, updatable = false)
    private String id;

    @Column(nullable = false)
    private String databaseType;      // postgres, mysql, mongodb

    @Column(nullable = false)
    private String databaseName;

    private String host;
    private int port;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BackupStatus status;

    @Column(nullable = false)
    private Instant createdAt;
    private Instant completedAt;

    private Long sizeBytes;

    private String checksum;          // SHA-256

    @Enumerated(EnumType.STRING)
    private CompressionType compression;
    private boolean encrypted;

    private String storageLocation;   // S3 key, local path, etc.

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> metadata;

    // ===== BUSINESS LOGIC (Pure domain logic) =====

    /**
     * Check if backup is expired based on retention policy
     */
    public boolean isExpired(int retentionDays) {
        if (createdAt == null) {
            return false;
        }
        Instant expirationDate = createdAt.plus(retentionDays, ChronoUnit.DAYS);
        return Instant.now().isAfter(expirationDate);
    }

    /**
     * Mark backup as completed with metadata
     */
    public Backup markAsCompleted(String checksum, long sizeBytes, String storageLocation) {
        return this.toBuilder()
                .status(BackupStatus.COMPLETED)
                .completedAt(Instant.now())
                .checksum(checksum)
                .sizeBytes(sizeBytes)
                .storageLocation(storageLocation)
                .build();
    }

    /**
     * Mark backup as failed
     */
    public Backup markAsFailed(String errorMessage) {
        return this.toBuilder()
                .status(BackupStatus.FAILED)
                .completedAt(Instant.now())
                .metadata(Map.of("error", errorMessage))
                .build();
    }

    /**
     * Get duration in seconds
     */
    public long getDurationSeconds() {
        if (createdAt == null || completedAt == null) {
            return 0;
        }
        return ChronoUnit.SECONDS.between(createdAt, completedAt);
    }

    /**
     * Check if backup is in progress
     */
    public boolean isInProgress() {
        return status == BackupStatus.IN_PROGRESS;
    }

    /**
     * Check if backup is successful
     */
    public boolean isSuccessful() {
        return status == BackupStatus.COMPLETED;
    }
}
