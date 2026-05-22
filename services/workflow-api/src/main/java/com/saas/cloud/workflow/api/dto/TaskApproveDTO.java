package com.saas.cloud.workflow.api.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

/**
 * 任务审批通过请求DTO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
public class TaskApproveDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Flowable任务ID */
    @NotBlank(message = "任务ID不能为空")
    private String taskId;

    /** 审批意见 */
    private String comment;

    /** 抄送人ID列表 */
    private List<Long> copyUserIds;
}
