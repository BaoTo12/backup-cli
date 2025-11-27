package com.chibao.dbbackup_cli.adapter.in.cli;

import com.chibao.dbbackup_cli.domain.port.in.RestoreUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;

@ShellComponent
@RequiredArgsConstructor
class RestoreCliAdapter {

    private final RestoreUseCase restoreUseCase;

    // Additional restore-specific commands
    // e.g., list-restorable-backups, verify-backup, etc.
}
