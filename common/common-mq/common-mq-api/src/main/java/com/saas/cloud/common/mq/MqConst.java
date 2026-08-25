package com.saas.cloud.common.mq;

/**
 * MQ 公共常量
 * <p>收敛 topic 命名与租户头 key，废弃散落在 {@code KafkaConfig} / 各消费者中的硬编码。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-24
 */
public final class MqConst {

    /** 租户头 key，生产者注入、消费者还原 */
    public static final String HEADER_TENANT_ID = "X-Tenant-Id";

    /** 消息唯一 ID 头 key，跨 MQ 透传，用于消费幂等 */
    public static final String HEADER_MSG_ID = "X-Msg-Id";

    /** 业务标识头 key */
    public static final String HEADER_BIZ_ID = "X-Biz-Id";

    // ===== Topic 常量（保持与原 KafkaConfig 一致，确保迁移期兼容） =====

    /** 操作日志主题 */
    public static final String TOPIC_OPERATION_LOG = "saas-operation-log";

    /** 通知事件主题 */
    public static final String TOPIC_NOTIFY_EVENT = "saas-notify-event";

    /** API 访问日志主题 */
    public static final String TOPIC_API_ACCESS_LOG = "saas-api-access-log";

    /** API 错误日志主题 */
    public static final String TOPIC_API_ERROR_LOG = "saas-api-error-log";

    /** WebSocket 集群广播主题 */
    public static final String TOPIC_WEBSOCKET_BROADCAST = "saas-websocket-broadcast";

    private MqConst() {
    }
}
