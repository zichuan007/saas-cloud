package com.saas.cloud.common.security.context;

import com.alibaba.ttl.TransmittableThreadLocal;
import lombok.Data;

import java.util.Set;

/**
 * 用户上下文，基于 TransmittableThreadLocal 支持异步传播
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
public final class UserContext {

    private static final TransmittableThreadLocal<UserInfo> CONTEXT = new TransmittableThreadLocal<>();

    private UserContext() {
    }

    public static void set(UserInfo userInfo) {
        CONTEXT.set(userInfo);
    }

    public static UserInfo get() {
        return CONTEXT.get();
    }

    public static Long getUserId() {
        UserInfo info = CONTEXT.get();
        return info != null ? info.getUserId() : null;
    }

    public static void clear() {
        CONTEXT.remove();
    }

    @Data
    public static class UserInfo {

        private Long userId;
        private String username;
        private Long tenantId;
        private Long deptId;
        private Integer roleLevel;
        private Integer dataScope;
        private Set<String> permissions;
    }
}
