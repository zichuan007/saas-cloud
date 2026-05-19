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
    }

    @Data
    public static class TenantInfo {

        private Long tenantId;
        private String tenantName;
    }
}
