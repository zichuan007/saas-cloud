package com.saas.cloud.common.mq.reliability;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.cloud.common.mq.MessageEnvelope;
import com.saas.cloud.common.mq.MessageSender;
import com.saas.cloud.common.mq.MqProperties;
import com.saas.cloud.common.mq.SendResult;
import com.saas.cloud.common.mq.SendStatus;
import com.saas.cloud.common.mq.reliability.entity.MqOutbox;
import com.saas.cloud.common.mq.reliability.enums.OutboxMsgStatus;
import com.saas.cloud.common.mq.reliability.mapper.MqOutboxMapper;
import com.saas.cloud.common.security.context.TenantContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Outbox 装饰器：包装底层 {@link MessageSender}，提供生产可靠性
 * <p>{@code send} 直投（低延迟）；{@code sendReliable} 先落 {@code mq_outbox}(INIT) 再实投，
 * 失败置 SEND_FAIL 等补偿 Job 重试，保证至少一次送达。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-24
 */
@Slf4j
@RequiredArgsConstructor
public class OutboxMessageSender implements MessageSender {

    private final MessageSender delegate;

    private final MqOutboxMapper outboxMapper;

    private final ObjectMapper objectMapper;

    private final MqProperties properties;

    @Override
    public <T> SendResult send(MessageEnvelope<T> msg) {
        // 同步直投不经 outbox，保持低延迟
        return delegate.send(msg);
    }

    @Override
    public <T> void sendReliable(MessageEnvelope<T> msg) {
        ensureMsgId(msg);
        MqOutbox outbox = new MqOutbox();
        outbox.setMsgId(msg.getMsgId());
        outbox.setBizId(msg.getBizId());
        outbox.setTopic(msg.getTopic());
        outbox.setMsgKey(msg.getMsgKey());
        outbox.setPayload(serialize(msg.getData()));
        outbox.setMsgStatus(OutboxMsgStatus.INIT.getCode());
        outbox.setRetryCount(0);
        outbox.setNextRetryTime(LocalDateTime.now());
        try {
            // mq_outbox 无 tenant_id 列，落库需绕过租户过滤
            TenantContext.executeWithoutTenant(() -> outboxMapper.insert(outbox));
        } catch (Exception e) {
            // outbox 落库失败则直接实投，降级为至少一次
            log.warn("[MQ-Outbox] 落库失败，降级直投 msgId={}: {}", msg.getMsgId(), e.getMessage());
            delegate.send(msg);
            return;
        }
        try {
            SendResult result = delegate.send(msg);
            if (result != null && result.getStatus() == SendStatus.SUCCESS) {
                updateStatus(outbox.getId(), OutboxMsgStatus.SEND_SUCCESS, null);
            } else {
                markFail(outbox.getId(), 0, result == null ? "unknown" : result.getError());
            }
        } catch (Exception e) {
            markFail(outbox.getId(), 0, e.getMessage());
            log.warn("[MQ-Outbox] 实投失败，等待补偿 msgId={}: {}", msg.getMsgId(), e.getMessage());
        }
    }

    /**
     * 更新状态
     *
     * @param id     outbox 主键
     * @param status 目标状态
     * @param nextRetry 下次重试时间，null 表示无需重试
     */
    public void updateStatus(Long id, OutboxMsgStatus status, LocalDateTime nextRetry) {
        MqOutbox update = new MqOutbox();
        update.setId(id);
        update.setMsgStatus(status.getCode());
        update.setNextRetryTime(nextRetry);
        TenantContext.executeWithoutTenant(() -> outboxMapper.updateById(update));
    }

    /**
     * 标记失败并安排下次重试
     *
     * @param id          outbox 主键
     * @param retryCount  当前重试次数
     * @param error       错误信息
     */
    public void markFail(Long id, int retryCount, String error) {
        int maxRetry = properties.getOutbox().getMaxRetry();
        OutboxMsgStatus status = retryCount >= maxRetry
                ? OutboxMsgStatus.SEND_GIVE_UP : OutboxMsgStatus.SEND_FAIL;
        LocalDateTime nextRetry = status == OutboxMsgStatus.SEND_GIVE_UP
                ? null : LocalDateTime.now().plusSeconds((retryCount + 1) * 10L);
        MqOutbox update = new MqOutbox();
        update.setId(id);
        update.setMsgStatus(status.getCode());
        update.setRetryCount(retryCount + 1);
        update.setNextRetryTime(nextRetry);
        TenantContext.executeWithoutTenant(() -> outboxMapper.updateById(update));
        log.debug("[MQ-Outbox] 标记 id={} status={} retryCount={} error={}",
                id, status.getDesc(), retryCount + 1, error);
    }

    /**
     * 确保信封有 msgId
     *
     * @param msg 信封
     */
    private <T> void ensureMsgId(MessageEnvelope<T> msg) {
        if (msg.getMsgId() == null || msg.getMsgId().isEmpty()) {
            msg.setMsgId(UUID.randomUUID().toString().replace("-", ""));
        }
    }

    /**
     * 序列化 payload，String 直接返回
     *
     * @param data 负载
     * @return JSON 字符串
     */
    private String serialize(Object data) {
        if (data == null) {
            return null;
        }
        if (data instanceof String) {
            return (String) data;
        }
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("payload 序列化失败: " + e.getMessage(), e);
        }
    }
}
