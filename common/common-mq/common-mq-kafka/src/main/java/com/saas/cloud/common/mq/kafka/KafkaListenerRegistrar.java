package com.saas.cloud.common.mq.kafka;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.util.ClassUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.cloud.common.mq.annotation.MqConsumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Kafka 消费者注册表
 * <p>启动时扫描所有 {@link com.saas.cloud.common.mq.MessageListener} Bean，
 * 对标注 {@link MqConsumer} 的，按 topic/group/concurrency 构建
 * {@link ConcurrentMessageListenerContainer}，挂载 {@link KafkaMessageListenerAdapter}
 * 替代 Spring 的 @KafkaListener，实现跨 MQ 统一消费。</p>
 *
 * <p>复用容器已有的 {@link ConsumerFactory} 与 {@link CommonErrorHandler}（死信/重试），
 * 行为与原 @KafkaListener 等价。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-24
 */
@Slf4j
@RequiredArgsConstructor
public class KafkaListenerRegistrar implements SmartInitializingSingleton, DisposableBean {

    private final ConsumerFactory<Object, Object> consumerFactory;

    private final ObjectProvider<CommonErrorHandler> errorHandlerProvider;

    private final ObjectProvider<ObjectMapper> objectMapperProvider;

    private final ApplicationContext applicationContext;

    private final List<ConcurrentMessageListenerContainer<Object, Object>> containers = new ArrayList<>();

    @Override
    public void afterSingletonsInstantiated() {
        ObjectMapper objectMapper = objectMapperProvider.getObject();
        Map<String, com.saas.cloud.common.mq.MessageListener> beans =
                applicationContext.getBeansOfType(com.saas.cloud.common.mq.MessageListener.class);
        for (Map.Entry<String, com.saas.cloud.common.mq.MessageListener> entry : beans.entrySet()) {
            com.saas.cloud.common.mq.MessageListener<?> biz = entry.getValue();
            Class<?> clazz = ClassUtils.getUserClass(biz.getClass());
            MqConsumer meta = clazz.getAnnotation(MqConsumer.class);
            if (meta == null) {
                continue;
            }
            register(biz, meta, objectMapper);
        }
    }

    /**
     * 注册单个消费者容器
     *
     * @param bizListener  业务监听器
     * @param meta         @MqConsumer 元数据
     * @param objectMapper JSON 反序列化
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void register(com.saas.cloud.common.mq.MessageListener<?> bizListener, MqConsumer meta,
                          ObjectMapper objectMapper) {
        ContainerProperties props = new ContainerProperties(meta.topic());
        // 广播模式每实例随机 group（收全量）；否则按注解 group 竞争消费
        props.setGroupId(meta.broadcast() ? UUID.randomUUID().toString() : meta.group());
        props.setMessageListener(new KafkaMessageListenerAdapter(bizListener, meta, objectMapper));
        ConcurrentMessageListenerContainer<Object, Object> container =
                new ConcurrentMessageListenerContainer<>(consumerFactory, props);
        if (meta.concurrency() > 0) {
            container.setConcurrency(meta.concurrency());
        }
        CommonErrorHandler errorHandler = errorHandlerProvider.getIfAvailable();
        if (errorHandler != null) {
            container.setCommonErrorHandler(errorHandler);
        }
        container.setBeanName("mq-kafka-" + meta.topic() + "-" + meta.group());
        container.start();
        containers.add(container);
        log.info("[MQ-Kafka] 注册消费者 topic={}, group={}, concurrency={}",
                meta.topic(), meta.group(), meta.concurrency());
    }

    @Override
    public void destroy() {
        for (ConcurrentMessageListenerContainer<Object, Object> container : containers) {
            try {
                container.stop();
            } catch (Exception e) {
                log.warn("[MQ-Kafka] 停止容器失败: {}", e.getMessage());
            }
        }
    }

    /**
     * 仅供测试/诊断：当前管理的容器数
     *
     * @return 容器数
     */
    @SuppressWarnings("unused")
    public int containerCount() {
        return containers.size();
    }
}
