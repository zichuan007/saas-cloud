package com.saas.cloud.common.data.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.alibaba.ttl.threadpool.TtlExecutors;

import lombok.extern.slf4j.Slf4j;

/**
 * 异步任务配置
 * <p>使用 TtlExecutors 包装线程池，使 {@link com.alibaba.ttl.TransmittableThreadLocal}
 * （{@code TenantContext}/{@code UserContext}）在 @Async 线程中自动透传，
 * 避免异步线程丢失租户上下文导致数据隔离失效或落库 tenant_id 丢失。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-07-22
 */
@Slf4j
@EnableAsync
@Configuration
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(32);
        executor.setQueueCapacity(200);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("saas-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        // TTL 包装：自动透传 TransmittableThreadLocal 上下文
        return TtlExecutors.getTtlExecutor(executor);
    }
}
