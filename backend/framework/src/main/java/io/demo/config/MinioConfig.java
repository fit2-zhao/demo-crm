package io.demo.config;

import io.demo.file.engine.MinioProperties;
import io.demo.common.util.LogUtils;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketLifecycleArgs;
import io.minio.messages.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;

/**
 * Configuration class for setting up MinIO client and bucket lifecycle management.
 * <p>
 * This configuration class includes the following functionalities:
 * 1. Create a MinIO client and configure its access credentials and endpoint information.
 * 2. Check if the specified bucket exists, and create it if it does not.
 * 3. Set lifecycle rules for the MinIO bucket to ensure files in the temporary directory expire after 7 days.
 * </p>
 */
@Configuration
public class MinioConfig {
    /**
     * Creates a MinIO client and configures related properties.
     * <p>
     * This method generates a MinIO client based on the configured MinIO endpoint and access keys, checks if the specified bucket exists,
     * and creates the bucket if it does not. It also sets lifecycle rules for the bucket to ensure files in the temporary directory expire after 7 days.
     * </p>
     *
     * @param minioProperties MinIO configuration properties, including endpoint, access key, secret key, and bucket name
     * @return Configured MinioClient object
     * @throws Exception If MinIO client initialization or bucket operations fail
     */
    @Bean
    public MinioClient minioClient(MinioProperties minioProperties) throws Exception {
        // If MinIO is not enabled, return null
        if (!minioProperties.isEnabled()) {
            LogUtils.info("MinIO is not enabled, skip MinIO client initialization.");
            return null;
        }

        // Create MinioClient client
        MinioClient minioClient = MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();

        // Set expiration time for files in the temporary directory
        setBucketLifecycle(minioClient, minioProperties.getBucket());

        // Check if the bucket exists, and create it if it does not
        boolean exist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioProperties.getBucket()).build());
        if (!exist) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioProperties.getBucket()).build());
        }
        return minioClient;
    }

    /**
     * Sets lifecycle rules for the bucket to automatically expire files in the temporary directory.
     * <p>
     * This method sets files named "system/temp/" in the bucket to expire after 7 days, helping manage the lifecycle of temporary files.
     * </p>
     *
     * @param minioClient MinIO client used to set lifecycle rules
     * @param bucket      Name of the target bucket
     */
    private static void setBucketLifecycle(MinioClient minioClient, String bucket) {
        // Set lifecycle rules
        List<LifecycleRule> rules = new LinkedList<>();
        rules.add(
                new LifecycleRule(
                        Status.ENABLED,
                        null,
                        new Expiration((ZonedDateTime) null, 7, null),
                        new RuleFilter("system/temp/"),
                        "temp-file",
                        null,
                        null,
                        null));

        // Configure lifecycle rules
        LifecycleConfiguration config = new LifecycleConfiguration(rules);
        try {
            minioClient.setBucketLifecycle(
                    SetBucketLifecycleArgs.builder()
                            .bucket(bucket)
                            .config(config)
                            .build());
        } catch (Exception e) {
            // Log the exception
            LogUtils.error(e);
        }
    }
}