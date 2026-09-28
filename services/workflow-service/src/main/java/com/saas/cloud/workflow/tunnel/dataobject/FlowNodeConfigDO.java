package com.saas.cloud.workflow.tunnel.dataobject;

import com.saas.cloud.common.data.base.TenantBaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 流程节点配置 DO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-14
 */
@TableName("wf_node_config")
@Data
@EqualsAndHashCode(callSuper = true)
public class FlowNodeConfigDO extends TenantBaseEntity {

    /** 流程定义Key */
    private String processDefKey;

    /** 节点定义Key */
    private String nodeDefKey;

    /** 节点名称 */
    private String nodeName;

    /** 审批模式: ANY-或签/ALL-会签/SEQUENTIAL-顺签 */
    private String approveMode;

    /** 通过阈值 (会签模式下达到此数即通过) */
    private Integer passThreshold;

    /** 顺签顺序 */
    private Integer sequentialOrder;

    /** 是否启用超时: 0-禁用 1-启用 */
    private Integer timeoutEnabled;

    /** 超时小时数 */
    private Integer timeoutHours;

    /** 超时策略: APPROVE/REJECT/TRANSFER/REMIND */
    private String timeoutStrategy;

    /** 超时转办人ID */
    private Long timeoutTransferUserId;

    /** 驳回模式: BPMN-默认流/INITIATOR-发起人/PREVIOUS-上一节点/CUSTOM-自定义 */
    private String rejectMode;

    /** 自定义驳回目标节点Key */
    private String customTargetNode;

    /** 同审批人自动跳过: 0-不跳过 1-跳过 */
    private Integer sameApproverSkip;

    /** 是否启用: 0-禁用 1-启用 */
    private Integer enabled;

    /** 排序 */
    private Integer sortOrder;
}