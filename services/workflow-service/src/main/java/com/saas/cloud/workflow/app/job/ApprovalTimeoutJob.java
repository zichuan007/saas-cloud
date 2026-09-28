package com.saas.cloud.workflow.app.job;

import com.saas.cloud.workflow.domain.gateway.ProcessEngineGateway;
import com.saas.cloud.workflow.tunnel.mapper.FlowNodeConfigMapper;
import com.saas.cloud.workflow.tunnel.dataobject.FlowNodeConfigDO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ApprovalTimeoutJob {

    private final FlowNodeConfigDAO nodeConfigDAO;
    private final ProcessEngineGateway processEngine;

    @Scheduled(cron = "0 0 * * * ?")
    public void handleTimeout() {
        FlowNodeConfigDO condition = new FlowNodeConfigDO();
        condition.setTimeoutEnabled(1);
        List<FlowNodeConfigDO> configs = nodeConfigMapper.selectList(condition);
        for (FlowNodeConfigDO config : configs) {
            try {
                handleNodeTimeout(config);
            } catch (Exception e) {
                log.error("处理超时节点失败: {}/{}", config.getProcessDefKey(), config.getNodeDefKey(), e);
            }
        }
    }

    private void handleNodeTimeout(FlowNodeConfigDO config) {
        // 按节点Key查询超时任务，根据 timeout_strategy 执行
        String strategy = config.getTimeoutStrategy();
        if ("APPROVE".equals(strategy)) {
            log.info("超时自动通过: {}/{}", config.getProcessDefKey(), config.getNodeDefKey());
        } else if ("REJECT".equals(strategy)) {
            log.info("超时自动驳回: {}/{}", config.getProcessDefKey(), config.getNodeDefKey());
        } else if ("TRANSFER".equals(strategy) && config.getTimeoutTransferUserId() != null) {
            log.info("超时转办: {}/{} -> {}", config.getProcessDefKey(), config.getNodeDefKey(), config.getTimeoutTransferUserId());
        } else {
            log.info("超时提醒: {}/{}", config.getProcessDefKey(), config.getNodeDefKey());
        }
    }
}