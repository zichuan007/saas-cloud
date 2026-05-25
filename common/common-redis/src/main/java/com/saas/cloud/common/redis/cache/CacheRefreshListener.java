package com.saas.cloud.common.redis.cache;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis Pub/Sub 缓存失效监听器
 * <p>监听 {@link TwoLevelCacheManager#CACHE_EVICT_CHANNEL} 频道的消息，
 * 收到其他实例发布的失效通知后，清除本地 Caffeine 缓存中对应的 key。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
@Slf4j
@RequiredArgsConstructor
public class CacheRefreshListener implements MessageListener {

    private final TwoLevelCacheManager cacheManager;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String body = new String(message.getBody());
            cacheManager.handleEvictMessage(body);
        } catch (Exception e) {
            log.warn("[二级缓存] 处理 Pub/Sub 消息失败", e);
        }
    }
}
