package com.saas.cloud.common.data.tenant;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * 多租户配置属性，支持 YAML 配置忽略表等参数
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-20
 */
@Data
@ConfigurationProperties(prefix = "saas.tenant")
public class TenantProperties {

    /**
     * 是否启用多租户
     */
    private boolean enable = true;

    /**
     * 租户字段名
     */
    private String column = "tenant_id";

    /**
     * 不需要租户隔离的表（平台级表），支持 application.yml 配置
     */
    private List<String> ignoreTables = Arrays.asList(
            "sys_menu",
            "sys_package",
            "sys_tenant",
            "sys_platform_user",
            "sys_announcement",
            "sys_global_config",
            "notify_template",
            "sys_area"
    );
}
