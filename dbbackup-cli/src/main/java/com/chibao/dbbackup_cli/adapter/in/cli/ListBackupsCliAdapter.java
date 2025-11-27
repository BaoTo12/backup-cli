package com.chibao.dbbackup_cli.adapter.in.cli;

import com.chibao.dbbackup_cli.adapter.in.rest.dto.BackupInfo;
import com.chibao.dbbackup_cli.domain.port.in.ListBackupsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.table.ArrayTableModel;
import org.springframework.shell.table.BorderStyle;
import org.springframework.shell.table.TableBuilder;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@ShellComponent
@RequiredArgsConstructor
public class ListBackupsCliAdapter {

    private final ListBackupsUseCase listBackupsUseCase;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    @ShellMethod(value = "List all historical backups.", key = "list-backups")
    public String listBackups() {
        // 1. Call the use case, which returns domain objects
        List<com.chibao.dbbackup_cli.domain.model.Backup> backupEntities = listBackupsUseCase.getAllBackups();

        if (backupEntities.isEmpty()) {
            return "No backup records found.";
        }

        // 2. The adapter is responsible for mapping domain objects to DTOs for presentation
        List<BackupInfo> backups = backupEntities.stream()
                .map(BackupInfo::from)
                .collect(java.util.stream.Collectors.toList());

        // 3. Build the table from the DTOs
        String[][] data = new String[backups.size() + 1][7];
        data[0] = new String[]{"ID", "DB Name", "DB Type", "Status", "Created At", "Size (MB)", "Storage Location"};

        for (int i = 0; i < backups.size(); i++) {
            BackupInfo b = backups.get(i);
            data[i + 1] = new String[]{
                    b.getId(),
                    b.getDatabaseName(),
                    b.getDatabaseType(),
                    b.getStatus().toString(),
                    FORMATTER.format(b.getCreatedAt()),
                    b.getSizeBytes() != null ? String.format("%.2f", b.getSizeBytes() / 1024.0 / 1024.0) : "N/A",
                    b.getStorageLocation() != null ? b.getStorageLocation() : "N/A"
            };
        }

        ArrayTableModel tableModel = new ArrayTableModel(data);
        TableBuilder tableBuilder = new TableBuilder(tableModel);
        tableBuilder.addFullBorder(BorderStyle.fancy_light);

        return tableBuilder.build().render(120);
    }
}
