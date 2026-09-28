package com.saas.cloud.workflow.domain.service.action;

import com.saas.cloud.workflow.api.enums.ApprovalActionEnum;
import com.saas.cloud.workflow.api.dto.request.RecallRequest;
import com.saas.cloud.workflow.domain.service.action.support.ApprovalActionSupport;
import com.saas.cloud.workflow.tunnel.dataobject.FlowApprovalRecordDO;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Recall 审批动作
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-14
 */
@Component
public class RecallActionService extends ApprovalActionSupport implements ApprovalAction<RecallRequest> {

    @Override
    public ApprovalActionEnum getActionType() {
        return ApprovalActionEnum.RECALL;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(RecallRequest request) {
        Map<String, Object> task = getAndValidateTask(request.getTaskId());
        String taskId = (String) task.get("taskId");
        String processInstanceId = (String) task.get("processInstanceId");

        // 前置校验: 下游任务已签收则无法拿回
        String assignee = (String) task.get("assignee");
        if (assignee != null && !assignee.isEmpty()) {
            throw new IllegalStateException("下游任务已被签收，无法拿回");
        }

        // 跳回当前节点
        processEngine.moveActivityIdTo(request.getCurrentActivityId(), request.getTargetActivityId());

        // 记录审批日志
        FlowApprovalRecordDO record = buildRecord(
                processInstanceId, taskId,
                (String) task.get("taskDefinitionKey"),
                (String) task.get("taskName"),
                request.getOperatorId(), request.getOperatorName(),
                ApprovalActionEnum.RECALL.getCode(), "拿回任务"
        );
        record.setSourceNodeKey(request.getCurrentActivityId());
        record.setTargetNodeKey(request.getTargetActivityId());
        saveRecord(record);
    }
}