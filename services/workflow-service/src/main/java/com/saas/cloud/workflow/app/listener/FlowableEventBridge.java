package com.saas.cloud.workflow.app.listener;

import com.saas.cloud.workflow.api.event.ProcessCompletedEvent;
import com.saas.cloud.workflow.api.event.TaskCompletedEvent;
import com.saas.cloud.workflow.api.event.TaskCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Flowable 事件桥接器 — 将 Flowable 引擎事件转换为 Spring ApplicationEvent
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-14
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class FlowableEventBridge implements FlowableEventListener {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void onEvent(FlowableEvent event) {
        if (!(event instanceof FlowableEngineEntityEvent entityEvent)) {
            return;
        }

        switch (event.getType()) {
            case TASK_CREATED -> {
                if (entityEvent.getEntity() instanceof Task task) {
                    TaskCreatedEvent springEvent = TaskCreatedEvent.builder()
                            .taskId(task.getId())
                            .processInstanceId(task.getProcessInstanceId())
                            .processDefKey(task.getProcessDefinitionId())
                            .taskDefKey(task.getTaskDefinitionKey())
                            .taskName(task.getName())
                            .assignee(task.getAssignee())
                            .build();
                    eventPublisher.publishEvent(springEvent);
                    log.debug("发布 TaskCreatedEvent: taskId={}", task.getId());
                }
            }
            case TASK_COMPLETED -> {
                if (entityEvent.getEntity() instanceof org.flowable.task.api.Task task) {
                    TaskCompletedEvent springEvent = TaskCompletedEvent.builder()
                            .taskId(task.getId())
                            .processInstanceId(task.getProcessInstanceId())
                            .processDefKey(task.getProcessDefinitionId())
                            .taskDefKey(task.getTaskDefinitionKey())
                            .taskName(task.getName())
                            .assignee(task.getAssignee())
                            .build();
                    eventPublisher.publishEvent(springEvent);
                    log.debug("发布 TaskCompletedEvent: taskId={}", task.getId());
                }
            }
            case PROCESS_COMPLETED -> {
                String processInstanceId = entityEvent.getProcessInstanceId();
                ProcessCompletedEvent springEvent = ProcessCompletedEvent.builder()
                        .processInstanceId(processInstanceId)
                        .build();
                eventPublisher.publishEvent(springEvent);
                log.debug("发布 ProcessCompletedEvent: processInstanceId={}", processInstanceId);
            }
            default -> {
                // 忽略其他事件类型
            }
        }
    }

    @Override
    public boolean isFailOnException() {
        // 异常阻断 Flowable 引擎，保证事务一致性
        return true;
    }

    @Override
    public boolean isFireOnTransactionLifecycleEvent() {
        return false;
    }

    @Override
    public String getOnTransaction() {
        return null;
    }
}