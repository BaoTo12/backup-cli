package com.chibao.dbbackup_cli.adapter.in.rest.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestoreRequestDto {

    @NotBlank(message = "Backup ID is required")
    private String backupId;

    @NotBlank(message = "Target host is required")
    private String targetHost;

    @Min(value = 1)
    @Max(value = 65535)
    private int targetPort;

    @NotBlank(message = "Target database name is required")
    private String targetDatabase;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    @Builder.Default
    private boolean skipIfExists = false;

    private List<String> tables;  // Optional: selective restore
}
