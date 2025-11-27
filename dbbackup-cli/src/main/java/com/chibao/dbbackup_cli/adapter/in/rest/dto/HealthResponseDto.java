package com.chibao.dbbackup_cli.adapter.in.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthResponseDto {
    private String status;  // UP, DOWN
    private String timestamp;
    private String version;
}
