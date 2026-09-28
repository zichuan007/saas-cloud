package com.saas.cloud.workflow.domain.interceptor;

/**
 * 任务拦截器接口
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-14
 */
public interface TaskInterceptor {

    /**
     * 执行顺序，值越小越先执行
     */
    int getOrder();

    /**
     * 任务创建时回调
     */
    default void onTaskCreate(TaskContext context) {
    }

    /**
     * 任务完成时回调
     */
    default void onTaskComplete(TaskContext context) {
    }
}