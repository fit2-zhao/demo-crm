package io.demo.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>表示许可证信息的 DTO 类，包含有关客户和授权的详细信息。</p>
 */
@Data
public class LicenseInfoDTO implements Serializable {

    /**
     * 序列化版本 UID，用于确保类的版本一致性
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 客户名称
     */
    @Schema(description = "客户名称")
    private String corporation;

    /**
     * 授权截止时间
     */
    @Schema(description = "授权截止时间")
    private String expired;

    /**
     * 产品名称
     */
    @Schema(description = "产品名称")
    private String product;

    /**
     * 产品版本
     */
    @Schema(description = "产品版本")
    private String edition;

    /**
     * 许可证版本
     */
    @Schema(description = "license版本")
    private String licenseVersion;

    /**
     * 授权数量
     */
    @Schema(description = "授权数量")
    private int count;

}
