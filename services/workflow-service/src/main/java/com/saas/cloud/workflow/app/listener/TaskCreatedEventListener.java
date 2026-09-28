package com.saas.cloud.workflow.app.listener;

import com.saas.cloud.workflow.api.enums.ApprovalActionEnum;
import com.saas.cloud.workflow.api.event.TaskCreatedEvent;
import com.saas.cloud.workflow.domain.gateway.ProcessEngineGateway;
import com.saas.cloud.workflow.domain.interceptor.TaskContext;
import com.saas.cloud.workflow.domain.interceptor.TaskInterceptorChain;
import com.saas.cloud.workflow.tunnel.mapper.FlowApprovalRecordMapper;
import com.saas.cloud.workflow.tunnel.dataobject.FlowApprovalRecordDO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 任务创建事件监听器
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-14
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class TaskCreatedEventListener {

    private final TaskInterceptorChain interceptorChain;
    private final ProcessEngineGateway processEngine;
    private final FlowApprovalRecordDAO approvalRecordDAO;

    /**
     * 监听 TaskCreatedEvent，与 Flowable 引擎共享事务
     */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onTaskCreated(TaskCreatedEvent event) {
        log.debug("处理任务创建事件: taskId={}, taskDefKey={}", event.getTaskId(), event.getTaskDefKey());

        // 构建拦截器上下文
        TaskContext context = new TaskContext();
        context.setTaskId(event.getTaskId());
        context.setProcessInstanceId(event.getProcessInstanceId());
        context.setProcessDefKey(event.getProcessDefKey());
        context.setTaskDefKey(event.getTaskDefKey());
        context.setAssignee(event.getAssignee());
        context.setInitiatorId(event.getInitiatorId());

        // 加载流程变量
        Map<String, Object> variables = processEngine.getVariables(event.getProcessInstanceId());
        context.setVariables(variables);

        // 执行拦截器链
        interceptorChain.executeOnTaskCreate(context);

        // 若 skipTask=true，则自动完成该任务
        if (context.isSkipTask()) {
            log.info("自动跳过任务: taskId={}, reason={}", event.getTaskId(), context.getSkipReason());
            processEngine.completeTask(event.getTaskId(), null);

            // 记录 AUTO_SKIP 日志
            FlowApprovalRecordDO record = new FlowApprovalRecordDO();
            record.setProcessInstanceId(event.getProcessInstanceId());
            record.setTaskId(event.getTaskId());
            record.setTaskDefKey(event.getTaskDefKey());
            record.setTaskName(event.getTaskName());
            record.setAction(ApprovalActionEnum.AUTO_SKIP.getCode());
            record.setSystemReason(context.getSkipReason());
            record.setOperatorType("SYSTEM");
            record.setActionTime(LocalDateTime.now());
            record.setFlowType("AUTO_SUBMIT");
            record.setIsFinalDecision(0);
            approvalRecordMapper.insert(record);
        }
    }
}