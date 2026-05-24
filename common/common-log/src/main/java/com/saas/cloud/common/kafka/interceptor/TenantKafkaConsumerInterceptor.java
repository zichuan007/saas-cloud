package com.saas.cloud.common.kafka.interceptor;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerInterceptor;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;

import com.saas.cloud.common.security.context.TenantContext;

/**
 * Kafka Consumer 拦截器：从消息 Header 中还原 tenantId 到 TenantContext
 * <p>注意：此拦截器在 poll() 返回后、消息交给 Listener 前生效。
 * 由于 ConsumerInterceptor 的 onConsume 是批量处理，
 * 实际的逐条租户设置建议配合 RecordInterceptor 使用。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
public class TenantKafkaConsumerInterceptor implements ConsumerInterceptor<Object, Object> {

    @Override
    public ConsumerRecords<Object, Object> onConsume(ConsumerRecords<Object, Object> records) {
        return records;
    }

    @Override
    public void onCommit(Map<TopicPartition, OffsetAndMetadata> offsets) {
    }

    @Override
    public void close() {
    }

    @Override
    public void configure(Map<String, ?> configs) {
    }

    /**
     * 从 Kafka 消息 Header 中提取 tenantId 并设置到 TenantContext
     *
     * @param headers Kafka 消息 Header
     */
    public static void extractTenantFromHeaders(Iterable<Header> headers) {
        if (headers == null) {
            return;
        }
        for (Header header : headers) {
            if (TenantKafkaProducerInterceptor.HEADER_TENANT_ID.equals(header.key())) {
                String tenantIdStr = new String(header.value(), StandardCharsets.UTF_8);
                try {
                    Long tenantId = Long.parseLong(tenantIdStr);
                    TenantContext.TenantInfo info = new TenantContext.TenantInfo();
                    info.setTenantId(tenantId);
                    TenantContext.set(info);
                } catch (NumberFormatException ignored) {
                }
                break;
            }
        }
    }
}
