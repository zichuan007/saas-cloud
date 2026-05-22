package com.saas.cloud.workflow.api.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 任务委派请求DTO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
public class TaskDelegateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Flowable任务ID */
    @NotBlank(message = "任务ID不能为空")
    private String taskId;

    /** 目标用户ID */
    @NotNull(message = "目标用户ID不能为空")
    private Long targetUserId;

    /** 委派说明 */
    private String comment;
}
