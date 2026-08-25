package com.saas.cloud.common.mq.rabbit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ClassUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.cloud.common.mq.annotation.MqConsumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * RabbitMQ 消费者注册表
 * <p>启动时扫描所有 {@link com.saas.cloud.common.mq.MessageListener} Bean，对标注 {@link MqConsumer}
 * 的，按 topic 声明 durable 队列并创建 {@link SimpleMessageListenerContainer}，挂载
 * {@link RabbitMessageListenerAdapter}。多实例共享同名队列实现竞争消费（等价 Kafka 消费组）。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-24
 */
@Slf4j
@RequiredArgsConstructor
public class RabbitListenerRegistrar implements SmartInitializingSingleton, DisposableBean {

    private final ConnectionFactory connectionFactory;

    private final ObjectProvider<RabbitAdmin> rabbitAdminProvider;

    private final ObjectProvider<ObjectMapper> objectMapperProvider;

    private final ApplicationContext applicationContext;

    private final List<SimpleMessageListenerContainer> containers = new ArrayList<>();

    /** 已声明队列缓存 */
    private final Set<String> declaredQueues = ConcurrentHashMap.newKeySet();

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
    private void register(com.saas.cloud.common.mq.MessageListener<?> bizListener, MqConsumer meta,
                          ObjectMapper objectMapper) {
        String topic = meta.topic();
        // 广播模式每实例独立队列（收全量）；否则共享同名队列竞争消费
        String queue = meta.broadcast() ? topic + "-" + UUID.randomUUID() : topic;
        declareQueueIfNeeded(queue);
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        container.setQueueNames(queue);
        container.setMessageListener(new RabbitMessageListenerAdapter(bizListener, meta, objectMapper));
        if (meta.concurrency() > 0) {
            container.setConcurrentConsumers(meta.concurrency());
        }
        container.setMissingQueuesFatal(false);
        container.setBeanName("mq-rabbit-" + topic);
        container.start();
        containers.add(container);
        log.info("[MQ-Rabbit] 注册消费者 topic={}, queue={}, concurrency={}",
                topic, queue, meta.concurrency());
    }

    /**
     * 幂等声明 durable 队列
     *
     * @param topic 主题（= 队列名）
     */
    private void declareQueueIfNeeded(String topic) {
        if (declaredQueues.contains(topic)) {
            return;
        }
        RabbitAdmin admin = rabbitAdminProvider.getIfAvailable();
        if (admin != null) {
            try {
                admin.declareQueue(new Queue(topic, true));
                declaredQueues.add(topic);
            } catch (Exception e) {
                log.warn("[MQ-Rabbit] 声明队列失败 topic={}: {}", topic, e.getMessage());
            }
        }
    }

    @Override
    public void destroy() {
        for (SimpleMessageListenerContainer container : containers) {
            try {
                container.stop();
            } catch (Exception e) {
                log.warn("[MQ-Rabbit] 停止容器失败: {}", e.getMessage());
            }
        }
    }
}
