package com.chibao.dbbackup_cli.domain.model;

import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder
public class DatabaseConfig {
    String type;             // postgres, mysql, mongodb
    String host;
    int port;
    String database;
    String username;
    String password;
    Map<String, String> additionalOptions;

    public String getJdbcUrl() {
        return switch (type.toLowerCase()) {
            case "postgres" -> String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
            case "mysql" -> String.format("jdbc:mysql://%s:%d/%s", host, port, database);
            default -> throw new IllegalArgumentException("Unsupported database type: " + type);
        };
    }
}
