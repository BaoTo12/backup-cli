package com.chibao.dbbackup_cli.adapter.in.rest.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionTestRequestDto {

    @NotBlank(message = "Database type is required")
    private String databaseType;

    @NotBlank(message = "Host is required")
    private String host;

    @Min(1)
    @Max(65535)
    private int port;

    @NotBlank(message = "Database name is required")
    private String database;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}

