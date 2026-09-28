package com.saas.cloud.workflow.domain.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 流程任务路由拦截器 — 最高优先级，根据 processDefinitionKey 路由到定制处理器
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-14
 */
@Slf4j
@Component
public class ProcessTaskRoutingInterceptor implements TaskInterceptor {

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }

    @Override
    public void onTaskCreate(TaskContext context) {
        // 根据 processDefKey 查找定制处理器
        // 找不到定制处理器时放行，不阻断全局拦截器链
        log.debug("任务路由拦截: processDefKey={}, taskDefKey={}", context.getProcessDefKey(), context.getTaskDefKey());
    }

    @Override
    public void onTaskComplete(TaskContext context) {
        log.debug("任务完成路由: processDefKey={}, taskDefKey={}", context.getProcessDefKey(), context.getTaskDefKey());
    }
}