package io.demo.file.engine;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 配置类，用于读取 MinIO 的相关配置。
 * <p>
 * 此类绑定到 Spring Boot 配置文件中的 minio 前缀的属性，
 * 通过 {@link ConfigurationProperties} 注解自动注入相关属性。
 * </p>
 *
 * @version 1.0
 */
@ConfigurationProperties(prefix = MinioProperties.MINIO_PREFIX)
@Getter
@Setter
public class MinioProperties {

    /**
     * 配置项的前缀，用于从配置文件中读取相关 MinIO 配置。
     */
    public static final String MINIO_PREFIX = "minio";

    /**
     * MinIO 的 endpoint 地址。
     * <p>
     * 该属性用于定义与 MinIO 服务的连接地址。
     * </p>
     */
    private String endpoint;

    /**
     * MinIO 的 accessKey，用于身份验证。
     * <p>
     * 该属性用于提供 MinIO 服务的访问凭证之一。
     * </p>
     */
    private String accessKey;

    /**
     * MinIO 的 secretKey，用于身份验证。
     * <p>
     * 该属性用于提供 MinIO 服务的访问凭证之一。
     * </p>
     */
    private String secretKey;

    /**
     * MinIO 使用的存储桶名称。
     * <p>
     * 该属性指定将数据存储在的桶的名称。
     * </p>
     */
    private String bucket;

    /**
     * MinIO 的存储桶是否启用。
     * <p>
     * 该属性用于指示是否启用 MinIO 存储桶。
     * </p>
     */
    private boolean enabled;
}
