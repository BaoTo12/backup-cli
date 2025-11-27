package com.chibao.dbbackup_cli.domain.service;

import com.chibao.dbbackup_cli.domain.model.Backup;
import com.chibao.dbbackup_cli.domain.port.in.ListBackupsUseCase;
import com.chibao.dbbackup_cli.domain.port.out.BackupRecordPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListBackupsService implements ListBackupsUseCase {

    private final BackupRecordPort backupRecordPort;

    @Override
    public List<Backup> getAllBackups() {
        return backupRecordPort.findAll();
    }
}
