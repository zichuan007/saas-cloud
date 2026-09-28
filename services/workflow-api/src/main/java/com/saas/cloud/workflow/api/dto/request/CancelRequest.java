package com.saas.cloud.workflow.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.io.Serializable;

@Data
public class CancelRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    @NotBlank private String processInstanceId;
    private String reason;
    @NotNull private Long operatorId;
    private String operatorName;
}