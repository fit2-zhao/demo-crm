package io.demo.security;

import io.demo.common.groups.Created;
import io.demo.common.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * <p>User Data Transfer Object, used to transfer user information, including other platform integration information and avatar.</p>
 */
@Data
public class UserDTO {
    @Schema(description = "User ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{user.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "Username", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{user.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description = "User email", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user.email.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{user.email.length_range}", groups = {Created.class, Updated.class})
    private String email;

    @Schema(description = "User password")
    private String password;

    @Schema(description = "Is enabled")
    private Boolean enable;

    @Schema(description = "Creation time")
    private Long createTime;

    @Schema(description = "Update time")
    private Long updateTime;

    @Schema(description = "Language")
    private String language;

    @Schema(description = "Current organization ID")
    private String lastOrganizationId;

    @Schema(description = "Phone number")
    private String phone;

    @Schema(description = "Source: LOCAL OIDC CAS OAUTH2", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user.source.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{user.source.length_range}", groups = {Created.class, Updated.class})
    private String source;

    @Schema(description = "Current project ID")
    private String lastProjectId;

    @Schema(description = "Creator")
    private String createUser;

    @Schema(description = "Updater")
    private String updateUser;

    @Schema(description = "Is deleted", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{user.deleted.not_blank}", groups = {Created.class})
    private Boolean deleted;

    @Schema(description = "CFT Token")
    private String cftToken;

    @Schema(description = "Other platform integration information", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private byte[] platformInfo;

    @Schema(description = "Avatar")
    private String avatar;
}