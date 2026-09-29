package com.saas.cloud.common.redis.util;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 基于 Redis 原子计数器的业务单号生成器
 * <p>格式：prefix + yyyyMMdd + N 位序号(左补零)，序号每日按 prefix 维度从 1 递增。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-09-29
 */
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SequenceUtil {

    private final RedissonClient redissonClient;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 生成单号：prefix + yyyyMMdd + 6 位序号
     *
     * @param prefix 业务前缀，如 "ORD"/"PAY"
     * @return 单号
     */
    public String next(String prefix) {
        return next(prefix, 6);
    }

    /**
     * 生成单号：prefix + yyyyMMdd + seqLength 位序号(左补零)
     *
     * @param prefix    业务前缀
     * @param seqLength 序号位数
     * @return 单号
     */
    public String next(String prefix, int seqLength) {
        String date = LocalDate.now().format(DATE_FMT);
        String key = "seq:" + prefix + ":" + date;
        RAtomicLong counter = redissonClient.getAtomicLong(key);
        long seq = counter.incrementAndGet();
        // 当日首次生成时设置过期，避免 key 常驻；2 天后自动清理
        if (seq == 1) {
            counter.expire(Duration.ofDays(2));
        }
        return prefix + date + leftPad(seq, seqLength);
    }

    private String leftPad(long value, int length) {
        String s = String.valueOf(value);
        if (s.length() >= length) {
            return s;
        }
        StringBuilder sb = new StringBuilder(length);
        for (int i = s.length(); i < length; i++) {
            sb.append('0');
        }
        return sb.append(s).toString();
    }
}
