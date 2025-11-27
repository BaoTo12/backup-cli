package com.chibao.dbbackup_cli.domain.port.in;

import com.chibao.dbbackup_cli.domain.port.in.dto.RestoreCommand;
import com.chibao.dbbackup_cli.domain.port.in.dto.RestoreResult;

public interface RestoreUseCase {
    RestoreResult execute(RestoreCommand command);
}
