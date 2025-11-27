package com.chibao.dbbackup_cli.domain.port.in.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ListBackupsQuery {
    String databaseName;
    String databaseType;
    Integer limit;

    @Builder.Default
    boolean includeExpired = false;
}
