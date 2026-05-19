package com.saas.cloud.common.core.constant;

/**
 * 安全相关常量
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
public final class SecurityConstants {

    private SecurityConstants() {
    }

    public static final String HEADER_TENANT_ID = "X-Tenant-Id";
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USERNAME = "X-Username";
    public static final String HEADER_ROLE_LEVEL = "X-Role-Level";
    public static final String HEADER_DATA_SCOPE = "X-Data-Scope";
    public static final String HEADER_DEPT_ID = "X-Dept-Id";
    public static final String HEADER_PERMISSIONS = "X-Permissions";
    public static final String HEADER_SIGNATURE = "X-Signature";
    public static final String HEADER_TIMESTAMP = "X-Timestamp";

    public static final String HEADER_INTERNAL_SIGNATURE = "X-Internal-Signature";
    public static final String HEADER_INTERNAL_TIMESTAMP = "X-Internal-Timestamp";

    public static final String TOKEN_PREFIX = "Bearer ";

    public static final String JWT_CLAIM_TENANT_ID = "tid";
    public static final String JWT_CLAIM_USERNAME = "username";
    public static final String JWT_CLAIM_ROLE_LEVEL = "roleLevel";
    public static final String JWT_CLAIM_DATA_SCOPE = "dataScope";
    public static final String JWT_CLAIM_DEPT_ID = "deptId";
    public static final String JWT_CLAIM_PERMISSIONS = "permissions";

    public static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    public static final String TENANT_CACHE_PREFIX = "tenant:info:";
}
