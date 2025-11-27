package com.chibao.dbbackup_cli.adapter.in.rest.dto;

import com.chibao.dbbackup_cli.domain.model.BackupStatus;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/**
 * DTO for representing backup information in REST API responses.
 */
@Value
@Builder
public class BackupInfoDto {
    String id;
    String databaseName;
    String databaseType;
    BackupStatus status;
    Long sizeBytes;
    Instant createdAt;
    String storageLocation;
}