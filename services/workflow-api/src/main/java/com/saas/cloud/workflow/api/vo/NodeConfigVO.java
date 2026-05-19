package com.saas.cloud.workflow.api.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 节点审批人配置视图对象
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
public class NodeConfigVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** BPMN节点ID */
    private String nodeId;

    /** 节点名称 */
    private String nodeName;

    /** 审批人类型 1-指定用户 2-指定角色 3-部门负责人 4-发起人自选 */
    private Byte assigneeType;

    /** 审批人类型描述 */
    private String assigneeTypeDesc;

    /** 审批人/角色ID列表(JSON) */
    private String assigneeIds;

    /** 审批模式 1-或签 2-会签 3-依次 */
    private Byte approvalMode;

    /** 审批模式描述 */
    private String approvalModeDesc;
}
