package com.chibao.dbbackup_cli.domain.model;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;


@Value
@Builder(toBuilder = true)
public class BackUp {
    String id;
    String databaseType;      // postgres, mysql, mongodb
    String databaseName;
    String host;
    int port;

    @With
    BackupStatus status;

    Instant createdAt;
    Instant completedAt;

    @With
    Long sizeBytes;

    @With
    String checksum;          // SHA-256

    CompressionType compression;
    boolean encrypted;

    String storageLocation;   // S3 key, local path, etc.

    @With
    Map<String, String> metadata;

    // ? BUSINESS LOGIC (Pure domain logic)

    // Check if backup is expired based on retention policy
    public boolean isExpired(int retentionDays) {
        if (createdAt == null) {
            return false;
        }
        Instant expirationDate = createdAt.plus(retentionDays, ChronoUnit.DAYS);
        return Instant.now().isAfter(expirationDate);
    }

    // Mark backup as completed with metadata
    public BackUp markAsCompleted(String checksum, long sizeBytes, String storageLocation) {
        return this.toBuilder()
                .status(BackupStatus.COMPLETED)
                .completedAt(Instant.now())
                .checksum(checksum)
                .sizeBytes(sizeBytes)
                .storageLocation(storageLocation)
                .build();
    }

    // Mark backup as failed
    public BackUp markAsFailed(String errorMessage) {
        return this.toBuilder()
                .status(BackupStatus.FAILED)
                .completedAt(Instant.now())
                .metadata(Map.of("error", errorMessage))
                .build();
    }

    public long getDurationSeconds() {
        if (createdAt == null || completedAt == null) {
            return 0;
        }
        return ChronoUnit.SECONDS.between(createdAt, completedAt);
    }

    // Check if backup is in progress
    public boolean isInProgress() {
        return status == BackupStatus.IN_PROGRESS;
    }

    // Check if backup is successful
    public boolean isSuccessful() {
        return status == BackupStatus.COMPLETED;
    }
}
