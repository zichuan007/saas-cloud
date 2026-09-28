package com.saas.cloud.workflow.domain.service.action;

import com.saas.cloud.workflow.api.enums.ApprovalActionEnum;
import com.saas.cloud.workflow.api.dto.request.CancelRequest;
import com.saas.cloud.workflow.domain.service.action.support.ApprovalActionSupport;
import com.saas.cloud.workflow.tunnel.dataobject.FlowApprovalRecordDO;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Cancel 审批动作
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-14
 */
@Component
public class CancelActionService extends ApprovalActionSupport implements ApprovalAction<CancelRequest> {

    @Override
    public ApprovalActionEnum getActionType() {
        return ApprovalActionEnum.CANCEL;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(CancelRequest request) {
        // 校验仅发起人可撤销
        processEngine.deleteProcessInstance(request.getProcessInstanceId(), request.getReason());

        // 记录审批日志
        FlowApprovalRecordDO record = buildRecord(
                request.getProcessInstanceId(), null, null, null,
                request.getOperatorId(), request.getOperatorName(),
                ApprovalActionEnum.CANCEL.getCode(), request.getReason()
        );
        record.setIsFinalDecision(1);
        saveRecord(record);
    }
}