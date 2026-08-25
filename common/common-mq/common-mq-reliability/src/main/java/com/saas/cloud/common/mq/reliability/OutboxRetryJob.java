package com.saas.cloud.common.mq.reliability;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.saas.cloud.common.mq.MessageEnvelope;
import com.saas.cloud.common.mq.MessageSender;
import com.saas.cloud.common.mq.MqProperties;
import com.saas.cloud.common.mq.SendStatus;
import com.saas.cloud.common.mq.reliability.entity.MqOutbox;
import com.saas.cloud.common.mq.reliability.enums.OutboxMsgStatus;
import com.saas.cloud.common.mq.reliability.mapper.MqOutboxMapper;
import com.saas.cloud.common.security.context.TenantContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Outbox 补偿重试 Job
 * <p>定时扫描 {@code mq_outbox} 中 INIT/SEND_FAIL 且到期的消息重投，超限置 SEND_GIVE_UP。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-24
 */
@Slf4j
@RequiredArgsConstructor
public class OutboxRetryJob {

    private final MessageSender delegate;

    private final MqOutboxMapper outboxMapper;

    private final OutboxMessageSender outboxMessageSender;

    private final MqProperties properties;

    /**
     * 补偿扫描，cron 由 {@code saas.mq.outbox.retry-cron} 配置
     */
    @Scheduled(cron = "${saas.mq.outbox.retry-cron:0 */1 * * * ?}")
    public void retry() {
        int batchSize = properties.getOutbox().getBatchSize();
        List<Integer> pendingStatus = Arrays.asList(
                OutboxMsgStatus.INIT.getCode(), OutboxMsgStatus.SEND_FAIL.getCode());
        LambdaQueryWrapper<MqOutbox> wrapper = new LambdaQueryWrapper<MqOutbox>()
                .in(MqOutbox::getMsgStatus, pendingStatus)
                .and(w -> w.isNull(MqOutbox::getNextRetryTime)
                        .or().le(MqOutbox::getNextRetryTime, LocalDateTime.now()))
                .orderByAsc(MqOutbox::getId)
                .last("LIMIT " + batchSize);
        // mq_outbox 无 tenant_id 列，查询需绕过租户过滤
        List<MqOutbox> list = TenantContext.executeWithoutTenant(() -> outboxMapper.selectList(wrapper));
        if (list.isEmpty()) {
            return;
        }
        log.info("[MQ-Outbox] 补偿扫描命中 {} 条", list.size());
        for (MqOutbox outbox : list) {
            retryOne(outbox);
        }
    }

    /**
     * 重投单条
     *
     * @param outbox outbox 记录
     */
    private void retryOne(MqOutbox outbox) {
        int retryCount = outbox.getRetryCount() == null ? 0 : outbox.getRetryCount();
        MessageEnvelope<String> envelope = MessageEnvelope.<String>builder()
                .msgId(outbox.getMsgId())
                .bizId(outbox.getBizId())
                .topic(outbox.getTopic())
                .msgKey(outbox.getMsgKey())
                .data(outbox.getPayload())
                .build();
        try {
            var result = delegate.send(envelope);
            if (result != null && result.getStatus() == SendStatus.SUCCESS) {
                outboxMessageSender.updateStatus(outbox.getId(), OutboxMsgStatus.SEND_SUCCESS, null);
                log.info("[MQ-Outbox] 重投成功 id={}, msgId={}", outbox.getId(), outbox.getMsgId());
            } else {
                outboxMessageSender.markFail(outbox.getId(), retryCount,
                        result == null ? "unknown" : result.getError());
            }
        } catch (Exception e) {
            outboxMessageSender.markFail(outbox.getId(), retryCount, e.getMessage());
            log.warn("[MQ-Outbox] 重投失败 id={}, msgId={}: {}", outbox.getId(), outbox.getMsgId(), e.getMessage());
        }
    }
}
