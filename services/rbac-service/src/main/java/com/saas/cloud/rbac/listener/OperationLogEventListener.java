package com.saas.cloud.rbac.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.saas.cloud.common.log.event.OperationLogEvent;
import com.saas.cloud.rbac.entity.OperationLog;
import com.saas.cloud.rbac.mapper.OperationLogMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 操作日志 Spring 事件监听器
 * 当 Kafka 不可用时，OperationLogAspect 通过 Spring 事件同步发布日志，
 * 本监听器接收事件并直接写入 sys_operation_log 表。
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-19
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class OperationLogEventListener {

    private final OperationLogMapper operationLogMapper;

    /**
     * 监听操作日志事件并入库
     *
     * @param event 操作日志事件
     */
    @EventListener
    public void onOperationLog(OperationLogEvent event) {
        try {
            OperationLog entity = new OperationLog();
            entity.setUserId(event.getUserId());
            entity.setUsername(event.getUsername());
            entity.setTenantId(event.getTenantId());
            entity.setModule(event.getModule());
            entity.setOperation(event.getOperation());
            entity.setMethod(event.getMethod());
            entity.setRequestUrl(event.getRequestUrl());
            entity.setRequestMethod(event.getRequestMethod());
            entity.setRequestParams(event.getRequestParams());
            entity.setResponseCode(event.getResponseCode());
            entity.setErrorMsg(event.getErrorMsg());
            entity.setIp(event.getIp());
            entity.setUserAgent(event.getUserAgent());
            entity.setDuration(event.getDuration());
            operationLogMapper.insert(entity);
            log.debug("[操作日志] 同步入库成功: module={}, operation={}, userId={}",
                    event.getModule(), event.getOperation(), event.getUserId());
        } catch (Exception e) {
            log.error("[操作日志] 同步入库失败: {}", e.getMessage(), e);
        }
    }
}
