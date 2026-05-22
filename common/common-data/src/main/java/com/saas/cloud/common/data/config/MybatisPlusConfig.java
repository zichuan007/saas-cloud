package com.saas.cloud.common.data.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.saas.cloud.common.data.interceptor.DataScopeSqlInterceptor;
import com.saas.cloud.common.data.tenant.TenantLineHandlerImpl;
import com.saas.cloud.common.data.tenant.TenantProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Configuration
@EnableConfigurationProperties(TenantProperties.class)
@RequiredArgsConstructor
public class MybatisPlusConfig {

    private final TenantProperties tenantProperties;

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 1. 多租户拦截器（必须放在第一个位置）
        if (tenantProperties.isEnable()) {
            TenantLineInnerInterceptor tenantInterceptor = new TenantLineInnerInterceptor();
            tenantInterceptor.setTenantLineHandler(new TenantLineHandlerImpl(tenantProperties));
            interceptor.addInnerInterceptor(tenantInterceptor);
        }

        // 2. 数据范围拦截器
        interceptor.addInnerInterceptor(new DataScopeSqlInterceptor());

        // 3. 分页拦截器
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));

        // 4. 乐观锁拦截器
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        return interceptor;
    }
}
