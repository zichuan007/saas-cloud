package com.saas.cloud.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.saas.cloud.common.data.base.TenantBaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 流程节点审批人配置表
 *
 * @author saas-cloud
 * @since 2026-05-18
 */
@Getter
@Setter
@TableName("wf_node_config")
public class WfNodeConfig extends TenantBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 流程定义ID
     */
    @TableField("process_definition_id")
    private String processDefinitionId;

    /**
     * BPMN节点ID
     */
    @TableField("node_id")
    private String nodeId;

    /**
     * 节点名称
     */
    @TableField("node_name")
    private String nodeName;

    /**
     * 审批人类型 1-指定用户 2-指定角色 3-部门负责人 4-发起人自选
     */
    @TableField("assignee_type")
    private Byte assigneeType;

    /**
     * 审批人/角色ID列表(JSON)
     */
    @TableField("assignee_ids")
    private String assigneeIds;

    /**
     * 审批模式 1-或签 2-会签 3-依次
     */
    @TableField("approval_mode")
    private Byte approvalMode;
}
