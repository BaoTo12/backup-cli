package com.chibao.dbbackup_cli.domain.repository;

import com.chibao.dbbackup_cli.domain.model.Backup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface BackupRepository extends JpaRepository<Backup, String> {
}
