package com.saas.cloud.common.mq.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

import com.saas.cloud.common.mq.MqProperties;
import com.saas.cloud.common.mq.kafka.KafkaMqAutoConfiguration;

/**
 * 统一 MQ 自动装配入口
 * <p>按 {@code saas.mq.type} 装配对应适配器。当前默认 Kafka；Rabbit/Rocket 适配器就绪后
 * 按 {@code @ConditionalOnProperty(name="saas.mq.type", havingValue="...")} 切换。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-24
 */
@AutoConfiguration
@EnableConfigurationProperties(MqProperties.class)
@Import(KafkaMqAutoConfiguration.class)
public class MqAutoConfiguration {
}
