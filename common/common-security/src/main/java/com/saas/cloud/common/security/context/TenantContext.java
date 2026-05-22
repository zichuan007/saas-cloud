package com.saas.cloud.common.security.context;

import com.alibaba.ttl.TransmittableThreadLocal;

import lombok.Data;

/**
 * 租户上下文，基于 TransmittableThreadLocal 支持异步传播
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
public final class TenantContext {

    private static final TransmittableThreadLocal<TenantInfo> CONTEXT = new TransmittableThreadLocal<>();

    private TenantContext() {
    }

    public static void set(TenantInfo tenantInfo) {
        CONTEXT.set(tenantInfo);
    }

    public static TenantInfo get() {
        return CONTEXT.get();
    }

    public static Long getTenantId() {
        TenantInfo info = CONTEXT.get();
        return info != null ? info.getTenantId() : null;
    }

    public static void clear() {
        CONTEXT.remove();
        IGNORE_TENANT.remove();
    }

    // ---------- 忽略租户过滤 ----------

    private static final TransmittableThreadLocal<Boolean> IGNORE_TENANT = new TransmittableThreadLocal<>();

    public static void setIgnoreTenant(boolean ignore) {
        IGNORE_TENANT.set(ignore);
    }

    public static boolean isIgnoreTenant() {
        return Boolean.TRUE.equals(IGNORE_TENANT.get());
    }

    public static void clearIgnoreTenant() {
        IGNORE_TENANT.remove();
    }

    /**
     * 在忽略租户过滤的上下文中执行有返回值的操作
     *
     * @param supplier 操作
     * @param <T>      返回类型
     * @return 操作结果
     */
    public static <T> T executeWithoutTenant(java.util.function.Supplier<T> supplier) {
        boolean original = isIgnoreTenant();
        try {
            setIgnoreTenant(true);
            return supplier.get();
        } finally {
            if (!original) {
                clearIgnoreTenant();
            }
        }
    }

    /**
     * 在忽略租户过滤的上下文中执行无返回值的操作
     *
     * @param runnable 操作
     */
    public static void executeWithoutTenant(Runnable runnable) {
        boolean original = isIgnoreTenant();
        try {
            setIgnoreTenant(true);
            runnable.run();
        } finally {
            if (!original) {
                clearIgnoreTenant();
            }
        }
    }

    @Data
    public static class TenantInfo {

        private Long tenantId;
        private String tenantName;
    }
}
