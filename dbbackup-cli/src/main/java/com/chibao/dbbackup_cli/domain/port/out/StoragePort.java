package com.chibao.dbbackup_cli.domain.port.out;

import lombok.Builder;
import lombok.Value;

import java.io.InputStream;
import java.util.Map;

/**
 * OUTBOUND PORT: Storage Port
 * Core cần lưu trữ backup files.
 * Implementations: S3, Local, MinIO, GCS, Azure
 */
public interface StoragePort {
    // Upload file to storage
    String upload(UploadRequest request);

    // Download file from storage
    InputStream download(String identifier);

    // Delete file from storage
    void delete(String identifier);

    // Check if file exists
    boolean exists(String identifier);

    // Get storage provider type
    String getProviderType();

    @Value
    @Builder
    class UploadRequest {
        InputStream data;
        String filename;
        long sizeBytes;
        Map<String, String> metadata;
        boolean enableMultipart;  // For large files
    }
}
