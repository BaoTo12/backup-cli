package com.chibao.dbbackup_cli.adapter.in.cli;

import com.chibao.dbbackup_cli.domain.port.in.RestoreUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
@RequiredArgsConstructor
public class RestoreCliAdapter {

    private final RestoreUseCase restoreUseCase;

    @ShellMethod(value = "Restore a database from a backup.", key = "restore")
    public String restore(
            @ShellOption(help = "The ID of the backup to restore.") String backupId,
            @ShellOption(help = "Target database host.") String targetHost,
            @ShellOption(help = "Target database port.") int targetPort,
            @ShellOption(help = "Target database name.") String targetDatabase,
            @ShellOption(help = "Username for target database connection.") String username,
            @ShellOption(help = "Password for target database connection.") String password
    ) {
        RestoreUseCase.RestoreCommand command = RestoreUseCase.RestoreCommand.builder()
                .backupId(backupId)
                .targetHost(targetHost)
                .targetPort(targetPort)
                .targetDatabase(targetDatabase)
                .username(username)
                .password(password)
                .build();

        RestoreUseCase.RestoreResult result = restoreUseCase.execute(command);

        if (result.isSuccess()) {
            return String.format("✅ %s (took %dms)", result.getMessage(), result.getDurationMs());
        } else {
            return String.format("❌ %s (took %dms)", result.getMessage(), result.getDurationMs());
        }
    }
}