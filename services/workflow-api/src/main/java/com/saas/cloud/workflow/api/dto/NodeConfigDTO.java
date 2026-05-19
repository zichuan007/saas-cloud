package com.saas.cloud.workflow.api.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 节点审批人配置请求DTO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
public class NodeConfigDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** BPMN节点ID */
    @NotBlank(message = "节点ID不能为空")
    private String nodeId;

    /** 节点名称 */
    @NotBlank(message = "节点名称不能为空")
    private String nodeName;

    /** 审批人类型 1-指定用户 2-指定角色 3-部门负责人 4-发起人自选 */
    @NotNull(message = "审批人类型不能为空")
    private Byte assigneeType;

    /** 审批人/角色ID列表(JSON) */
    private String assigneeIds;

    /** 审批模式 1-或签 2-会签 3-依次 */
    private Byte approvalMode;
}
