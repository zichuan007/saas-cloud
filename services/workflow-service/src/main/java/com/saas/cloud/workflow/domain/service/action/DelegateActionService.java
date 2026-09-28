package com.saas.cloud.workflow.domain.service.action;

import com.saas.cloud.workflow.api.enums.ApprovalActionEnum;
import com.saas.cloud.workflow.api.dto.request.DelegateRequest;
import com.saas.cloud.workflow.domain.service.action.support.ApprovalActionSupport;
import com.saas.cloud.workflow.tunnel.dataobject.FlowApprovalRecordDO;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Delegate 审批动作
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-14
 */
@Component
public class DelegateActionService extends ApprovalActionSupport implements ApprovalAction<DelegateRequest> {

    @Override
    public ApprovalActionEnum getActionType() {
        return ApprovalActionEnum.DELEGATE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(DelegateRequest request) {
        Map<String, Object> task = getAndValidateTask(request.getTaskId());
        String taskId = (String) task.get("taskId");
        String processInstanceId = (String) task.get("processInstanceId");

        // 先签收再委派
        claimTaskIfNeeded(task, request.getOperatorId().toString());
        processEngine.delegateTask(taskId, request.getTargetUserId().toString());

        // 记录审批日志
        FlowApprovalRecordDO record = buildRecord(
                processInstanceId, taskId,
                (String) task.get("taskDefinitionKey"),
                (String) task.get("taskName"),
                request.getOperatorId(), request.getOperatorName(),
                ApprovalActionEnum.DELEGATE.getCode(), request.getComment()
        );
        saveRecord(record);

        saveTaskRelation(processInstanceId, taskId, null, "DELEGATE",
                request.getOperatorId(), request.getOperatorName(),
                request.getTargetUserId(), null);
    }
}