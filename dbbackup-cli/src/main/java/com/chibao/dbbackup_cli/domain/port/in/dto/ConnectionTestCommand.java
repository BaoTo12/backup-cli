package com.chibao.dbbackup_cli.domain.port.in.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ConnectionTestCommand {
    String databaseType;
    String host;
    int port;
    String database;
    String username;
    String password;
}
