package com.saas.cloud.workflow.tunnel.dataobject;

import com.saas.cloud.common.data.base.TenantBaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 审批操作记录 DO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-14
 */
@TableName("wf_approval_record")
@Data
@EqualsAndHashCode(callSuper = true)
public class FlowApprovalRecordDO extends TenantBaseEntity {

    /** Flowable流程实例ID */
    private String processInstanceId;

    /** 流程定义Key (冗余，方便按流程类型统计) */
    private String processDefKey;

    /** 流程名称 */
    private String processName;

    /** Flowable任务ID (流程级操作如撤销/撤回时为NULL) */
    private String taskId;

    /** 节点定义Key (流程级操作时为NULL) */
    private String taskDefKey;

    /** 节点名称 */
    private String taskName;

    /** 操作人ID */
    private Long operatorId;

    /** 操作人姓名 */
    private String operatorName;

    /** 操作人部门ID */
    private Long operatorDeptId;

    /** 操作动作: APPROVED/REJECTED/RETURNED/TRANSFER/DELEGATE/ADD_SIGN/RECALL/CANCEL/RESUBMIT/URGE/AUTO_SUBMIT/AUTO_SKIP */
    private String action;

    /** 操作来源: USER/SYSTEM */
    private String operatorType;

    /** 审批意见 */
    private String comment;

    /** 系统判定原因 (如NODE_AUTO_SKIPPED)，与人工comment分离 */
    private String systemReason;

    /** 操作时间 */
    private LocalDateTime actionTime;

    /** 流转分类: NORMAL/REJECT/AUTO_SUBMIT */
    private String flowType;

    /** 来源节点Key (驳回/跳转场景) */
    private String sourceNodeKey;

    /** 目标节点Key (驳回/跳转场景) */
    private String targetNodeKey;

    /** 父任务ID (转办/委派/加签场景) */
    private String parentTaskId;

    /** 节点审批策略: ANY/ALL/SEQUENTIAL */
    private String approveStrategy;

    /** 是否终态决定: 0-否 1-是 */
    private Integer isFinalDecision;
}