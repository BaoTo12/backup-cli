package com.chibao.dbbackup_cli.adapter.out.storage;

import com.chibao.dbbackup_cli.domain.port.out.StoragePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.InputStream;

/**
 * MinIO Storage Adapter (S3-compatible)
 *
 * Can reuse S3Client with MinIO endpoint configuration.
 * Useful for local development and testing.
 */
@Component
@ConditionalOnProperty(name = "storage.provider", havingValue = "minio")
@RequiredArgsConstructor
@Slf4j
class MinIOStorageAdapter implements StoragePort {

    private final S3Client s3Client; // Configured with MinIO endpoint

    @Value("${storage.minio.bucket}")
    private String bucketName;

    // Implementation is identical to S3StorageAdapter
    // MinIO is S3-compatible, so same SDK works

    @Override
    public String upload(UploadRequest request) {
        // Same as S3StorageAdapter
        log.info("Uploading to MinIO: bucket={}, filename={}", bucketName, request.getFilename());
        // ... implementation
        throw new UnsupportedOperationException("MinIO adapter not fully implemented");
    }

    @Override
    public InputStream download(String identifier) {
        throw new UnsupportedOperationException("MinIO adapter not fully implemented");
    }

    @Override
    public void delete(String identifier) {
        throw new UnsupportedOperationException("MinIO adapter not fully implemented");
    }

    @Override
    public boolean exists(String identifier) {
        throw new UnsupportedOperationException("MinIO adapter not fully implemented");
    }

    @Override
    public String getProviderType() {
        return "minio";
    }
}