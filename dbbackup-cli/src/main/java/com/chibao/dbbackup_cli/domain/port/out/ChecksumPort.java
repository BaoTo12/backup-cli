package com.chibao.dbbackup_cli.domain.port.out;


import java.io.InputStream;
import java.nio.file.Path;

/**
 * OUTBOUND PORT: Checksum Port
 * Core cần tính checksum cho integrity verification
 */
public interface ChecksumPort {

    // Calculate checksum of file
    String calculate(Path filePath);

   // Calculate checksum of stream
    String calculate(InputStream inputStream);

   // Verify checksum matches
    boolean verify(Path filePath, String expectedChecksum);

    // Get algorithm name --> algorithm (SHA-256, MD5, etc.)
    String getAlgorithm();
}
