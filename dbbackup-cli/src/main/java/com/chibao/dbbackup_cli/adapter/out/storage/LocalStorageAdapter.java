package com.chibao.dbbackup_cli.adapter.out.storage;

import com.chibao.dbbackup_cli.domain.exception.StorageException;
import com.chibao.dbbackup_cli.domain.port.out.StoragePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Local File System Storage Adapter
 *
 * OUTBOUND ADAPTER - implements StoragePort
 *
 * Stores backups on local file system.
 * Useful for development and testing.
 */
@Component
@ConditionalOnProperty(name = "storage.provider", havingValue = "local", matchIfMissing = true)
@Slf4j
class LocalStorageAdapter implements StoragePort {

    @Value("${storage.local.path:/var/lib/dbbackup}")
    private String basePath;

    @Override
    public String upload(UploadRequest request) {
        try {
            Path baseDir = Paths.get(basePath);

            // Create directory if not exists
            if (!Files.exists(baseDir)) {
                Files.createDirectories(baseDir);
            }

            // Build file path
            Path targetFile = baseDir.resolve(request.getFilename());

            log.info("Saving to local storage: path={}, size={} bytes",
                    targetFile, request.getSizeBytes());

            // Copy input stream to file
            Files.copy(
                    request.getData(),
                    targetFile,
                    StandardCopyOption.REPLACE_EXISTING
            );

            log.info("Local storage upload completed: path={}", targetFile);

            return targetFile.toString();

        } catch (IOException e) {
            log.error("Local storage upload failed: filename={}", request.getFilename(), e);
            throw new StorageException("Local storage upload failed", e);
        }
    }

    @Override
    public InputStream download(String identifier) {
        try {
            Path file = Paths.get(identifier);

            if (!Files.exists(file)) {
                throw new StorageException("File not found: " + identifier);
            }

            log.info("Reading from local storage: path={}", identifier);

            return Files.newInputStream(file);

        } catch (IOException e) {
            log.error("Local storage download failed: path={}", identifier, e);
            throw new StorageException("Local storage download failed", e);
        }
    }

    @Override
    public void delete(String identifier) {
        try {
            Path file = Paths.get(identifier);

            if (Files.exists(file)) {
                Files.delete(file);
                log.info("Deleted from local storage: path={}", identifier);
            } else {
                log.warn("File not found for deletion: path={}", identifier);
            }

        } catch (IOException e) {
            log.error("Local storage delete failed: path={}", identifier, e);
            throw new StorageException("Local storage delete failed", e);
        }
    }

    @Override
    public boolean exists(String identifier) {
        Path file = Paths.get(identifier);
        return Files.exists(file);
    }

    @Override
    public String getProviderType() {
        return "local";
    }
}