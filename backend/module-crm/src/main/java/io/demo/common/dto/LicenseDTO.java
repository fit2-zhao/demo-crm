package io.demo.common.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>表示许可证信息的 DTO 类。</p>
 * <p>包含许可证的状态和许可证的详细信息。</p>
 */
@Data
public class LicenseDTO implements Serializable {

    /**
     * 序列化版本 UID，用于保证类的版本一致性
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 许可证的状态（如有效、无效等）
     */
    private String status;

    /**
     * 许可证的详细信息
     */
    private LicenseInfoDTO license;

}
