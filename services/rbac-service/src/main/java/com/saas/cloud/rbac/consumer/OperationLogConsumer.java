package com.saas.cloud.rbac.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.cloud.common.data.tenant.annotation.TenantIgnore;
import com.saas.cloud.common.log.event.OperationLogEvent;
import com.saas.cloud.common.mq.MessageConsumer;
import com.saas.cloud.common.mq.MessageEnvelope;
import com.saas.cloud.common.mq.MessageListener;
import com.saas.cloud.common.mq.MqConst;
import com.saas.cloud.common.mq.annotation.MqConsumer;
import com.saas.cloud.common.mq.annotation.MqIdempotent;
import com.saas.cloud.rbac.entity.OperationLog;
import com.saas.cloud.rbac.mapper.OperationLogMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 操作日志消费者（原 @KafkaListener，现 @MqConsumer 统一消费）
 * <p>消费操作日志事件并写入 sys_operation_log 表，跨租户审计（@TenantIgnore）。
 * 异常向上抛出，由容器错误处理器重试 3 次后投递死信队列，避免审计日志静默丢失。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@MqConsumer(topic = MqConst.TOPIC_OPERATION_LOG, group = "rbac-service")
public class OperationLogConsumer implements MessageListener<OperationLogEvent> {

    private final OperationLogMapper operationLogMapper;

    private final ObjectMapper objectMapper;

    /**
     * 负载类型，供适配器反序列化
     *
     * @return OperationLogEvent
     */
    @Override
    public Class<OperationLogEvent> payloadType() {
        return OperationLogEvent.class;
    }

    /**
     * 消费操作日志事件
     *
     * @param msg 消息信封（data 已反序列化为 OperationLogEvent）
     * @param ctx 消费上下文
     */
    @Override
    @TenantIgnore
    @MqIdempotent
    public void onMessage(MessageEnvelope<OperationLogEvent> msg, MessageConsumer ctx) {
        OperationLogEvent event = msg.getData();
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
