package com.saas.cloud.workflow.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 驳回请求
 */
@Data
public class RejectRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    @NotBlank private String taskId;
    private String comment;
    /** 驳回模式: BPMN/INITIATOR/PREVIOUS/CUSTOM */
    private String rejectMode;
    /** 自定义驳回目标节点 */
    private String customTargetNode;
    @NotNull private Long operatorId;
    private String operatorName;
}