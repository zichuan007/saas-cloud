package com.saas.cloud.workflow.domain.service.action;

import com.saas.cloud.workflow.api.enums.ApprovalActionEnum;
import com.saas.cloud.workflow.api.dto.request.AddSignRequest;
import com.saas.cloud.workflow.domain.service.action.support.ApprovalActionSupport;
import com.saas.cloud.workflow.tunnel.dataobject.FlowApprovalRecordDO;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * AddSign 审批动作
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-14
 */
@Component
public class AddSignActionService extends ApprovalActionSupport implements ApprovalAction<AddSignRequest> {

    @Override
    public ApprovalActionEnum getActionType() {
        return ApprovalActionEnum.ADD_SIGN;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(AddSignRequest request) {
        Map<String, Object> task = getAndValidateTask(request.getTaskId());
        String taskId = (String) task.get("taskId");
        String processInstanceId = (String) task.get("processInstanceId");

        // 为每个加签人添加候选人
        for (Long userId : request.getUserIds()) {
            processEngine.addCandidateUser(taskId, userId.toString());
        }

        // 记录审批日志
        FlowApprovalRecordDO record = buildRecord(
                processInstanceId, taskId,
                (String) task.get("taskDefinitionKey"),
                (String) task.get("taskName"),
                request.getOperatorId(), request.getOperatorName(),
                ApprovalActionEnum.ADD_SIGN.getCode(), request.getComment()
        );
        saveRecord(record);
    }
}