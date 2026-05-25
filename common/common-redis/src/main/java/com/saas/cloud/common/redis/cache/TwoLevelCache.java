package com.saas.cloud.common.redis.cache;

import java.util.concurrent.Callable;

import org.springframework.cache.Cache;
import org.springframework.data.redis.core.RedisTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * 二级缓存实现（Caffeine 本地缓存 + Redis 远程缓存）
 * <p>读取链路：Caffeine → Redis → 数据库（回填两级）。
 * 写入/删除时同时操作两级缓存，并通过 Redis Pub/Sub 通知其他实例失效本地缓存。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
@Slf4j
public class TwoLevelCache implements Cache {

    private final String name;
    private final com.github.benmanes.caffeine.cache.Cache<Object, Object> caffeineCache;
    private final Cache redisCache;
    private final RedisTemplate<String, Object> redisTemplate;
    private final String pubSubChannel;

    public TwoLevelCache(String name,
                         com.github.benmanes.caffeine.cache.Cache<Object, Object> caffeineCache,
                         Cache redisCache,
                         RedisTemplate<String, Object> redisTemplate,
                         String pubSubChannel) {
        this.name = name;
        this.caffeineCache = caffeineCache;
        this.redisCache = redisCache;
        this.redisTemplate = redisTemplate;
        this.pubSubChannel = pubSubChannel;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getNativeCache() {
        return caffeineCache;
    }

    @Override
    public ValueWrapper get(Object key) {
        // L1: Caffeine
        Object localValue = caffeineCache.getIfPresent(key);
        if (localValue != null) {
            log.debug("[二级缓存] L1 命中: cache={}, key={}", name, key);
            return () -> localValue;
        }

        // L2: Redis
        ValueWrapper redisValue = redisCache.get(key);
        if (redisValue != null) {
            log.debug("[二级缓存] L2 命中，回填 L1: cache={}, key={}", name, key);
            caffeineCache.put(key, redisValue.get());
            return redisValue;
        }

        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Object key, Class<T> type) {
        ValueWrapper wrapper = get(key);
        if (wrapper == null) {
            return null;
        }
        Object value = wrapper.get();
        if (value != null && type != null && !type.isInstance(value)) {
            throw new IllegalStateException("Cached value type mismatch: expected " + type.getName());
        }
        return (T) value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Object key, Callable<T> valueLoader) {
        ValueWrapper wrapper = get(key);
        if (wrapper != null) {
            return (T) wrapper.get();
        }

        // 缓存未命中，通过 valueLoader 加载
        try {
            T value = valueLoader.call();
            put(key, value);
            return value;
        } catch (Exception e) {
            throw new Cache.ValueRetrievalException(key, valueLoader, e);
        }
    }

    @Override
    public void put(Object key, Object value) {
        redisCache.put(key, value);
        caffeineCache.put(key, value);
    }

    @Override
    public void evict(Object key) {
        redisCache.evict(key);
        caffeineCache.invalidate(key);
        publishEvictMessage(key);
    }

    @Override
    public void clear() {
        redisCache.clear();
        caffeineCache.invalidateAll();
        publishEvictMessage(null);
    }

    /**
     * 发布缓存失效通知到其他实例
     */
    private void publishEvictMessage(Object key) {
        try {
            String message = name + "::" + (key != null ? key.toString() : "__ALL__");
            redisTemplate.convertAndSend(pubSubChannel, message);
        } catch (Exception e) {
            log.warn("[二级缓存] Pub/Sub 通知失败: cache={}, key={}", name, key, e);
        }
    }

    /**
     * 处理来自其他实例的失效通知
     */
    public void handleEvictMessage(Object key) {
        if (key == null || "__ALL__".equals(key.toString())) {
            caffeineCache.invalidateAll();
        } else {
            caffeineCache.invalidate(key);
        }
    }
}
