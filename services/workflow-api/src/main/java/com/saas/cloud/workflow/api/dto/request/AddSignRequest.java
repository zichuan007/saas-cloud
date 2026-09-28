package com.saas.cloud.workflow.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 加签请求
 */
@Data
public class AddSignRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    @NotBlank private String taskId;
    @NotNull private List<Long> userIds;
    private String comment;
    @NotNull private Long operatorId;
    private String operatorName;
}