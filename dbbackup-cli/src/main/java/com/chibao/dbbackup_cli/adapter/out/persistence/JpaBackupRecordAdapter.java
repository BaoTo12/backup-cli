package com.chibao.dbbackup_cli.adapter.out.persistence;

import com.chibao.dbbackup_cli.domain.model.Backup;
import com.chibao.dbbackup_cli.domain.port.out.BackupRecordPort;
import com.chibao.dbbackup_cli.domain.repository.BackupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Outbound adapter that implements the BackupRecordPort using Spring Data JPA.
 * This class is the bridge between the domain logic and the database.
 */
@Component
@RequiredArgsConstructor
public class JpaBackupRecordAdapter implements BackupRecordPort {

    private final BackupRepository backupRepository;

    @Override
    public Backup save(Backup backup) {
        return backupRepository.save(backup);
    }
}
