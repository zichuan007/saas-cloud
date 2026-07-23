package com.saas.cloud.rbac.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.cloud.common.kafka.config.KafkaConfig;
import com.saas.cloud.common.data.tenant.annotation.TenantIgnore;
import com.saas.cloud.common.log.event.OperationLogEvent;
import com.saas.cloud.rbac.entity.OperationLog;
import com.saas.cloud.rbac.mapper.OperationLogMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 操作日志 Kafka 消费者
 * 消费 Kafka 操作日志事件并写入 sys_operation_log 表
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class OperationLogConsumer {

    private final OperationLogMapper operationLogMapper;
    private final ObjectMapper objectMapper;

    /**
     * 消费操作日志事件
     * <p>异常向上抛出，由 DefaultErrorHandler 重试 3 次后投递死信队列，避免审计日志静默丢失。</p>
     *
     * @param message Kafka 消息（JSON 字符串）
     */
    @KafkaListener(topics = KafkaConfig.TOPIC_OPERATION_LOG, groupId = "rbac-service")
    @TenantIgnore
    public void onMessage(String message) throws Exception {
        OperationLogEvent event = objectMapper.readValue(message, OperationLogEvent.class);
        OperationLog entity = convertToEntity(event);
        operationLogMapper.insert(entity);
        log.debug("[操作日志消费] 入库成功: module={}, operation={}, userId={}",
                event.getModule(), event.getOperation(), event.getUserId());
    }

    /**
     * 将事件 DTO 转换为数据库实体
     *
     * @param event 操作日志事件
     * @return 操作日志实体
     */
    private OperationLog convertToEntity(OperationLogEvent event) {
        OperationLog entity = new OperationLog();
        entity.setUserId(event.getUserId());
        entity.setUsername(event.getUsername());
        entity.setTenantId(event.getTenantId());
        entity.setModule(event.getModule());
        entity.setOperation(event.getOperation());
        entity.setOperateType(event.getOperateType());
        entity.setMethod(event.getMethod());
        entity.setRequestUrl(event.getRequestUrl());
        entity.setRequestMethod(event.getRequestMethod());
        entity.setRequestParams(event.getRequestParams());
        entity.setChangeDiff(event.getChangeDiff());
        entity.setResponseCode(event.getResponseCode());
        entity.setErrorMsg(event.getErrorMsg());
        entity.setIp(event.getIp());
        entity.setLocation(event.getLocation());
        entity.setUserAgent(event.getUserAgent());
        entity.setDuration(event.getDuration());
        return entity;
    }
}
