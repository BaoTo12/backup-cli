package com.chibao.dbbackup_cli.domain.port.in;

import com.chibao.dbbackup_cli.domain.model.Backup;
import java.util.List;

/**
 * Inbound Port for listing historical backups.
 */
public interface ListBackupsUseCase {

    /**
     * Retrieves a list of all backup records.
     * @return A list of Backup objects.
     */
    List<Backup> getAllBackups();
}
