package com.chibao.dbbackup_cli.adapter.in.rest;


import com.chibao.dbbackup_cli.domain.port.in.BackupUseCase;
import com.chibao.dbbackup_cli.domain.port.in.ListBackupsUseCase;
import com.chibao.dbbackup_cli.domain.port.in.TestConnectionUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Collectors;

/**
 * Backup REST Controller
 *
 * INBOUND ADAPTER - REST API for backup operations
 *
 * Provides HTTP endpoints for:
 * - Creating backups
 * - Listing backups
 * - Testing database connections
 *
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

    /**
     * Create a new backup
     *
     * POST /api/v1/backups
     *
     * Example request:
     * {
     *   "databaseType": "postgres",
     *   "host": "localhost",
     *   "port": 5432,
     *   "database": "mydb",
     *   "username": "postgres",
     *   "password": "secret",
     *   "compression": "GZIP",
     *   "encrypt": false,
     *   "storageProvider": "s3"
     * }
     */
    @PostMapping
    public ResponseEntity<BackupResponseDto> createBackup(
            @Valid @RequestBody BackupRequestDto request
    ) {
        log.info("REST API: Create backup request received for database: {}", request.getDatabase());

        try {
            // Convert REST DTO → Domain Command
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

            // Execute use case
            BackupUseCase.BackupResult result = backupUseCase.execute(command);

            // Convert Domain Result → REST DTO
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

    /**
     * List all backups
     *
     * GET /api/v1/backups?database=mydb&dbType=postgres&limit=10
     */
    @GetMapping
    public ResponseEntity<List<BackupInfoDto>> listBackups(
            @RequestParam(required = false) String database,
            @RequestParam(required = false) String dbType,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "false") boolean includeExpired
    ) {
        log.info("REST API: List backups request - database={}, dbType={}, limit={}",
                database, dbType, limit);

        try {
            // Create query
            ListBackupsUseCase.ListBackupsQuery query = ListBackupsUseCase.ListBackupsQuery.builder()
                    .databaseName(database)
                    .databaseType(dbType)
                    .limit(limit)
                    .includeExpired(includeExpired)
                    .build();

            // Execute use case
            List<ListBackupsUseCase.BackupInfo> backups = listBackupsUseCase.execute(query);

            // Convert to DTOs
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

    /**
     * Get backup by ID
     *
     * GET /api/v1/backups/{backupId}
     */
    @GetMapping("/{backupId}")
    public ResponseEntity<BackupInfoDto> getBackup(@PathVariable String backupId) {
        log.info("REST API: Get backup request - backupId={}", backupId);

        // TODO: Implement GetBackupByIdUseCase

        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .build();
    }

    /**
     * Delete backup
     *
     * DELETE /api/v1/backups/{backupId}
     */
    @DeleteMapping("/{backupId}")
    public ResponseEntity<Void> deleteBackup(@PathVariable String backupId) {
        log.info("REST API: Delete backup request - backupId={}", backupId);

        // TODO: Implement DeleteBackupUseCase

        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /**
     * Test database connection
     *
     * POST /api/v1/backups/test-connection
     */
    @PostMapping("/test-connection")
    public ResponseEntity<ConnectionTestResponseDto> testConnection(
            @Valid @RequestBody ConnectionTestRequestDto request
    ) {
        log.info("REST API: Test connection request - database={}", request.getDatabase());

        try {
            // Convert DTO → Domain Command
            TestConnectionUseCase.ConnectionTestCommand command =
                    TestConnectionUseCase.ConnectionTestCommand.builder()
                            .databaseType(request.getDatabaseType())
                            .host(request.getHost())
                            .port(request.getPort())
                            .database(request.getDatabase())
                            .username(request.getUsername())
                            .password(request.getPassword())
                            .build();

            // Execute use case
            TestConnectionUseCase.ConnectionTestResult result = testConnectionUseCase.execute(command);

            // Convert to DTO
            ConnectionTestResponseDto response = ConnectionTestResponseDto.builder()
                    .success(result.isSuccess())
                    .message(result.getMessage())
                    .responseTimeMs(result.getResponseTimeMs())
                    .databaseVersion(result.getDatabaseVersion())
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

    private BackupInfoDto mapToBackupInfoDto(ListBackupsUseCase.BackupInfo info) {
        return BackupInfoDto.builder()
                .id(info.getId())
                .databaseName(info.getDatabaseName())
                .databaseType(info.getDatabaseType())
                .status(info.getStatus())
                .sizeBytes(info.getSizeBytes())
                .createdAt(info.getCreatedAt())
                .storageLocation(info.getStorageLocation())
                .build();
    }
}