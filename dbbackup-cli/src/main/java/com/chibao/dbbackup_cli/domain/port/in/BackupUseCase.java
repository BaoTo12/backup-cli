package com.chibao.dbbackup_cli.domain.port.in;

import com.chibao.dbbackup_cli.domain.port.in.dto.BackupCommand;
import com.chibao.dbbackup_cli.domain.port.in.dto.BackupResult;

public interface BackupUseCase {
    BackupResult execute(BackupCommand command);
}
