package com.saas.cloud.workflow.domain.service.action;

import com.saas.cloud.workflow.api.enums.ApprovalActionEnum;
import com.saas.cloud.workflow.api.dto.request.RejectRequest;
import com.saas.cloud.workflow.domain.service.action.support.ApprovalActionSupport;
import com.saas.cloud.workflow.tunnel.dataobject.FlowApprovalRecordDO;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Reject 审批动作
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-14
 */
@Component
public class RejectActionService extends ApprovalActionSupport implements ApprovalAction<RejectRequest> {

    @Override
    public ApprovalActionEnum getActionType() {
        return ApprovalActionEnum.REJECTED;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(RejectRequest request) {
        Map<String, Object> task = getAndValidateTask(request.getTaskId());
        String taskId = (String) task.get("taskId");
        String processInstanceId = (String) task.get("processInstanceId");
        String taskDefKey = (String) task.get("taskDefinitionKey");

        // 驳回走 BPMN 默认流: 设置路由变量后完成当前任务
        Map<String, Object> variables = new java.util.HashMap<>();
        variables.put("RESULT", "REJECT");
        variables.put("ACTION", "REJECTED");
        processEngine.completeTask(taskId, variables);

        // 记录审批日志
        FlowApprovalRecordDO record = buildRecord(
                processInstanceId, taskId, taskDefKey,
                (String) task.get("taskName"),
                request.getOperatorId(), request.getOperatorName(),
                ApprovalActionEnum.REJECTED.getCode(), request.getComment()
        );
        record.setFlowType("REJECT");
        record.setIsFinalDecision(1);
        saveRecord(record);
    }
}