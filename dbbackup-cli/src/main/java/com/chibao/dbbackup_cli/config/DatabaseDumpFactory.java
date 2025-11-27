package com.chibao.dbbackup_cli.config;

import com.chibao.dbbackup_cli.domain.port.out.DatabaseDumpPort;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Database Dump Factory
 *
 * Automatically discovers all DatabaseDumpPort implementations
 * and provides factory method to get adapter by database type.
 *
 * This enables EASY EXTENSIBILITY:
 * - Add new database → Create new adapter with @Component
 * - Spring auto-discovers it
 * - Factory automatically includes it
 * - ZERO changes to existing code!
 */
@Component
@RequiredArgsConstructor
@Slf4j
class DatabaseDumpFactory {

    // Spring automatically injects ALL beans implementing DatabaseDumpPort
    private final List<DatabaseDumpPort> dumpAdapters;

    // Map: database type → adapter
    private Map<String, DatabaseDumpPort> adapterMap;

    @PostConstruct
    public void init() {
        // Build map for fast lookup
        adapterMap = dumpAdapters.stream()
                .collect(Collectors.toMap(
                        DatabaseDumpPort::getSupportedDatabaseType,
                        Function.identity()
                ));

        log.info("Registered database dump adapters: {}", adapterMap.keySet());
    }

    /**
     * Get adapter for database type
     *
     * @param databaseType postgres, mysql, mongodb, etc.
     * @return adapter implementation
     * @throws UnsupportedDatabaseException if type not supported
     */
    public DatabaseDumpPort getAdapter(String databaseType) {
        DatabaseDumpPort adapter = adapterMap.get(databaseType.toLowerCase());

        if (adapter == null) {
            String supportedTypes = String.join(", ", adapterMap.keySet());
            throw new UnsupportedDatabaseException(
                    String.format(
                            "Database type '%s' is not supported. Supported types: %s",
                            databaseType,
                            supportedTypes
                    )
            );
        }

        return adapter;
    }

    /**
     * Get all supported database types
     */
    public List<String> getSupportedTypes() {
        return List.copyOf(adapterMap.keySet());
    }

    /**
     * Check if database type is supported
     */
    public boolean supports(String databaseType) {
        return adapterMap.containsKey(databaseType.toLowerCase());
    }
}
