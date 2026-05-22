package com.saas.cloud.workflow.api.dto;

import java.io.Serializable;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * 任务加签请求DTO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
public class TaskAddSignDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Flowable任务ID */
    @NotBlank(message = "任务ID不能为空")
    private String taskId;

    /** 加签用户ID列表 */
    @NotEmpty(message = "加签用户不能为空")
    private List<Long> userIds;

    /** 加签说明 */
    private String comment;
}
