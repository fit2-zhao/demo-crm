package io.demo.crm.common.dto;

import io.demo.crm.modules.system.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>用户数据传输对象，用于传输用户信息，包含其他平台对接信息和头像。</p>
 */
@Data
public class UserDTO extends User {

    /**
     * 继承自 {@link User} 的基础信息
     */
    private String id;  // 或根据需要选择继承或独立字段

    @Schema(description = "其他平台对接信息", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private byte[] platformInfo;

    @Schema(description = "头像")
    private String avatar;


    // 如果需要转换或加密解密，可以在这里添加相关逻辑
}
