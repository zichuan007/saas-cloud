package com.saas.cloud.common.redis.cache;

import java.time.Duration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * 二级缓存自动配置
 * <p>注册 {@link TwoLevelCacheManager} 作为命名 Bean（不影响 @Primary 的 TenantRedisCacheManager），
 * 并注册 Redis Pub/Sub 监听器用于多实例间本地缓存同步失效。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
@Configuration
@ConditionalOnClass(Caffeine.class)
public class TwoLevelCacheAutoConfiguration {

    /** 本地缓存最大条目 */
    private static final int LOCAL_MAX_SIZE = 1000;

    /** 本地缓存过期时间（5 分钟） */
    private static final Duration LOCAL_EXPIRE = Duration.ofMinutes(5);

    @Bean
    public TwoLevelCacheManager twoLevelCacheManager(RedisConnectionFactory factory,
                                                      RedisTemplate<String, Object> redisTemplate) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(2))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        RedisCacheWriter cacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(factory);
        RedisCacheManager redisCacheManager = new RedisCacheManager(cacheWriter, config);

        return new TwoLevelCacheManager(redisCacheManager, redisTemplate, LOCAL_MAX_SIZE, LOCAL_EXPIRE);
    }

    @Bean
    public CacheRefreshListener cacheRefreshListener(TwoLevelCacheManager twoLevelCacheManager) {
        return new CacheRefreshListener(twoLevelCacheManager);
    }

    @Bean
    public RedisMessageListenerContainer cacheEvictListenerContainer(RedisConnectionFactory factory,
                                                                      CacheRefreshListener listener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        container.addMessageListener(listener, new ChannelTopic(TwoLevelCacheManager.CACHE_EVICT_CHANNEL));
        return container;
    }
}
