package com.saas.cloud.workflow.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 审批通过请求
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-14
 */
@Data
public class ApproveRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "任务ID不能为空")
    private String taskId;

    /** 审批意见 */
    private String comment;

    /** 抄送人ID列表 */
    private List<Long> copyUserIds;

    /** 操作人ID */
    @NotNull(message = "操作人ID不能为空")
    private Long operatorId;

    /** 操作人姓名 */
    private String operatorName;
}