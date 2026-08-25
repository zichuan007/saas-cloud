package com.saas.cloud.common.mq.rocket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.remoting.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ClassUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.cloud.common.mq.annotation.MqConsumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * RocketMQ 消费者注册表
 * <p>启动时扫描所有 {@link com.saas.cloud.common.mq.MessageListener} Bean，对标注 {@link MqConsumer}
 * 的，构建 {@link DefaultMQPushConsumer}：CLUSTERING 模式同 group 竞争消费（等价 Kafka 消费组），
 * {@link MqConsumer#broadcast()} 为 true 时切换 BROADCASTING（每实例收全量，用于 WebSocket 广播）。
 * nameserver 取自 {@link RocketMQTemplate} 的 producer 或 {@link RocketMQProperties}。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-24
 */
@Slf4j
@RequiredArgsConstructor
public class RocketListenerRegistrar implements SmartInitializingSingleton, DisposableBean {

    private final RocketMQTemplate rocketMQTemplate;

    private final ObjectProvider<RocketMQProperties> rocketMQPropertiesProvider;

    private final ObjectProvider<ObjectMapper> objectMapperProvider;

    private final ApplicationContext applicationContext;

    private final List<DefaultMQPushConsumer> consumers = new ArrayList<>();

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
     * 注册单个消费者
     *
     * @param bizListener  业务监听器
     * @param meta         @MqConsumer 元数据
     * @param objectMapper JSON 反序列化
     */
    private void register(com.saas.cloud.common.mq.MessageListener<?> bizListener, MqConsumer meta,
                          ObjectMapper objectMapper) {
        String group = meta.group();
        String topic = meta.topic();
        try {
            DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(group);
            String nameserver = resolveNameserver();
            if (nameserver != null && !nameserver.isEmpty()) {
                consumer.setNamesrvAddr(nameserver);
            }
            consumer.subscribe(topic, "*");
            consumer.setMessageModel(meta.broadcast()
                    ? MessageModel.BROADCASTING : MessageModel.CLUSTERING);
            if (meta.concurrency() > 0) {
                consumer.setConsumeThreadMin(meta.concurrency());
            }
            consumer.registerMessageListener(
                    new RocketMessageListenerAdapter(bizListener, meta, objectMapper));
            consumer.setConsumerGroup(group);
            consumer.start();
            consumers.add(consumer);
            log.info("[MQ-Rocket] 注册消费者 topic={}, group={}, broadcast={}", topic, group, meta.broadcast());
        } catch (Exception e) {
            log.error("[MQ-Rocket] 注册消费者失败 topic={}, group={}: {}", topic, group, e.getMessage(), e);
        }
    }

    /**
     * 解析 nameserver：优先 producer 已配的，次取 RocketMQProperties
     *
     * @return nameserver 地址
     */
    private String resolveNameserver() {
        try {
            String addr = rocketMQTemplate.getProducer().getNamesrvAddr();
            if (addr != null && !addr.isEmpty()) {
                return addr;
            }
        } catch (Exception ignored) {
            // producer 尚未就绪时回退到 properties
        }
        RocketMQProperties props = rocketMQPropertiesProvider.getIfAvailable();
        return props == null ? null : props.getNameServer();
    }

    @Override
    public void destroy() {
        for (DefaultMQPushConsumer consumer : consumers) {
            try {
                consumer.shutdown();
            } catch (Exception e) {
                log.warn("[MQ-Rocket] 关停消费者失败: {}", e.getMessage());
            }
        }
    }
}
