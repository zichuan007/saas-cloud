package com.saas.cloud.common.core.tenant;

import java.util.HashMap;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

/**
 * 租户初始化上下文
 * <p>
 * 携带租户注册信息，并提供 data map 供各初始化器之间传递数据。
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-20
 */
@Data
@Builder
public class TenantInitContext {

    /** 租户ID */
    private Long tenantId;

    /** 租户名称 */
    private String tenantName;

    /** 联系人 */
    private String contactPerson;

    /** 联系电话 */
    private String contactPhone;

    /** 管理员密码（已加密） */
    private String password;

    /** 初始化器间数据传递 */
    @Builder.Default
    private Map<String, Object> data = new HashMap<>();

    public void put(String key, Object value) {
        data.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) data.get(key);
    }
}
