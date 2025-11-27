package com.chibao.dbbackup_cli.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class BackupMetadata {
    String filename;
    String version;          // CLI version
    String dbType;
    String dbHost;
    int dbPort;
    String dbName;
    Instant startTime;
    Instant endTime;
    long durationMs;
    long sizeBytes;
    String sha256;
    String compressAlgo;
    String encryptAlgo;
    Integer chunks;          // For multipart uploads
    String status;
}
