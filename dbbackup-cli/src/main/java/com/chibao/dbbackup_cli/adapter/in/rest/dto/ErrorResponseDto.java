package com.chibao.dbbackup_cli.adapter.in.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDto {

    private String timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    public static ErrorResponseDto of(
            int status,
            String error,
            String message,
            String path
    ) {
        return ErrorResponseDto.builder()
                .timestamp(java.time.Instant.now().toString())
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .build();
    }
}