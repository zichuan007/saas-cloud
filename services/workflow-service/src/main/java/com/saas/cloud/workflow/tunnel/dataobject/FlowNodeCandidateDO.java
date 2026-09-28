package com.saas.cloud.workflow.tunnel.dataobject;

import com.saas.cloud.common.data.base.TenantBaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 节点候选人来源 DO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-14
 */
@TableName("wf_node_candidate")
@Data
@EqualsAndHashCode(callSuper = true)
public class FlowNodeCandidateDO extends TenantBaseEntity {

    /** 节点配置ID (关联 FlowNodeConfigDO.id) */
    private Long nodeConfigId;

    /** 流程定义Key (冗余) */
    private String processDefKey;

    /** 节点定义Key (冗余) */
    private String nodeDefKey;

    /** 分配策略: USER/ROLE/DEPT/GROUP/EXPRESSION/API/INITIATOR */
    private String assignType;

    /** 策略值 (用户ID/角色编码/审批组前缀/表达式/API URL) */
    private String assignValue;

    /** 排序 */
    private Integer sortOrder;
}