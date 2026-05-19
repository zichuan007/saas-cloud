package com.saas.cloud.wechat.oa;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 微信公众号服务启动类
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@EnableScheduling
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.saas.cloud")
@SpringBootApplication(scanBasePackages = "com.saas.cloud")
@MapperScan("com.saas.cloud.wechat.oa.mapper")
public class WechatOaApplication {

    public static void main(String[] args) {
        SpringApplication.run(WechatOaApplication.class, args);
    }
}
