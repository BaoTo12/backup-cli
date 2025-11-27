package com.chibao.dbbackup_cli.adapter.in.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionTestResponseDto {

    private boolean success;
    private String message;
    private Long responseTimeMs;
    private String databaseVersion;

    public static ConnectionTestResponseDto error(String message) {
        return ConnectionTestResponseDto.builder()
                .success(false)
                .message(message)
                .responseTimeMs(-1L)
                .build();
    }
}