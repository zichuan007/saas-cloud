package com.saas.cloud.workflow.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.io.Serializable;

@Data
public class ResubmitRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    @NotBlank private String taskId;
    @NotBlank private String currentActivityId;
    @NotBlank private String targetActivityId;
    @NotNull private Long operatorId;
    private String operatorName;
}