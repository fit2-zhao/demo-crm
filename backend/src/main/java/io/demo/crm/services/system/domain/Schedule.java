package io.demo.crm.services.system.domain;

import io.demo.crm.common.groups.Created;
import io.demo.crm.common.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class Schedule implements Serializable {
    @Schema(description = "", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{schedule.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{schedule.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "qrtz UUID")
    private String key;

    @Schema(description = "执行类型 cron", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{schedule.type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{schedule.type.length_range}", groups = {Created.class, Updated.class})
    private String type;

    @Schema(description = "cron 表达式", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{schedule.value.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{schedule.value.length_range}", groups = {Created.class, Updated.class})
    private String value;

    @Schema(description = "Schedule Job Class Name", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{schedule.job.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{schedule.job.length_range}", groups = {Created.class, Updated.class})
    private String job;

    @Schema(description = "资源类型 API/TESL_PLAN", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{schedule.resource_type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{schedule.resource_type.length_range}", groups = {Created.class, Updated.class})
    private String resourceType;

    @Schema(description = "是否开启")
    private Boolean enable;

    @Schema(description = "资源ID，api_scenario ui_scenario load_test")
    private String resourceId;

    @Schema(description = "创建人")
    private String createUser;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;

    @Schema(description = "项目ID")
    private String projectId;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "配置")
    private String config;

    @Schema(description = "业务ID")
    private Long num;

    private static final long serialVersionUID = 1L;
}