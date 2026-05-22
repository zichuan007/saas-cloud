package com.saas.cloud.generator.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 代码生成器 Web 服务启动类
 * 通过 Nacos 注册到网关，数据库连接由用户通过接口参数传入，按需建立 JDBC 连接。
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@SpringBootApplication(
        scanBasePackages = "com.saas.cloud.generator.web",
        exclude = {DataSourceAutoConfiguration.class},
        excludeName = {
                "com.saas.cloud.common.data.config.MybatisPlusConfig",
                "com.saas.cloud.common.data.handler.AuditFieldHandler",
                "com.saas.cloud.common.security.filter.TenantContextFilter",
                "com.saas.cloud.common.security.interceptor.PermissionAspect"
        }
)
public class GeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(GeneratorApplication.class, args);
    }
}
