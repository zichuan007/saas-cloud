package com.saas.cloud.workflow.config;

import lombok.extern.slf4j.Slf4j;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Flowable 流程引擎配置
 * <p>
 * Flowable ProcessEngine 由 spring-boot-starter 自动配置，
 * 这里通过 EngineConfigurationConfigurer 对引擎进行额外定制。
 * </p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@Configuration
public class FlowableConfig {

    /**
     * 定制 Flowable 引擎配置
     * <p>
     * 自动部署通过 application.yml 中 flowable.process.deploy-resources=false 控制。
     * 这里保留扩展点用于后续定制（如自定义 ID 生成器等）。
     * </p>
     *
     * @return 引擎配置定制器
     */
    @Bean
    public EngineConfigurationConfigurer<SpringProcessEngineConfiguration> flowableConfigurer() {
        return configuration -> {
            log.info("Flowable 引擎配置: database-schema-update={}, async-executor={}",
                    configuration.getDatabaseSchemaUpdate(),
                    configuration.isAsyncExecutorActivate());
        };
    }
}
