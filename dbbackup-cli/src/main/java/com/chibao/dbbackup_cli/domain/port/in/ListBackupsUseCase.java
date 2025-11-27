package com.chibao.dbbackup_cli.domain.port.in;

import com.chibao.dbbackup_cli.domain.port.in.dto.BackupInfo;
import com.chibao.dbbackup_cli.domain.port.in.dto.ListBackupsQuery;

import java.util.List;

public interface ListBackupsUseCase {
    List<BackupInfo> execute(ListBackupsQuery query);
}
