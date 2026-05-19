package com.saas.cloud.rbac;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 权限管理服务启动类
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.saas.cloud.rbac", "com.saas.cloud.platform.api.feign"})
@SpringBootApplication(scanBasePackages = "com.saas.cloud")
@MapperScan("com.saas.cloud.rbac.mapper")
public class RbacApplication {

    public static void main(String[] args) {
        SpringApplication.run(RbacApplication.class, args);
    }
}
