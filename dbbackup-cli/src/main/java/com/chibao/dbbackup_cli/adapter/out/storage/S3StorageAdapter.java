package com.chibao.dbbackup_cli.adapter.out.storage;

import com.chibao.dbbackup_cli.domain.exception.StorageException;
import com.chibao.dbbackup_cli.domain.port.out.StoragePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * S3 Storage Adapter
 *
 * OUTBOUND ADAPTER - implements StoragePort
 *
 * Handles file upload/download to AWS S3.
 * Supports multipart upload for large files.
 */
@Component
@ConditionalOnProperty(name = "storage.provider", havingValue = "s3")
@RequiredArgsConstructor
@Slf4j
public class S3StorageAdapter implements StoragePort {

    private final S3Client s3Client;

    @Value("${storage.s3.bucket}")
    private String bucketName;

    @Value("${storage.s3.prefix:backups/}")
    private String prefix;

    private static final long MULTIPART_THRESHOLD = 100 * 1024 * 1024; // 100MB
    private static final long PART_SIZE = 50 * 1024 * 1024; // 50MB per part

    @Override
    public String upload(UploadRequest request) {
        String objectKey = buildObjectKey(request.getFilename());

        log.info("Uploading to S3: bucket={}, key={}, size={} bytes",
                bucketName, objectKey, request.getSizeBytes());

        try {
            if (request.isEnableMultipart() && request.getSizeBytes() > MULTIPART_THRESHOLD) {
                return multipartUpload(request, objectKey);
            } else {
                return simpleUpload(request, objectKey);
            }
        } catch (Exception e) {
            log.error("S3 upload failed: key={}", objectKey, e);
            throw new StorageException("S3 upload failed", e);
        }
    }

    @Override
    public InputStream download(String identifier) {
        log.info("Downloading from S3: bucket={}, key={}", bucketName, identifier);

        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(identifier)
                    .build();

            return s3Client.getObject(getRequest);

        } catch (S3Exception e) {
            log.error("S3 download failed: key={}", identifier, e);
            throw new StorageException("S3 download failed", e);
        }
    }

    @Override
    public void delete(String identifier) {
        log.info("Deleting from S3: bucket={}, key={}", bucketName, identifier);

        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(identifier)
                    .build();

            s3Client.deleteObject(deleteRequest);

        } catch (S3Exception e) {
            log.error("S3 delete failed: key={}", identifier, e);
            throw new StorageException("S3 delete failed", e);
        }
    }

    @Override
    public boolean exists(String identifier) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(identifier)
                    .build();

            s3Client.headObject(headRequest);
            return true;

        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            log.error("S3 exists check failed: key={}", identifier, e);
            throw new StorageException("S3 exists check failed", e);
        }
    }

    @Override
    public String getProviderType() {
        return "s3";
    }

    // ===== PRIVATE METHODS =====

    /**
     * Simple upload for small files
     */
    private String simpleUpload(UploadRequest request, String objectKey) {
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .metadata(request.getMetadata())
                .build();

        s3Client.putObject(
                putRequest,
                RequestBody.fromInputStream(request.getData(), request.getSizeBytes())
        );

        log.info("S3 simple upload completed: key={}", objectKey);
        return objectKey;
    }

    /**
     * Multipart upload for large files
     *
     * Properly handles byte buffer and uses correct S3 API
     */
    private String multipartUpload(UploadRequest request, String objectKey) throws IOException {
        log.info("Starting S3 multipart upload: key={}, size={} bytes",
                objectKey, request.getSizeBytes());

        // 1. Initiate multipart upload
        CreateMultipartUploadRequest createRequest = CreateMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .metadata(request.getMetadata())
                .build();

        CreateMultipartUploadResponse createResponse = s3Client.createMultipartUpload(createRequest);
        String uploadId = createResponse.uploadId();

        log.debug("Multipart upload initiated: uploadId={}", uploadId);

        List<CompletedPart> completedParts = new ArrayList<>();
        int partNumber = 1;
        byte[] buffer = new byte[(int) PART_SIZE];
        int bytesRead;

        try {
            // 2. Upload parts
            InputStream inputStream = request.getData();

            while ((bytesRead = inputStream.read(buffer)) > 0) {

                // Create byte array with actual bytes read (important!)
                byte[] partData = java.util.Arrays.copyOf(buffer, bytesRead);

                // Build upload part request with RequestBody
                UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                        .bucket(bucketName)
                        .key(objectKey)
                        .uploadId(uploadId)
                        .partNumber(partNumber)
                        .contentLength((long) bytesRead)  // Important for S3
                        .build();

                // ✅ CORRECT: uploadPart() takes request + RequestBody separately
                UploadPartResponse uploadPartResponse = s3Client.uploadPart(
                        uploadPartRequest,
                        RequestBody.fromBytes(partData)
                );

                CompletedPart part = CompletedPart.builder()
                        .partNumber(partNumber)
                        .eTag(uploadPartResponse.eTag())
                        .build();

                completedParts.add(part);

                log.debug("Uploaded part {}: {} bytes, etag={}",
                        partNumber, bytesRead, uploadPartResponse.eTag());

                partNumber++;
            }

            // Validate at least one part uploaded
            if (completedParts.isEmpty()) {
                throw new StorageException("No parts uploaded - file may be empty");
            }

            // 3. Complete multipart upload
            CompletedMultipartUpload completedUpload = CompletedMultipartUpload.builder()
                    .parts(completedParts)
                    .build();

            CompleteMultipartUploadRequest completeRequest = CompleteMultipartUploadRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .uploadId(uploadId)
                    .multipartUpload(completedUpload)
                    .build();

            s3Client.completeMultipartUpload(completeRequest);

            log.info("S3 multipart upload completed: key={}, parts={}", objectKey, completedParts.size());
            return objectKey;

        } catch (Exception e) {
            // Abort multipart upload on failure
            log.error("Multipart upload failed, aborting: uploadId={}", uploadId, e);

            try {
                AbortMultipartUploadRequest abortRequest = AbortMultipartUploadRequest.builder()
                        .bucket(bucketName)
                        .key(objectKey)
                        .uploadId(uploadId)
                        .build();

                s3Client.abortMultipartUpload(abortRequest);
                log.info("Aborted multipart upload: uploadId={}", uploadId);

            } catch (Exception abortException) {
                log.error("Failed to abort multipart upload: uploadId={}", uploadId, abortException);
            }

            throw new StorageException("Multipart upload failed", e);
        }
    }

    /**
     * Build S3 object key with prefix
     * Format: prefix/env/dbtype/dbname/yyyy/MM/filename
     */
    private String buildObjectKey(String filename) {
        // Extract metadata from filename if available
        // For now, simple prefix + filename
        return prefix + filename;
    }
}