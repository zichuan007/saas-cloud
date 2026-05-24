package com.saas.cloud.common.redis.tenant;

import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;

import com.saas.cloud.common.security.context.TenantContext;

import lombok.extern.slf4j.Slf4j;

/**
 * 多租户 Redis 缓存管理器
 * <p>操作指定 name 的 Cache 时，自动拼接租户后缀，格式为 name:tenantId，
 * 确保不同租户的 @Cacheable 等注解缓存数据隔离。</p>
 * <p>当租户上下文不存在或处于忽略租户模式时，使用原始 name 不拼接。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
@Slf4j
public class TenantRedisCacheManager extends RedisCacheManager {

    public TenantRedisCacheManager(RedisCacheWriter cacheWriter,
                                    RedisCacheConfiguration defaultCacheConfiguration) {
        super(cacheWriter, defaultCacheConfiguration);
    }

    @Override
    public Cache getCache(String name) {
        if (!TenantContext.isIgnoreTenant() && TenantContext.getTenantId() != null) {
            name = name + ":" + TenantContext.getTenantId();
        }
        return super.getCache(name);
    }
}
