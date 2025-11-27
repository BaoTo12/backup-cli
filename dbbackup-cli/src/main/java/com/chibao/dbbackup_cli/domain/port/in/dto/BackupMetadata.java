package com.chibao.dbbackup_cli.domain.port.in.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BackupMetadata {
    String storageLocation;
    long sizeBytes;
    String checksum;
    long durationMs;
}