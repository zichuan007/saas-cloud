package com.saas.cloud.common.mq;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * 统一 MQ 配置
 * <p>对应 yaml {@code saas.mq} 前缀，控制底座类型与可靠性模式开关。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-24
 */
@Data
@ConfigurationProperties(prefix = "saas.mq")
public class MqProperties {

    /** 底座类型，缺省 Kafka */
    private MQType type = MQType.KAFKA;

    /** Outbox 生产可靠性配置 */
    private Outbox outbox = new Outbox();

    /** 消费幂等配置 */
    private Idempotent idempotent = new Idempotent();

    /**
     * Outbox 生产可靠性配置
     */
    @Data
    public static class Outbox {
        /** 是否启用 outbox，默认关；启用后 {@code sendReliable} 走本地消息表 + 补偿重试 */
        private boolean enabled = false;
        /** 补偿扫描 cron */
        private String retryCron = "0 */1 * * * ?";
        /** 最大重试次数，超限置 SEND_GIVE_UP */
        private int maxRetry = 10;
        /** 单批扫描条数 */
        private int batchSize = 200;
    }

    /**
     * 消费幂等配置
     */
    @Data
    public static class Idempotent {
        /** 是否启用消费幂等，默认关；启用前需建 mq_consume_log 表 */
        private boolean enabled = false;
    }
}
