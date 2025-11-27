package com.chibao.dbbackup_cli.domain.port.out;

import com.chibao.dbbackup_cli.domain.model.Backup;
import java.util.List;
import java.util.Optional;

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

    /**
     * Retrieves all backup records.
     *
     * @return A list of all backups.
     */
    List<Backup> findAll();

    /**
     * Finds a backup record by its ID.
     * @param backupId The ID of the backup.
     * @return An Optional containing the backup if found.
     */
    Optional<Backup> findById(String backupId);
}
