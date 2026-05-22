package com.saas.cloud.rbac.job;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 清理过期 Token 定时任务
 * <p>清理 Sa-Token 过期会话和 Redis 中的登录失败计数</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-22
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class CleanExpiredTokenJobHandler {

    private static final String REDIS_LOGIN_FAIL_PREFIX = "auth:login_fail:*";

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 清理过期的登录失败计数
     * <p>建议 Cron: 0 0 2 * * ? (每天凌晨 2:00)</p>
     */
    @XxlJob("cleanExpiredTokenJob")
    public void execute() {

        log.info("[XXL-Job] 开始清理过期登录失败计数");
        int cleaned = 0;
        Set<String> keys = redisTemplate.keys(REDIS_LOGIN_FAIL_PREFIX);
        if (keys != null && !keys.isEmpty()) {
            Long deleted = redisTemplate.delete(keys);
            cleaned = deleted != null ? deleted.intValue() : 0;
        }
        String msg = "清理完成, 删除过期登录失败计数: " + cleaned + " 条";
        log.info("[XXL-Job] {}", msg);
        XxlJobHelper.handleSuccess(msg);
    }

}
