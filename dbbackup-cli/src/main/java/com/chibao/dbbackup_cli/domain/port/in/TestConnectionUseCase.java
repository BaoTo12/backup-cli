package com.chibao.dbbackup_cli.domain.port.in;

import com.chibao.dbbackup_cli.domain.port.in.dto.ConnectionTestCommand;
import com.chibao.dbbackup_cli.domain.port.in.dto.ConnectionTestResult;

public interface TestConnectionUseCase {
    ConnectionTestResult execute(ConnectionTestCommand command);
}
