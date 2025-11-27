package com.chibao.dbbackup_cli.adapter.out.checksum;

import com.chibao.dbbackup_cli.domain.exception.StorageException;
import com.chibao.dbbackup_cli.domain.port.out.ChecksumPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * SHA-256 Checksum Adapter
 * OUTBOUND ADAPTER - implements ChecksumPort
 * Calculates SHA-256 checksums for file integrity verification.
 */
@Component
@Slf4j
public class Sha256ChecksumAdapter implements ChecksumPort {

    private static final String ALGORITHM = "SHA-256";
    private static final int BUFFER_SIZE = 8192; // 8KB buffer

    @Override
    public String calculate(Path filePath) {
        log.debug("Calculating SHA-256 checksum for file: {}", filePath);

        try (InputStream inputStream = Files.newInputStream(filePath)) {
            return calculate(inputStream);
        } catch (IOException e) {
            log.error("Failed to calculate checksum for file: {}", filePath, e);
            throw new StorageException("Checksum calculation failed", e);
        }
    }

    @Override
    public String calculate(InputStream inputStream) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;

            // Stream file through digest
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }

            // Get hash bytes
            byte[] hashBytes = digest.digest();

            // Convert to hex string
            String checksum = HexFormat.of().formatHex(hashBytes);

            log.debug("Calculated SHA-256 checksum: {}", checksum);

            return checksum;

        } catch (Exception e) {
            log.error("Failed to calculate checksum from stream", e);
            throw new StorageException("Checksum calculation failed", e);
        }
    }

    @Override
    public boolean verify(Path filePath, String expectedChecksum) {
        log.debug("Verifying checksum for file: {}", filePath);

        String actualChecksum = calculate(filePath);
        boolean matches = actualChecksum.equalsIgnoreCase(expectedChecksum);

        if (matches) {
            log.info("Checksum verification PASSED: {}", filePath);
        } else {
            log.error("Checksum verification FAILED: file={}, expected={}, actual={}",
                    filePath, expectedChecksum, actualChecksum);
        }

        return matches;
    }

    @Override
    public String getAlgorithm() {
        return ALGORITHM;
    }
}