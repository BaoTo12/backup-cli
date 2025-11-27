package com.chibao.dbbackup_cli.adapter.in.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestoreStatusDto {
    private String restoreId;
    private String status;  // PENDING, IN_PROGRESS, COMPLETED, FAILED
    private Integer progressPercent;
    private String message;
}