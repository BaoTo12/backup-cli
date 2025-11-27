package com.chibao.dbbackup_cli.domain.port.in;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * INBOUND PORT: List Backups Use Case
 */
public interface ListBackupsUseCase {

    List<BackupInfo> execute(ListBackupsQuery query);

    @Value
    @Builder
    class ListBackupsQuery {
        String databaseName;
        String databaseType;
        Integer limit;

        @Builder.Default
        boolean includeExpired = false;
    }

    @Value
    @Builder
    class BackupInfo {
        String id;
        String databaseName;
        String databaseType;
        String status;
        long sizeBytes;
        String createdAt;
        String storageLocation;
    }
}
