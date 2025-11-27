package com.chibao.dbbackup_cli.adapter.in.rest;

import com.chibao.dbbackup_cli.adapter.in.rest.dto.RestoreRequestDto;
import com.chibao.dbbackup_cli.adapter.in.rest.dto.RestoreResponseDto;
import com.chibao.dbbackup_cli.adapter.in.rest.dto.RestoreStatusDto;
import com.chibao.dbbackup_cli.domain.port.in.RestoreUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Restore REST Controller
 * INBOUND ADAPTER - REST API for restore operations
 */
@RestController
@RequestMapping("/api/v1/restores")
@RequiredArgsConstructor
@Slf4j
class RestoreRestController {

    private final RestoreUseCase restoreUseCase;

    /**
     * Restore database from backup
     * POST /api/v1/restores
     * Example request:
     * {
     *   "backupId": "abc-123",
     *   "targetHost": "localhost",
     *   "targetPort": 5432,
     *   "targetDatabase": "mydb_restored",
     *   "username": "postgres",
     *   "password": "secret",
     *   "skipIfExists": false
     * }
     */
    @PostMapping
    public ResponseEntity<RestoreResponseDto> restore(
            @Valid @RequestBody RestoreRequestDto request
    ) {
        log.info("REST API: Restore request received - backupId={}, targetDb={}",
                request.getBackupId(), request.getTargetDatabase());

        try {
            // Convert DTO → Domain Command
            RestoreUseCase.RestoreCommand command = RestoreUseCase.RestoreCommand.builder()
                    .backupId(request.getBackupId())
                    .targetHost(request.getTargetHost())
                    .targetPort(request.getTargetPort())
                    .targetDatabase(request.getTargetDatabase())
                    .username(request.getUsername())
                    .password(request.getPassword())
                    .skipIfExists(request.isSkipIfExists())
                    .tables(request.getTables())
                    .build();

            // Execute use case
            RestoreUseCase.RestoreResult result = restoreUseCase.execute(command);

            // Convert to DTO
            RestoreResponseDto response = RestoreResponseDto.builder()
                    .backupId(result.getBackupId())
                    .success(result.isSuccess())
                    .message(result.getMessage())
                    .durationMs(result.getDurationMs())
                    .build();

            HttpStatus status = result.isSuccess() ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;

            log.info("REST API: Restore {} - backupId={}",
                    result.isSuccess() ? "completed" : "failed", result.getBackupId());

            return ResponseEntity.status(status).body(response);

        } catch (Exception e) {
            log.error("REST API: Restore error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(RestoreResponseDto.error(request.getBackupId(), e.getMessage()));
        }
    }

    /**
     * Get restore status
     * GET /api/v1/restores/{restoreId}
     */
    @GetMapping("/{restoreId}")
    public ResponseEntity<RestoreStatusDto> getRestoreStatus(@PathVariable String restoreId) {
        log.info("REST API: Get restore status - restoreId={}", restoreId);

        // TODO: Implement async restore tracking

        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}