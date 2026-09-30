package com.saas.cloud.workflow.config;

import java.util.List;

import com.saas.cloud.workflow.app.listener.FlowableEventBridge;
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

            // 注册事件桥接器：用 setEventListeners 在引擎 init 阶段应用（configurer 阶段 getEventDispatcher() 尚为 null）
            // bridge.onEvent 内部按 FlowableEventType 过滤，仅处理 TASK_CREATED/TASK_COMPLETED/PROCESS_COMPLETED
            configuration.setEventListeners(List.of(eventBridge));
        };
    }
}