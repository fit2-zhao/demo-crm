package io.demo.crm.common.log.dto;

import io.demo.crm.services.system.domain.OperationLog;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 日志数据传输对象（DTO），继承自操作日志（OperationLog）。
 * 用于封装操作日志的具体数据，包含变更前后内容、是否需要历史记录等信息。
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class LogDTO extends OperationLog {

    /**
     * 变更前内容，采用字节数组存储，以便灵活处理不同类型的内容
     */
    @Schema(description = "变更前内容")
    private byte[] originalValue;

    /**
     * 变更后内容，采用字节数组存储，以便灵活处理不同类型的内容
     */
    @Schema(description = "变更后内容")
    private byte[] modifiedValue;

    /**
     * 是否需要历史记录的标志，默认为 false
     */
    @Schema(description = "是否需要历史记录")
    private Boolean history = false;

    /**
     * 默认构造函数
     */
    public LogDTO() {
    }

    /**
     * 带参构造函数，用于快速初始化日志数据
     *
     * @param projectId      项目ID
     * @param organizationId 组织ID
     * @param sourceId       数据源ID
     * @param createUser     创建用户
     * @param type           日志类型
     * @param module         模块
     * @param content        日志内容
     */
    public LogDTO(String projectId, String organizationId, String sourceId, String createUser, String type, String module, String content) {
        this.setProjectId(projectId);
        this.setOrganizationId(organizationId);
        this.setSourceId(sourceId);
        this.setCreateUser(createUser);
        this.setType(type);
        this.setModule(module);
        this.setContent(content);
        this.setCreateTime(System.currentTimeMillis());
    }
}
