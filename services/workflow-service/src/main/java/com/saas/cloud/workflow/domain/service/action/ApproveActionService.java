package com.saas.cloud.workflow.domain.service.action;

import com.saas.cloud.workflow.api.enums.ApprovalActionEnum;
import com.saas.cloud.workflow.api.dto.request.ApproveRequest;
import com.saas.cloud.workflow.domain.service.action.support.ApprovalActionSupport;
import com.saas.cloud.workflow.tunnel.dataobject.FlowApprovalRecordDO;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Approve 审批动作
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-14
 */
@Component
public class ApproveActionService extends ApprovalActionSupport implements ApprovalAction<ApproveRequest> {

    @Override
    public ApprovalActionEnum getActionType() {
        return ApprovalActionEnum.APPROVED;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(ApproveRequest request) {
        Map<String, Object> task = getAndValidateTask(request.getTaskId());
        String taskId = (String) task.get("taskId");
        String processInstanceId = (String) task.get("processInstanceId");
        String taskDefKey = (String) task.get("taskDefinitionKey");

        // 按需签收
        claimTaskIfNeeded(task, request.getOperatorId().toString());

        // 服务端强制覆盖路由变量，防止客户端伪造
        Map<String, Object> variables = new java.util.HashMap<>();
        variables.put("RESULT", "PASS");
        variables.put("ACTION", "APPROVED");

        // 完成 Flowable 任务
        processEngine.completeTask(taskId, variables);

        // 记录审批日志
        FlowApprovalRecordDO record = buildRecord(
                processInstanceId, taskId, taskDefKey,
                (String) task.get("taskName"),
                request.getOperatorId(), request.getOperatorName(),
                ApprovalActionEnum.APPROVED.getCode(), request.getComment()
        );
        record.setApproveStrategy("ANY");
        record.setIsFinalDecision(1);
        saveRecord(record);
    }
}