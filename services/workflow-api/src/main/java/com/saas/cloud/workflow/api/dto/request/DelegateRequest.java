package com.saas.cloud.workflow.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 委派请求
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-14
 */
@Data
public class DelegateRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    @NotBlank private String taskId;
    @NotNull private Long targetUserId;
    private String comment;
    @NotNull private Long operatorId;
    private String operatorName;
}