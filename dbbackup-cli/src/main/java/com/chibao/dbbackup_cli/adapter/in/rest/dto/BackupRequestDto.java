package com.chibao.dbbackup_cli.adapter.in.rest.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackupRequestDto {

    @NotBlank(message = "Database type is required")
    @Pattern(regexp = "postgres|mysql|mongodb", message = "Database type must be: postgres, mysql, or mongodb")
    private String databaseType;

    @NotBlank(message = "Host is required")
    private String host;

    @Min(value = 1, message = "Port must be between 1 and 65535")
    @Max(value = 65535, message = "Port must be between 1 and 65535")
    private int port;

    @NotBlank(message = "Database name is required")
    private String database;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    @Builder.Default
    @Pattern(regexp = "NONE|GZIP|ZIP", message = "Compression must be: NONE, GZIP, or ZIP")
    private String compression = "GZIP";

    @Builder.Default
    private boolean encrypt = false;

    @Builder.Default
    @Pattern(regexp = "local|s3|minio", message = "Storage provider must be: local, s3, or minio")
    private String storageProvider = "local";

    private List<String> tables;  // Optional: selective backup

    @Builder.Default
    private Map<String, String> additionalOptions = Map.of();
}
