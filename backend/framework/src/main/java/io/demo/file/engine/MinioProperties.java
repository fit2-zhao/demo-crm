package io.demo.file.engine;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration class for reading MinIO related settings.
 * <p>
 * This class is bound to the properties with the prefix "minio" in the Spring Boot configuration file,
 * and the relevant properties are automatically injected through the {@link ConfigurationProperties} annotation.
 * </p>
 *
 * @version 1.0
 */
@ConfigurationProperties(prefix = MinioProperties.MINIO_PREFIX)
@Getter
@Setter
public class MinioProperties {

    /**
     * The prefix for the configuration items, used to read the relevant MinIO settings from the configuration file.
     */
    public static final String MINIO_PREFIX = "minio";

    /**
     * The endpoint address of MinIO.
     * <p>
     * This property is used to define the connection address to the MinIO service.
     * </p>
     */
    private String endpoint;

    /**
     * The access key of MinIO, used for authentication.
     * <p>
     * This property is used to provide one of the access credentials for the MinIO service.
     * </p>
     */
    private String accessKey;

    /**
     * The secret key of MinIO, used for authentication.
     * <p>
     * This property is used to provide one of the access credentials for the MinIO service.
     * </p>
     */
    private String secretKey;

    /**
     * The bucket name used by MinIO.
     * <p>
     * This property specifies the name of the bucket where data will be stored.
     * </p>
     */
    private String bucket;

    /**
     * Whether the MinIO bucket is enabled.
     * <p>
     * This property is used to indicate whether the MinIO bucket is enabled.
     * </p>
     */
    private boolean enabled;
}