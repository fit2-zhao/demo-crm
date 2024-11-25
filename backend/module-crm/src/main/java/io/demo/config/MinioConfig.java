package io.demo.config;

import io.demo.common.file.storage.MinioProperties;
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
 * 配置类，用于设置 MinIO 客户端和桶的生命周期管理。
 * <p>
 * 该配置类包含以下功能：
 * 1. 创建 MinIO 客户端，并配置其访问凭证和端点信息。
 * 2. 检查指定的桶是否存在，如果不存在，则创建该桶。
 * 3. 设置 MinIO 桶的生命周期规则，确保临时目录下的文件在 7 天后自动过期。
 * </p>
 */
@Configuration
public class MinioConfig {
    /**
     * 创建 MinIO 客户端，并配置相关属性。
     * <p>
     * 该方法将根据配置的 MinIO 端点和访问密钥生成 MinIO 客户端，并检查指定的桶是否存在，
     * 如果不存在，则创建该桶。同时，设置桶的生命周期规则，确保临时目录下的文件会在 7 天后过期。
     * </p>
     *
     * @param minioProperties MinIO 配置属性，包括端点、访问密钥、密钥和桶名称
     * @return 配置好的 MinioClient 对象
     * @throws Exception 如果 MinIO 客户端初始化或桶操作失败，则抛出异常
     */
    @Bean
    public MinioClient minioClient(MinioProperties minioProperties) throws Exception {
        // 如果 MinIO 未启用，则直接返回 null
        if (!minioProperties.isEnabled()) {
            LogUtils.info("MinIO is not enabled, skip MinIO client initialization.");
            return null;
        }

        // 创建 MinioClient 客户端
        MinioClient minioClient = MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();

        // 设置临时目录下文件的过期时间
        setBucketLifecycle(minioClient, minioProperties.getBucket());

        // 检查桶是否存在，如果不存在，则创建该桶
        boolean exist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioProperties.getBucket()).build());
        if (!exist) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioProperties.getBucket()).build());
        }
        return minioClient;
    }

    /**
     * 设置桶的生命周期规则，自动过期临时目录下的文件。
     * <p>
     * 该方法将桶内名为 "system/temp/" 的文件设置为 7 天后自动过期，帮助管理临时文件的生命周期。
     * </p>
     *
     * @param minioClient MinIO 客户端，用于设置生命周期规则
     * @param bucket      目标桶的名称
     */
    private static void setBucketLifecycle(MinioClient minioClient, String bucket) {
        // 设置生命周期规则
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

        // 配置生命周期规则
        LifecycleConfiguration config = new LifecycleConfiguration(rules);
        try {
            minioClient.setBucketLifecycle(
                    SetBucketLifecycleArgs.builder()
                            .bucket(bucket)
                            .config(config)
                            .build());
        } catch (Exception e) {
            // 日志记录异常
            LogUtils.error(e);
        }
    }
}
