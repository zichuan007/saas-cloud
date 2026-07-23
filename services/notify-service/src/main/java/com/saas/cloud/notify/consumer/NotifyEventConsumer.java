package com.saas.cloud.notify.consumer;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.cloud.common.kafka.config.KafkaConfig;
import com.saas.cloud.common.security.context.TenantContext;
import com.saas.cloud.notify.api.enums.NotifyChannelType;
import com.saas.cloud.notify.api.event.NotifyEvent;
import com.saas.cloud.notify.entity.NotifyChannelConfig;
import com.saas.cloud.notify.sender.DingtalkMessageBuilder;
import com.saas.cloud.notify.sender.EmailSender;
import com.saas.cloud.notify.sender.FeishuMessageBuilder;
import com.saas.cloud.notify.sender.WebhookSender;
import com.saas.cloud.notify.sender.WecomMessageBuilder;
import com.saas.cloud.notify.service.INotifyChannelConfigService;
import com.saas.cloud.notify.service.INotifyMessageService;
import com.saas.cloud.notify.service.ISmsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 通知事件 Kafka 消费者
 * <p>监听通知事件 Topic，创建站内消息并根据租户渠道配置路由到邮件/飞书/钉钉/企微等渠道。</p>
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
    private final INotifyChannelConfigService channelConfigService;
    private final ISmsService smsService;
    private final EmailSender emailSender;
    private final WebhookSender webhookSender;
    private final FeishuMessageBuilder feishuMessageBuilder;
    private final DingtalkMessageBuilder dingtalkMessageBuilder;
    private final WecomMessageBuilder wecomMessageBuilder;
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

            // 从事件体兜底还原租户上下文（Kafka header 缺失时，避免写库 tenant_id 丢失）
            if (event.getTenantId() != null) {
                TenantContext.TenantInfo info = new TenantContext.TenantInfo();
                info.setTenantId(event.getTenantId());
                TenantContext.set(info);
            }

            // 1. 站内信（必发）
            messageService.createMessage(event);

            // 2. 查询租户启用的渠道配置
            List<NotifyChannelConfig> enabledChannels = channelConfigService.list(
                    new LambdaQueryWrapper<NotifyChannelConfig>()
                            .eq(NotifyChannelConfig::getEnabled, (byte) 1)
            );

            String title = StringUtils.hasText(event.getTitle()) ? event.getTitle() : "系统通知";
            String content = StringUtils.hasText(event.getContent()) ? event.getContent() : "";

            // 3. 按渠道类型分发
            for (NotifyChannelConfig channel : enabledChannels) {
                NotifyChannelType channelType = NotifyChannelType.getByCode(channel.getChannelType());
                if (channelType == null || channelType == NotifyChannelType.STATION_MAIL) {
                    continue;
                }
                try {
                    dispatchToChannel(channelType, channel.getConfigJson(), event, title, content);
                } catch (Exception e) {
                    log.error("[通知中心] 渠道[{}]发送失败", channelType.getDesc(), e);
                }
            }

            // 4. 短信（按事件标记发送）
            if (Boolean.TRUE.equals(event.getSendSms()) && StringUtils.hasText(event.getPhone())) {
                try {
                    if (StringUtils.hasText(event.getTemplateCode())) {
                        smsService.sendSms(event.getPhone(), event.getTemplateCode(), event.getParams());
                    } else if (StringUtils.hasText(content)) {
                        smsService.sendSmsContent(event.getPhone(), content);
                    }
                } catch (Exception smsEx) {
                    log.error("[通知中心] 短信发送失败, phone={}", event.getPhone(), smsEx);
                }
            }
        } catch (Exception e) {
            log.error("[通知中心] 处理通知事件失败, message={}", message, e);
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * 按渠道类型分发消息
     */
    private void dispatchToChannel(NotifyChannelType channelType, String configJson,
                                   NotifyEvent event, String title, String content) throws Exception {
        switch (channelType) {
            case EMAIL:
                if (StringUtils.hasText(event.getEmail())) {
                    emailSender.send(configJson, event.getEmail(), title, content);
                }
                break;
            case FEISHU:
                String feishuWebhook = extractWebhookUrl(configJson);
                if (StringUtils.hasText(feishuWebhook)) {
                    String feishuBody = feishuMessageBuilder.buildTextMessage(title, content);
                    webhookSender.send(feishuWebhook, feishuBody, "飞书");
                }
                break;
            case DINGTALK:
                String dingtalkWebhook = extractWebhookUrl(configJson);
                if (StringUtils.hasText(dingtalkWebhook)) {
                    String dingtalkBody = dingtalkMessageBuilder.buildMarkdownMessage(title, content);
                    webhookSender.send(dingtalkWebhook, dingtalkBody, "钉钉");
                }
                break;
            case WECOM:
                String wecomWebhook = extractWebhookUrl(configJson);
                if (StringUtils.hasText(wecomWebhook)) {
                    String wecomBody = wecomMessageBuilder.buildMarkdownMessage(title, content);
                    webhookSender.send(wecomWebhook, wecomBody, "企业微信");
                }
                break;
            default:
                break;
        }
    }

    /**
     * 从渠道配置 JSON 中提取 webhookUrl
     */
    private String extractWebhookUrl(String configJson) {
        try {
            JsonNode node = objectMapper.readTree(configJson);
            return node.path("webhookUrl").asText(null);
        } catch (Exception e) {
            log.warn("[通知中心] 解析渠道配置JSON失败", e);
            return null;
        }
    }
}
