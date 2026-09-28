package com.saas.cloud.workflow.app.listener;

import com.saas.cloud.workflow.api.event.ProcessCompletedEvent;
import com.saas.cloud.workflow.domain.gateway.ProcessEngineGateway;
import com.saas.cloud.workflow.tunnel.mapper.FlowProcessInstanceExtMapper;
import com.saas.cloud.workflow.tunnel.dataobject.FlowProcessInstanceExtDO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ProcessCompletedEventListener {

    private final FlowProcessInstanceExtDAO processInstanceExtDAO;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onProcessCompleted(ProcessCompletedEvent event) {
        List<FlowProcessInstanceExtDO> instances = processInstanceExtMapper.selectByProcessInstanceId(event.getProcessInstanceId());
        for (FlowProcessInstanceExtDO instance : instances) {
            instance.setStatus(1); // 已完成
            instance.setEndTime(LocalDateTime.now());
            processInstanceExtMapper.updateById(instance);
        }
        log.info("流程完成: processInstanceId={}", event.getProcessInstanceId());
    }
}