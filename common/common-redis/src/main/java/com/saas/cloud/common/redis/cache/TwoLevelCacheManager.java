package com.saas.cloud.common.redis.cache;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisTemplate;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.saas.cloud.common.security.context.TenantContext;

import lombok.extern.slf4j.Slf4j;

/**
 * 二级缓存管理器（Caffeine + Redis）
 * <p>作为命名 Bean 使用，业务方通过 {@code @Cacheable(cacheManager = "twoLevelCacheManager")} 指定。
 * 自动支持多租户隔离（Cache Name 拼接 tenantId）。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
@Slf4j
public class TwoLevelCacheManager implements CacheManager {

    /** Redis Pub/Sub 频道名 */
    public static final String CACHE_EVICT_CHANNEL = "saas:cache:evict";

    private final ConcurrentMap<String, TwoLevelCache> cacheMap = new ConcurrentHashMap<>();
    private final RedisCacheManager redisCacheManager;
    private final RedisTemplate<String, Object> redisTemplate;

    /** 本地缓存最大条目数 */
    private final int localMaxSize;

    /** 本地缓存过期时间 */
    private final Duration localExpire;

    public TwoLevelCacheManager(RedisCacheManager redisCacheManager,
                                 RedisTemplate<String, Object> redisTemplate,
                                 int localMaxSize,
                                 Duration localExpire) {
        this.redisCacheManager = redisCacheManager;
        this.redisTemplate = redisTemplate;
        this.localMaxSize = localMaxSize;
        this.localExpire = localExpire;
    }

    @Override
    public Cache getCache(String name) {
        // 多租户隔离：拼接 tenantId
        String actualName = name;
        try {
            if (!TenantContext.isIgnoreTenant() && TenantContext.getTenantId() != null) {
                actualName = name + ":" + TenantContext.getTenantId();
            }
        } catch (NoClassDefFoundError e) {
            // TenantContext 不在 classpath 时忽略
        }

        String finalName = actualName;
        return cacheMap.computeIfAbsent(finalName, key -> {
            Cache redisCache = redisCacheManager.getCache(key);
            if (redisCache == null) {
                return null;
            }

            com.github.benmanes.caffeine.cache.Cache<Object, Object> caffeineCache =
                    Caffeine.newBuilder()
                            .maximumSize(localMaxSize)
                            .expireAfterWrite(localExpire)
                            .build();

            log.debug("[二级缓存] 创建缓存: name={}", key);
            return new TwoLevelCache(key, caffeineCache, redisCache, redisTemplate, CACHE_EVICT_CHANNEL);
        });
    }

    @Override
    public Collection<String> getCacheNames() {
        return Collections.unmodifiableSet(cacheMap.keySet());
    }

    /**
     * 处理来自 Redis Pub/Sub 的缓存失效通知
     *
     * @param message 格式：cacheName::key 或 cacheName::__ALL__
     */
    public void handleEvictMessage(String message) {
        if (message == null || !message.contains("::")) {
            return;
        }
        String[] parts = message.split("::", 2);
        String cacheName = parts[0];
        String key = parts[1];

        TwoLevelCache cache = cacheMap.get(cacheName);
        if (cache != null) {
            cache.handleEvictMessage("__ALL__".equals(key) ? null : key);
            log.debug("[二级缓存] 收到失效通知: cache={}, key={}", cacheName, key);
        }
    }
}
