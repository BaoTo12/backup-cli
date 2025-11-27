package com.chibao.dbbackup_cli.domain.port.in.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BackupInfo {
    String id;
    String databaseName;
    String databaseType;
    String status;
    long sizeBytes;
    String createdAt;
    String storageLocation;
}
