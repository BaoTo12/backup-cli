package com.chibao.dbbackup_cli.config;

import com.chibao.dbbackup_cli.domain.port.in.BackupUseCase;
import com.chibao.dbbackup_cli.domain.port.out.*;
import com.chibao.dbbackup_cli.domain.service.BackupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Backup Service Wrapper
 *
 * This service uses factories to dynamically select adapters
 * based on command parameters.
 *
 * Original BackupService depends on single adapter instances.
 * This wrapper allows runtime selection of adapters.
 */
@Service
@RequiredArgsConstructor
@Slf4j
class DynamicBackupService implements BackupUseCase {

    private final DatabaseDumpFactory databaseDumpFactory;
    private final StorageFactory storageFactory;
    private final ChecksumPort checksumPort;
    private final EncryptionPort encryptionPort;
    private final MetadataPort metadataPort;
    private final MetricsPort metricsPort;

    @Override
    public BackupResult execute(BackupCommand command) {

        // Get adapters dynamically based on command
        DatabaseDumpPort databaseDumpPort = databaseDumpFactory.getAdapter(command.getDatabaseType());
        StoragePort storagePort = storageFactory.getAdapter(command.getStorageProvider());

        log.info("Using database adapter: {} for type: {}",
                databaseDumpPort.getClass().getSimpleName(),
                command.getDatabaseType());

        log.info("Using storage adapter: {} for provider: {}",
                storagePort.getClass().getSimpleName(),
                command.getStorageProvider());

        // Create BackupService instance with selected adapters
        BackupService backupService = new BackupService(
                databaseDumpPort,
                storagePort,
                checksumPort,
                encryptionPort,
                metadataPort,
                metricsPort
        );

        // Delegate to actual service
        return backupService.execute(command);
    }
}
