package com.chibao.dbbackup_cli.adapter.out.encryption;

import com.chibao.dbbackup_cli.domain.exception.StorageException;
import com.chibao.dbbackup_cli.domain.port.out.EncryptionPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.SecureRandom;

/**
 * AES-256-GCM Encryption Adapter
 *
 * OUTBOUND ADAPTER - implements EncryptionPort
 *
 * Encrypts/decrypts files using AES-256-GCM algorithm.
 * GCM provides both confidentiality and authenticity.
 */
@Component
@Slf4j
class AesGcmEncryptionAdapter implements EncryptionPort {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256; // bits
    private static final int GCM_IV_LENGTH = 12; // bytes (96 bits recommended)
    private static final int GCM_TAG_LENGTH = 128; // bits
    private static final int BUFFER_SIZE = 8192; // 8KB

    @Override
    public Path encrypt(Path input, EncryptionConfig config) {
        log.info("Encrypting file: {}", input);

        try {
            // 1. Generate or get encryption key
            SecretKey secretKey = getOrGenerateKey(config);

            // 2. Generate random IV (Initialization Vector)
            byte[] iv = generateIV();

            // 3. Initialize cipher
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);

            // 4. Create output file
            Path outputPath = createEncryptedFilePath(input);

            // 5. Write IV to output file first (needed for decryption)
            try (FileOutputStream fos = new FileOutputStream(outputPath.toFile());
                 FileInputStream fis = new FileInputStream(input.toFile())) {

                // Write IV length + IV
                fos.write(iv.length);
                fos.write(iv);

                // Encrypt file content
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;

                while ((bytesRead = fis.read(buffer)) != -1) {
                    byte[] output = cipher.update(buffer, 0, bytesRead);
                    if (output != null) {
                        fos.write(output);
                    }
                }

                // Write final block (includes GCM authentication tag)
                byte[] outputBytes = cipher.doFinal();
                if (outputBytes != null) {
                    fos.write(outputBytes);
                }
            }

            log.info("File encrypted successfully: {} -> {}", input, outputPath);

            return outputPath;

        } catch (Exception e) {
            log.error("Encryption failed for file: {}", input, e);
            throw new StorageException("Encryption failed", e);
        }
    }

    @Override
    public Path decrypt(Path input, EncryptionConfig config) {
        log.info("Decrypting file: {}", input);

        try {
            // 1. Get decryption key
            SecretKey secretKey = getOrGenerateKey(config);

            // 2. Read IV from file
            byte[] iv;
            try (FileInputStream fis = new FileInputStream(input.toFile())) {
                int ivLength = fis.read();
                iv = new byte[ivLength];
                fis.read(iv);
            }

            // 3. Initialize cipher
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);

            // 4. Create output file
            Path outputPath = createDecryptedFilePath(input);

            // 5. Decrypt file content
            try (FileInputStream fis = new FileInputStream(input.toFile());
                 FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {

                // Skip IV (already read)
                fis.skip(1 + iv.length);

                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;

                while ((bytesRead = fis.read(buffer)) != -1) {
                    byte[] output = cipher.update(buffer, 0, bytesRead);
                    if (output != null) {
                        fos.write(output);
                    }
                }

                // Write final block (verifies GCM authentication tag)
                byte[] outputBytes = cipher.doFinal();
                if (outputBytes != null) {
                    fos.write(outputBytes);
                }
            }

            log.info("File decrypted successfully: {} -> {}", input, outputPath);

            return outputPath;

        } catch (Exception e) {
            log.error("Decryption failed for file: {}", input, e);
            throw new StorageException("Decryption failed", e);
        }
    }

    @Override
    public String getAlgorithm() {
        return TRANSFORMATION;
    }

    // ===== PRIVATE HELPER METHODS =====

    /**
     * Get encryption key from config or generate new one
     */
    private SecretKey getOrGenerateKey(EncryptionConfig config) throws Exception {

        if (config.getKey() != null && config.getKey().length > 0) {
            // Use provided key
            return new SecretKeySpec(config.getKey(), ALGORITHM);
        } else {
            // Generate new key
            log.warn("No encryption key provided, generating new key (NOT recommended for production)");
            KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
            keyGen.init(KEY_SIZE);
            return keyGen.generateKey();
        }
    }

    /**
     * Generate random Initialization Vector (IV)
     */
    private byte[] generateIV() {
        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        return iv;
    }

    /**
     * Create output path for encrypted file
     */
    private Path createEncryptedFilePath(Path input) throws IOException {
        String filename = input.getFileName().toString();
        String encryptedFilename = filename + ".enc";
        return input.getParent().resolve(encryptedFilename);
    }

    /**
     * Create output path for decrypted file
     */
    private Path createDecryptedFilePath(Path input) throws IOException {
        String filename = input.getFileName().toString();
        String decryptedFilename = filename.replace(".enc", "");
        return input.getParent().resolve(decryptedFilename);
    }
}