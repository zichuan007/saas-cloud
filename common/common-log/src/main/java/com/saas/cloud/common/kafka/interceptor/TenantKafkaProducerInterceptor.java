package com.saas.cloud.common.kafka.interceptor;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import com.saas.cloud.common.security.context.TenantContext;

/**
 * Kafka Producer 拦截器：自动将 TenantContext 中的 tenantId 写入消息 Header
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
public class TenantKafkaProducerInterceptor implements ProducerInterceptor<Object, Object> {

    public static final String HEADER_TENANT_ID = "X-Tenant-Id";

    @Override
    public ProducerRecord<Object, Object> onSend(ProducerRecord<Object, Object> record) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            record.headers().add(HEADER_TENANT_ID,
                    tenantId.toString().getBytes(StandardCharsets.UTF_8));
        }
        return record;
    }

    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
    }

    @Override
    public void close() {
    }

    @Override
    public void configure(Map<String, ?> configs) {
    }
}
