package com.chibao.dbbackup_cli.adapter.in.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackupInfoDto {
    private String id;
    private String databaseName;
    private String databaseType;
    private String status;
    private Long sizeBytes;
    private String createdAt;
    private String storageLocation;
}

