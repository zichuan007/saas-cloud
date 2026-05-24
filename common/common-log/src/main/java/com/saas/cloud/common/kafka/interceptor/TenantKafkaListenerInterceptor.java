package com.saas.cloud.common.kafka.interceptor;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.RecordInterceptor;

import com.saas.cloud.common.security.context.TenantContext;

import lombok.extern.slf4j.Slf4j;

/**
 * Kafka Listener 级别的 RecordInterceptor：
 * 在每条消息交给 @KafkaListener 方法前，从 Header 还原租户上下文；
 * 处理完成后自动清理 TenantContext。
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
@Slf4j
public class TenantKafkaListenerInterceptor implements RecordInterceptor<Object, Object> {

    @Override
    public ConsumerRecord<Object, Object> intercept(ConsumerRecord<Object, Object> record,
                                                     Consumer<Object, Object> consumer) {
        TenantKafkaConsumerInterceptor.extractTenantFromHeaders(record.headers());
        Long tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            log.debug("[Kafka] 消费消息设置租户上下文: tenantId={}, topic={}", tenantId, record.topic());
        }
        return record;
    }

    @Override
    public void afterRecord(ConsumerRecord<Object, Object> record, Consumer<Object, Object> consumer) {
        TenantContext.clear();
    }
}
