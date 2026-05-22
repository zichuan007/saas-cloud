package com.saas.cloud.workflow.api.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 任务驳回请求DTO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
public class TaskRejectDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Flowable任务ID */
    @NotBlank(message = "任务ID不能为空")
    private String taskId;

    /** 驳回原因 */
    private String comment;
}
