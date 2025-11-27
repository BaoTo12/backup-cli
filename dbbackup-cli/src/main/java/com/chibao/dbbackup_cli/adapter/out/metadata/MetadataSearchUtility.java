package com.chibao.dbbackup_cli.adapter.out.metadata;

import com.chibao.dbbackup_cli.domain.port.out.MetadataPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Metadata Search Utility
 * Search and query metadata files
 */
@Component
@Slf4j
class MetadataSearchUtility {

    private final JsonMetadataAdapter metadataAdapter;

    public MetadataSearchUtility(JsonMetadataAdapter metadataAdapter) {
        this.metadataAdapter = metadataAdapter;
    }

    /**
     * Find all metadata files in directory
     */
    public java.util.List<MetadataPort.BackupMetadataDto> findAllInDirectory(Path directory) {
        java.util.List<MetadataPort.BackupMetadataDto> results = new java.util.ArrayList<>();

        try (java.util.stream.Stream<Path> paths = Files.walk(directory)) {
            paths.filter(path -> path.toString().endsWith(".meta.json"))
                    .forEach(path -> {
                        try {
                            MetadataPort.BackupMetadataDto metadata = metadataAdapter.load(path);
                            results.add(metadata);
                        } catch (Exception e) {
                            log.warn("Failed to load metadata: {}", path, e);
                        }
                    });
        } catch (IOException e) {
            log.error("Failed to search metadata files in directory: {}", directory, e);
        }

        return results;
    }

    /**
     * Find metadata by database name
     */
    public java.util.List<MetadataPort.BackupMetadataDto> findByDatabaseName(
            Path directory,
            String databaseName
    ) {
        return findAllInDirectory(directory).stream()
                .filter(metadata -> databaseName.equals(metadata.getDbName()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Find metadata by status
     */
    public java.util.List<MetadataPort.BackupMetadataDto> findByStatus(
            Path directory,
            String status
    ) {
        return findAllInDirectory(directory).stream()
                .filter(metadata -> status.equalsIgnoreCase(metadata.getStatus()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Find latest metadata for a database
     */
    public java.util.Optional<MetadataPort.BackupMetadataDto> findLatest(
            Path directory,
            String databaseName
    ) {
        return findByDatabaseName(directory, databaseName).stream()
                .filter(metadata -> "COMPLETED".equalsIgnoreCase(metadata.getStatus()))
                .max(java.util.Comparator.comparing(MetadataPort.BackupMetadataDto::getEndTime));
    }
}