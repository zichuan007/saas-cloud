package com.saas.cloud.workflow.domain.service.action;

import com.saas.cloud.workflow.api.enums.ApprovalActionEnum;
import com.saas.cloud.workflow.api.dto.request.UrgeRequest;
import com.saas.cloud.workflow.domain.service.action.support.ApprovalActionSupport;
import com.saas.cloud.workflow.tunnel.dataobject.FlowApprovalRecordDO;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Urge 审批动作（催办）
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-14
 */
@Component
public class UrgeActionService extends ApprovalActionSupport implements ApprovalAction<UrgeRequest> {

    @Override
    public ApprovalActionEnum getActionType() {
        return ApprovalActionEnum.URGE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(UrgeRequest request) {
        Map<String, Object> task = getAndValidateTask(request.getTaskId());
        String taskId = (String) task.get("taskId");
        String processInstanceId = (String) task.get("processInstanceId");

        // 催办: 记录催办日志，发送通知
        List<String> targetUsers = new ArrayList<>();
        String assignee = (String) task.get("assignee");
        if (assignee != null && !assignee.isEmpty()) {
            targetUsers.add(assignee);
        }

        // 记录审批日志
        FlowApprovalRecordDO record = buildRecord(
                processInstanceId, taskId,
                (String) task.get("taskDefinitionKey"),
                (String) task.get("taskName"),
                request.getOperatorId(), request.getOperatorName(),
                ApprovalActionEnum.URGE.getCode(), "催办"
        );
        saveRecord(record);

        for (String targetUserId : targetUsers) {
            saveUrgeLog(processInstanceId, taskId,
                    request.getOperatorId(), request.getOperatorName(),
                    Long.parseLong(targetUserId), null);
        }
    }
}