package com.saas.cloud.workflow.domain.service.action;

import com.saas.cloud.workflow.api.enums.ApprovalActionEnum;
import com.saas.cloud.workflow.api.dto.request.ResubmitRequest;
import com.saas.cloud.workflow.domain.service.action.support.ApprovalActionSupport;
import com.saas.cloud.workflow.tunnel.dataobject.FlowApprovalRecordDO;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Resubmit 审批动作
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-14
 */
@Component
public class ResubmitActionService extends ApprovalActionSupport implements ApprovalAction<ResubmitRequest> {

    @Override
    public ApprovalActionEnum getActionType() {
        return ApprovalActionEnum.RESUBMIT;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(ResubmitRequest request) {
        // 从审批记录反查驳回锚点，找到驳回来源节点
        Map<String, Object> task = getAndValidateTask(request.getTaskId());
        String taskId = (String) task.get("taskId");
        String processInstanceId = (String) task.get("processInstanceId");

        // 跳回驳回来源节点
        processEngine.moveActivityIdTo(request.getCurrentActivityId(), request.getTargetActivityId());

        // 记录审批日志
        FlowApprovalRecordDO record = buildRecord(
                processInstanceId, taskId,
                (String) task.get("taskDefinitionKey"),
                (String) task.get("taskName"),
                request.getOperatorId(), request.getOperatorName(),
                ApprovalActionEnum.RESUBMIT.getCode(), "重新提交"
        );
        record.setFlowType("REJECT");
        saveRecord(record);
    }
}