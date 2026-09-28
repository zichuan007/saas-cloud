package com.saas.cloud.workflow.config;

import com.saas.cloud.workflow.app.listener.FlowableEventBridge;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

/**
 * Flowable 流程引擎配置
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@Configuration
public class FlowableConfig {

    @Bean
    public EngineConfigurationConfigurer<SpringProcessEngineConfiguration> flowableConfigurer(
            FlowableEventBridge eventBridge) {
        return configuration -> {
            log.info("Flowable 引擎配置: database-schema-update={}, async-executor={}",
                    configuration.getDatabaseSchemaUpdate(),
                    configuration.isAsyncExecutorActivate());

            // 注册事件桥接器，将 Flowable 内部事件统一转换为 Spring ApplicationEvent
            configuration.getEventDispatcher().addEventListener(eventBridge,
                    FlowableEngineEventType.TASK_CREATED,
                    FlowableEngineEventType.TASK_COMPLETED,
                    FlowableEngineEventType.PROCESS_COMPLETED);
        };
    }
}