package com.chibao.dbbackup_cli.adapter.out.dbdump;

import com.chibao.dbbackup_cli.domain.model.DatabaseConfig;
import com.chibao.dbbackup_cli.domain.port.out.DatabaseDumpPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component("mysqlDump")
@Slf4j
class MySQLDumpAdapter implements DatabaseDumpPort {

    @Override
    public DumpOutput performDump(DumpConfig config) {
        // Similar to PostgreSQL but using mysqldump
        log.info("Starting MySQL dump: database={}", config.getDatabase());

        // mysqldump -h host -P port -u user -p password database > dump.sql
        // Use --single-transaction for InnoDB consistency
        // Use --routines --events to include stored procedures and events

        throw new UnsupportedOperationException("MySQL dump not yet implemented");
    }

    @Override
    public void performRestore(RestoreInput input) {
        // mysql -h host -P port -u user -p password database < dump.sql
        throw new UnsupportedOperationException("MySQL restore not yet implemented");
    }

    @Override
    public boolean testConnection(DatabaseConfig config) {
        String url = String.format(
                "jdbc:mysql://%s:%d/%s",
                config.getHost(),
                config.getPort(),
                config.getDatabase()
        );

        try (Connection conn = DriverManager.getConnection(
                url,
                config.getUsername(),
                config.getPassword()
        )) {
            return conn.isValid(5);
        } catch (SQLException e) {
            log.warn("MySQL connection failed: {}", url, e);
            return false;
        }
    }

    @Override
    public String getSupportedDatabaseType() {
        return "mysql";
    }
}
