package com.chibao.dbbackup_cli.domain.port.out;

import lombok.Builder;
import lombok.Value;

import java.nio.file.Path;
import java.util.Map;

/**
 * OUTBOUND PORT: Encryption Port
 * Core cần encrypt/decrypt backup files
 */
public interface EncryptionPort {
    /**
     * Encrypt file
     * input file to encrypt
     * config encryption configuration
     * @return encrypted file path
     */
    Path encrypt(Path input, EncryptionConfig config);

    /**
     * Decrypt file
     *  input encrypted file
     *  config encryption configuration
     * @return decrypted file path
     */
    Path decrypt(Path input, EncryptionConfig config);

    /**
     * Get encryption algorithm
     * @return algorithm name (AES-256-GCM)
     */
    String getAlgorithm();

    @Value
    @Builder
    class EncryptionConfig {
        String keyId;           // KMS key ID or reference
        byte[] key;             // Data encryption key (DEK)
        String algorithm;
        Map<String, String> additionalParams;
    }
}
