package com.chibao.dbbackup_cli.domain.port.in.dto;

import com.chibao.dbbackup_cli.domain.model.CompressionType;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Map;

@Value
@Builder
public class BackupCommand {
    String databaseType;      // postgres, mysql, mongodb
    String host;
    int port;
    String database;
    String username;
    String password;

    @Builder.Default
    CompressionType compression = CompressionType.GZIP;

    @Builder.Default
    boolean encrypt = false;

    String storageProvider;   // s3, local, minio

    List<String> tables;      // For selective backup (optional)

    @Builder.Default
    Map<String, String> additionalOptions = Map.of();
}