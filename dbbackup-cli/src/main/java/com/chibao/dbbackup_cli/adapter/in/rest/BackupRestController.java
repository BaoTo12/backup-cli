package com.chibao.dbbackup_cli.adapter.in.rest;

import com.chibao.dbbackup_cli.adapter.in.rest.dto.BackupInfoDto;
import com.chibao.dbbackup_cli.adapter.in.rest.dto.BackupRequestDto;
import com.chibao.dbbackup_cli.adapter.in.rest.dto.BackupResponseDto;
import com.chibao.dbbackup_cli.adapter.in.rest.dto.ConnectionTestRequestDto;
import com.chibao.dbbackup_cli.adapter.in.rest.dto.ConnectionTestResponseDto;
import com.chibao.dbbackup_cli.domain.model.Backup;
import com.chibao.dbbackup_cli.domain.model.CompressionType;
import com.chibao.dbbackup_cli.domain.port.in.BackupUseCase;
import com.chibao.dbbackup_cli.domain.port.in.ListBackupsUseCase;
import com.chibao.dbbackup_cli.domain.port.in.TestConnectionUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Backup REST Controller
 * INBOUND ADAPTER - REST API for backup operations
 * Provides HTTP endpoints for:
 * - Creating backups
 * - Listing backups
 * - Testing database connections
 * Depends on INBOUND PORTS (use cases).
 */
@RestController
@RequestMapping("/api/v1/backups")
@RequiredArgsConstructor
@Slf4j
public class BackupRestController {

    private final BackupUseCase backupUseCase;
    private final ListBackupsUseCase listBackupsUseCase;
    private final TestConnectionUseCase testConnectionUseCase;

    @PostMapping
    public ResponseEntity<BackupResponseDto> createBackup(
            @Valid @RequestBody BackupRequestDto request
    ) {
        log.info("REST API: Create backup request received for database: {}", request.getDatabase());

        try {
            BackupUseCase.BackupCommand command = BackupUseCase.BackupCommand.builder()
                    .databaseType(request.getDatabaseType())
                    .host(request.getHost())
                    .port(request.getPort())
                    .database(request.getDatabase())
                    .username(request.getUsername())
                    .password(request.getPassword())
                    .compression(CompressionType.valueOf(request.getCompression().toUpperCase()))
                    .encrypt(request.isEncrypt())
                    .storageProvider(request.getStorageProvider())
                    .tables(request.getTables())
                    .additionalOptions(request.getAdditionalOptions())
                    .build();

            BackupUseCase.BackupResult result = backupUseCase.execute(command);
            BackupResponseDto response = mapToResponseDto(result);

            if (result.isSuccess()) {
                log.info("REST API: Backup created successfully: backupId={}", result.getBackupId());
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                log.error("REST API: Backup failed: backupId={}, error={}",
                        result.getBackupId(), result.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (IllegalArgumentException e) {
            log.error("REST API: Invalid request parameters", e);
            return ResponseEntity.badRequest()
                    .body(BackupResponseDto.error(null, "Invalid request: " + e.getMessage()));
        } catch (Exception e) {
            log.error("REST API: Unexpected error during backup", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BackupResponseDto.error(null, "Internal server error: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<BackupInfoDto>> listBackups() {
        log.info("REST API: List backups request");

        try {
            // 1. Call use case to get domain objects
            List<Backup> backups = listBackupsUseCase.getAllBackups();

            // 2. Adapter maps domain objects to DTOs
            List<BackupInfoDto> response = backups.stream()
                    .map(this::mapToBackupInfoDto)
                    .collect(Collectors.toList());

            log.info("REST API: Found {} backups", response.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("REST API: Error listing backups", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/test-connection")
    public ResponseEntity<ConnectionTestResponseDto> testConnection(
            @Valid @RequestBody ConnectionTestRequestDto request
    ) {
        log.info("REST API: Test connection request - database={}", request.getDatabase());

        try {
            // Adapter maps REST DTO to Domain Command
            TestConnectionUseCase.TestConnectionCommand command =
                    TestConnectionUseCase.TestConnectionCommand.builder()
                            .databaseType(request.getDatabaseType())
                            .host(request.getHost())
                            .port(request.getPort())
                            .database(request.getDatabase())
                            .username(request.getUsername())
                            .password(request.getPassword())
                            .build();

            TestConnectionUseCase.TestConnectionResult result = testConnectionUseCase.testConnection(command);

            ConnectionTestResponseDto response = ConnectionTestResponseDto.builder()
                    .success(result.isSuccess())
                    .message(result.getMessage())
                    .responseTimeMs(result.getDurationMs())
                    .build();

            HttpStatus status = result.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(response);

        } catch (Exception e) {
            log.error("REST API: Error testing connection", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ConnectionTestResponseDto.error("Connection test failed: " + e.getMessage()));
        }
    }

    // ===== PRIVATE HELPER METHODS =====

    private BackupResponseDto mapToResponseDto(BackupUseCase.BackupResult result) {
        BackupResponseDto.BackupResponseDtoBuilder builder = BackupResponseDto.builder()
                .backupId(result.getBackupId())
                .success(result.isSuccess())
                .message(result.getMessage());

        if (result.getMetadata() != null) {
            builder.storageLocation(result.getMetadata().getStorageLocation())
                    .sizeBytes(result.getMetadata().getSizeBytes())
                    .checksum(result.getMetadata().getChecksum())
                    .durationMs(result.getMetadata().getDurationMs());
        }
        return builder.build();
    }

    private BackupInfoDto mapToBackupInfoDto(Backup backup) {
        return BackupInfoDto.builder()
                .id(backup.getId())
                .databaseName(backup.getDatabaseName())
                .databaseType(backup.getDatabaseType())
                .status(backup.getStatus())
                .sizeBytes(backup.getSizeBytes())
                .createdAt(backup.getCreatedAt())
                .storageLocation(backup.getStorageLocation())
                .build();
    }
}