package com.chibao.dbbackup_cli.domain.port.out;

import com.chibao.dbbackup_cli.domain.model.Backup;

/**
 * Outbound port for persisting Backup entity records.
 * This abstracts the database storage mechanism from the core domain logic.
 */
public interface BackupRecordPort {

    /**
     * Saves or updates a backup record in the persistent store.
     *
     * @param backup The Backup entity to save.
     * @return The saved Backup entity.
     */
    Backup save(Backup backup);
}
