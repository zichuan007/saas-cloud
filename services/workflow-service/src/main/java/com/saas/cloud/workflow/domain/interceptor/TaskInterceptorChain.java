package com.saas.cloud.workflow.domain.interceptor;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * 任务拦截器责任链
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-14
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class TaskInterceptorChain {

    private final List<TaskInterceptor> interceptors;

    private List<TaskInterceptor> sortedInterceptors;

    @PostConstruct
    public void init() {
        sortedInterceptors = interceptors.stream()
                .sorted(Comparator.comparingInt(TaskInterceptor::getOrder))
                .toList();
        log.info("任务拦截器链初始化完成，共 {} 个拦截器", sortedInterceptors.size());
    }

    /**
     * 执行任务创建拦截链
     */
    public void executeOnTaskCreate(TaskContext context) {
        for (TaskInterceptor interceptor : sortedInterceptors) {
            if (context.isSkip()) {
                break;
            }
            try {
                interceptor.onTaskCreate(context);
            } catch (Exception e) {
                log.error("拦截器 [{}] onTaskCreate 执行异常", interceptor.getClass().getSimpleName(), e);
                throw e;
            }
        }
    }

    /**
     * 执行任务完成拦截链
     */
    public void executeOnTaskComplete(TaskContext context) {
        for (TaskInterceptor interceptor : sortedInterceptors) {
            if (context.isSkip()) {
                break;
            }
            try {
                interceptor.onTaskComplete(context);
            } catch (Exception e) {
                log.error("拦截器 [{}] onTaskComplete 执行异常", interceptor.getClass().getSimpleName(), e);
                throw e;
            }
        }
    }
}