package io.demo.crm.services.system.domain;

import io.demo.crm.common.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

@Data
public class OperationLogBlob implements Serializable {
    @Schema(description = "主键,与operation_log表id一致", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{operation_log_blob.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{operation_log_blob.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "变更前内容")
    private byte[] originalValue;

    @Schema(description = "变更后内容")
    private byte[] modifiedValue;

    @Serial
    private static final long serialVersionUID = 1L;
}