package com.chibao.dbbackup_cli.adapter.out.checksum;

// ===== CHECKSUM UTILITY (Additional) =====

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * Checksum Utility
 *
 * Provides additional checksum methods (MD5, SHA-1, etc.)
 */
@Component
@Slf4j
class ChecksumUtility {

    /**
     * Calculate MD5 checksum (for backward compatibility)
     */
    public String calculateMD5(Path filePath) throws IOException {
        return calculateChecksum(filePath, "MD5");
    }

    /**
     * Calculate checksum with specified algorithm
     */
    private String calculateChecksum(Path filePath, String algorithm) throws IOException {
        try (InputStream inputStream = Files.newInputStream(filePath)) {
            MessageDigest digest = MessageDigest.getInstance(algorithm);

            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }

            byte[] hashBytes = digest.digest();
            return HexFormat.of().formatHex(hashBytes);

        } catch (Exception e) {
            throw new IOException("Checksum calculation failed", e);
        }
    }
}
