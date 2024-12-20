package io.demo.modules.system.domain;

import io.demo.common.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

@Data
public class UserExtend implements Serializable {
    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_extend.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{user_extend.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "其他平台对接信息")
    private byte[] platformInfo;

    @Serial
    private static final long serialVersionUID = 1L;
}