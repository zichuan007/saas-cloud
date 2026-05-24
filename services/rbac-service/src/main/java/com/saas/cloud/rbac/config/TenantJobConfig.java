package com.saas.cloud.rbac.config;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.data.tenant.job.TenantFrameworkService;
import com.saas.cloud.platform.api.feign.PlatformFeignClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * TenantJob 配置：基于 PlatformFeignClient 提供租户ID列表
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
@Slf4j
@Configuration
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class TenantJobConfig {

    private final PlatformFeignClient platformFeignClient;

    @Bean
    public TenantFrameworkService tenantFrameworkService() {
        return () -> {
            try {
                ApiResult<List<Long>> result = platformFeignClient.getActiveTenantIds();
                if (result != null && result.isSuccess() && result.getData() != null) {
                    return result.getData();
                }
                log.warn("[TenantFrameworkService] 获取租户ID列表失败: {}", result);
                return Collections.emptyList();
            } catch (Exception e) {
                log.error("[TenantFrameworkService] 获取租户ID列表异常", e);
                return Collections.emptyList();
            }
        };
    }
}
