package com.saas.cloud.common.mq.reliability;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Outbox 消息达到死信状态事件
 * <p>Outbox 重试达上限置 {@code SEND_GIVE_UP} 时发布，供监听方做告警/转死信队列/人工补偿。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-09-28
 */
@Getter
@AllArgsConstructor
public class MqDeadLetterEvent {

    /** 消息 ID */
    private final String msgId;

    /** 业务 ID */
    private final String bizId;

    /** 主题 */
    private final String topic;

    /** 分区/路由键 */
    private final String msgKey;

    /** 负载（JSON 字符串） */
    private final String payload;

    /** 最终重试次数 */
    private final int retryCount;

    /** 最后一次失败原因 */
    private final String error;

    /** 发生时间 */
    private final LocalDateTime occurTime;
}
