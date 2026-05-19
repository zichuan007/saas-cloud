package com.saas.cloud.notify.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.cloud.common.kafka.config.KafkaConfig;
import com.saas.cloud.notify.api.event.NotifyEvent;
import com.saas.cloud.notify.service.INotifyMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 通知事件 Kafka 消费者
 * <p>
 * 监听通知事件 Topic，反序列化后调用消息服务创建站内消息。
 * </p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class NotifyEventConsumer {

    private final INotifyMessageService messageService;
    private final ObjectMapper objectMapper;

    /**
     * 消费通知事件消息
     *
     * @param message Kafka 消息（JSON 字符串）
     */
    @KafkaListener(topics = KafkaConfig.TOPIC_NOTIFY_EVENT, groupId = "notify-service")
    public void onMessage(String message) {
        log.info("[通知中心] 收到通知事件消息: {}", message);
        try {
            NotifyEvent event = objectMapper.readValue(message, NotifyEvent.class);
            if (event == null) {
                log.warn("[通知中心] 通知事件反序列化结果为空, message={}", message);
                return;
            }
            if (event.getReceiverId() == null) {
                log.warn("[通知中心] 通知事件缺少接收人ID, message={}", message);
                return;
            }
            messageService.createMessage(event);
        } catch (Exception e) {
            log.error("[通知中心] 处理通知事件失败, message={}", message, e);
        }
    }
}
