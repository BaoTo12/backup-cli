package com.chibao.dbbackup_cli.adapter.in.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestoreResponseDto {

    private String backupId;
    private boolean success;
    private String message;
    private Long durationMs;

    public static RestoreResponseDto error(String backupId, String message) {
        return RestoreResponseDto.builder()
                .backupId(backupId)
                .success(false)
                .message(message)
                .durationMs(0L)
                .build();
    }
}