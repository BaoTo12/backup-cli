package com.chibao.dbbackup_cli.domain.port.in.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class RestoreCommand {
    String targetHost;
    int targetPort;
    String targetDatabase;
    String username;
    String password;

    @Builder.Default
    boolean skipIfExists = false;

    List<String> tables;  // For selective restore
}
