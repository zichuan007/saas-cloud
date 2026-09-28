package com.saas.cloud.workflow.domain.service.action;

import com.saas.cloud.workflow.api.enums.ApprovalActionEnum;

/**
 * 审批动作策略接口
 *
 * @param <T> 请求 DTO 类型
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-14
 */
public interface ApprovalAction<T> {

    /**
     * 获取动作类型
     */
    ApprovalActionEnum getActionType();

    /**
     * 执行审批动作
     */
    void execute(T request);
}