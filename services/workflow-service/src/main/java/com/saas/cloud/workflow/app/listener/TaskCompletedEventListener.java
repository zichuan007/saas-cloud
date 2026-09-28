package com.saas.cloud.workflow.app.listener;

import com.saas.cloud.workflow.api.event.TaskCompletedEvent;
import com.saas.cloud.workflow.domain.interceptor.TaskContext;
import com.saas.cloud.workflow.domain.interceptor.TaskInterceptorChain;
import com.saas.cloud.workflow.domain.gateway.ProcessEngineGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 任务完成事件监听器
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-14
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class TaskCompletedEventListener {

    private final TaskInterceptorChain interceptorChain;
    private final ProcessEngineGateway processEngine;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onTaskCompleted(TaskCompletedEvent event) {
        log.debug("处理任务完成事件: taskId={}, taskDefKey={}", event.getTaskId(), event.getTaskDefKey());

        // 构建拦截器上下文并执行拦截链
        TaskContext context = new TaskContext();
        context.setTaskId(event.getTaskId());
        context.setProcessInstanceId(event.getProcessInstanceId());
        context.setProcessDefKey(event.getProcessDefKey());
        context.setTaskDefKey(event.getTaskDefKey());
        context.setAssignee(event.getAssignee());
        context.setVariables(processEngine.getVariables(event.getProcessInstanceId()));

        interceptorChain.executeOnTaskComplete(context);
    }
}