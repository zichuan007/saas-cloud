package com.saas.cloud.common.core.tenant;

/**
 * 租户初始化器接口
 * <p>
 * 定义租户注册时需要执行的初始化操作。每个实现类负责一个独立的初始化步骤，
 * 由 {@link TenantInitializerRegistry} 按 order 排序后依次执行。
 * <p>
 * 失败时会倒序调用已成功初始化器的 rollback 方法。
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-20
 */
public interface TenantInitializer {

    /**
     * 初始化器唯一标识
     */
    String getCode();

    /**
     * 执行顺序，值越小越先执行
     */
    default int getOrder() {
        return 100;
    }

    /**
     * 执行初始化操作
     *
     * @param context 租户初始化上下文
     */
    void initialize(TenantInitContext context);

    /**
     * 回滚操作（当后续初始化器失败时调用）
     *
     * @param context 租户初始化上下文
     */
    default void rollback(TenantInitContext context) {
    }
}
