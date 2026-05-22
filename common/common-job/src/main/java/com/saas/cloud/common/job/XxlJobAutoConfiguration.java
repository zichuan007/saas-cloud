package com.saas.cloud.common.job;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * XXL-Job 自动配置
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-21
 */
@Slf4j
@Configuration
@ConditionalOnClass(XxlJobSpringExecutor.class)
@ConditionalOnProperty(prefix = "xxl.job", name = "admin-addresses")
@EnableConfigurationProperties(XxlJobProperties.class)
public class XxlJobAutoConfiguration {

    @Bean
    public XxlJobSpringExecutor xxlJobExecutor(XxlJobProperties properties) {
        log.info("XXL-Job 执行器初始化: adminAddresses={}, appname={}", properties.getAdminAddresses(), properties.getExecutorAppname());

        XxlJobSpringExecutor executor = new XxlJobSpringExecutor();
        executor.setAdminAddresses(properties.getAdminAddresses());
        executor.setAppname(properties.getExecutorAppname());
        executor.setAddress(properties.getExecutorAddress());
        executor.setIp(properties.getExecutorIp());
        executor.setPort(properties.getExecutorPort());
        executor.setAccessToken(properties.getAccessToken());
        executor.setLogPath(properties.getExecutorLogPath());
        executor.setLogRetentionDays(properties.getExecutorLogRetentionDays());
        return executor;
    }
}
