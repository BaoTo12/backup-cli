package com.chibao.dbbackup_cli.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

/**
 * Application Configuration
 * Wires all dependencies together.
 * Configures factories for auto-discovery of adapters.
 */
@Configuration
@Slf4j
public class AdapterConfig {

    /**
     * S3 Client Bean
     * Configured for AWS S3 or MinIO (S3-compatible)
     */
    @Bean
    public S3Client s3Client(
            @org.springframework.beans.factory.annotation.Value("${storage.s3.endpoint:}")
            String endpoint,

            @org.springframework.beans.factory.annotation.Value("${storage.s3.region:us-east-1}")
            String region,

            @org.springframework.beans.factory.annotation.Value("${storage.s3.access-key:}")
            String accessKey,

            @org.springframework.beans.factory.annotation.Value("${storage.s3.secret-key:}")
            String secretKey
    ) {

        var builder = S3Client.builder()
                .region(Region.of(region));

        // For MinIO or custom S3-compatible endpoint
        if (!endpoint.isEmpty()) {
            log.info("Configuring S3 client with custom endpoint: {}", endpoint);
            builder.endpointOverride(URI.create(endpoint));
        }

        // For explicit credentials (not recommended for production)
        if (!accessKey.isEmpty() && !secretKey.isEmpty()) {
            log.warn("Using static credentials for S3 - not recommended for production");
            builder.credentialsProvider(
                    StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKey, secretKey)
                    )
            );
        }

        return builder.build();
    }
}
