package com.saas.cloud.workflow.domain.service.action;

import com.saas.cloud.workflow.api.enums.ApprovalActionEnum;
import com.saas.cloud.workflow.api.dto.request.TransferRequest;
import com.saas.cloud.workflow.domain.service.action.support.ApprovalActionSupport;
import com.saas.cloud.workflow.tunnel.dataobject.FlowApprovalRecordDO;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Transfer 审批动作
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-14
 */
@Component
public class TransferActionService extends ApprovalActionSupport implements ApprovalAction<TransferRequest> {

    @Override
    public ApprovalActionEnum getActionType() {
        return ApprovalActionEnum.TRANSFER;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(TransferRequest request) {
        Map<String, Object> task = getAndValidateTask(request.getTaskId());
        String taskId = (String) task.get("taskId");
        String processInstanceId = (String) task.get("processInstanceId");

        // 改签: 直接修改处理人
        processEngine.setAssignee(taskId, request.getTargetUserId().toString());

        // 记录审批日志
        FlowApprovalRecordDO record = buildRecord(
                processInstanceId, taskId,
                (String) task.get("taskDefinitionKey"),
                (String) task.get("taskName"),
                request.getOperatorId(), request.getOperatorName(),
                ApprovalActionEnum.TRANSFER.getCode(), request.getComment()
        );
        saveRecord(record);

        saveTaskRelation(processInstanceId, taskId, null, "TRANSFER",
                request.getOperatorId(), request.getOperatorName(),
                request.getTargetUserId(), null);
    }
}