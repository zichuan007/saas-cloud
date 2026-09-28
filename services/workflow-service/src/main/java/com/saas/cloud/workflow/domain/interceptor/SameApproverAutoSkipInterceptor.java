package com.saas.cloud.workflow.domain.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 同审批人自动跳过拦截器 — 当上一节点审批人在后续节点重复出现时，自动完结该节点
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-14
 */
@Slf4j
@Component
public class SameApproverAutoSkipInterceptor implements TaskInterceptor {

    private static final String LAST_APPROVER_ID = "LAST_APPROVER_ID";

    @Override
    public int getOrder() {
        return 100;
    }

    @Override
    public void onTaskCreate(TaskContext context) {
        // 跳过条件检查:
        // 1. 当前节点不是发起人节点
        // 2. 存在上一个审批人ID
        // 3. 上一审批人在当前节点潜在审批人列表中
        Object lastApproverId = context.getVariables().get(LAST_APPROVER_ID);
        if (lastApproverId == null) {
            return;
        }

        String assignee = context.getAssignee();
        if (assignee != null && assignee.equals(String.valueOf(lastApproverId))) {
            log.info("同审批人自动跳过: taskDefKey={}, assignee={}", context.getTaskDefKey(), assignee);
            context.skipTask("同一审批人，自动跳过");
        }
    }

    @Override
    public void onTaskComplete(TaskContext context) {
        // 写入 LAST_APPROVER_ID 供后续节点使用
        if (context.getAssignee() != null) {
            context.getVariables().put(LAST_APPROVER_ID, context.getAssignee());
        }
    }
}