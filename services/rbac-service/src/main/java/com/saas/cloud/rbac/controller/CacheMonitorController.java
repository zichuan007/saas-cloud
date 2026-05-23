package com.saas.cloud.rbac.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.rbac.api.vo.CacheInfoVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 缓存监控
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Slf4j
@Tag(name = "缓存监控")
@RestController
@RequestMapping("/monitor/cache")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class CacheMonitorController {

    private final StringRedisTemplate redisTemplate;

    /**
     * 获取 Redis 缓存信息
     *
     * @return 缓存信息
     */
    @Operation(summary = "获取缓存信息")
    @GetMapping("/info")
    public ApiResult<CacheInfoVO> info() {
        Properties info = redisTemplate.execute((RedisCallback<Properties>) RedisServerCommands::info);
        Long dbSize = redisTemplate.execute((RedisCallback<Long>) RedisServerCommands::dbSize);

        CacheInfoVO vo = new CacheInfoVO();
        if (info != null) {
            vo.setRedisVersion(info.getProperty("redis_version"));
            vo.setRedisMode(info.getProperty("redis_mode"));
            vo.setOs(info.getProperty("os"));
            vo.setUsedMemoryHuman(info.getProperty("used_memory_human"));
            vo.setMaxMemoryHuman(info.getProperty("maxmemory_human", "不限制"));
            vo.setConnectedClients(info.getProperty("connected_clients"));
            vo.setUptimeInDays(info.getProperty("uptime_in_days"));

            long usedMemory = Long.parseLong(info.getProperty("used_memory", "0"));
            long maxMemory = Long.parseLong(info.getProperty("maxmemory", "0"));
            if (maxMemory > 0) {
                vo.setMemoryUsageRate(String.format("%.2f", (double) usedMemory / maxMemory * 100));
            } else {
                vo.setMemoryUsageRate("N/A");
            }
        }
        vo.setDbSize(dbSize);

        Properties commandStats = redisTemplate.execute(
                (RedisCallback<Properties>) connection -> connection.serverCommands().info("commandstats"));
        List<Map<String, String>> statsList = new ArrayList<>();
        if (commandStats != null) {
            commandStats.forEach((key, value) -> {
                Map<String, String> stat = new HashMap<>();
                String cmdName = String.valueOf(key).replace("cmdstat_", "");
                stat.put("name", cmdName);
                stat.put("value", String.valueOf(value));
                statsList.add(stat);
            });
        }
        vo.setCommandStats(statsList);

        return ApiResult.ok(vo);
    }

    /**
     * 按前缀查询 key 列表（使用 SCAN，安全）
     *
     * @param prefix key 前缀
     * @param count  返回数量限制，默认 100
     * @return key 列表
     */
    @Operation(summary = "按前缀搜索缓存Key")
    @GetMapping("/keys")
    public ApiResult<List<String>> keys(
            @RequestParam(defaultValue = "*") String prefix,
            @RequestParam(defaultValue = "100") int count) {
        String pattern = prefix.endsWith("*") ? prefix : prefix + "*";
        List<String> keys = new ArrayList<>();
        redisTemplate.execute((RedisCallback<Object>) connection -> {
            var cursor = connection.keyCommands().scan(
                    org.springframework.data.redis.core.ScanOptions.scanOptions()
                            .match(pattern)
                            .count(count)
                            .build());
            int collected = 0;
            while (cursor.hasNext() && collected < count) {
                keys.add(new String(cursor.next()));
                collected++;
            }
            return null;
        });
        return ApiResult.ok(keys);
    }

    /**
     * 删除指定缓存 key
     *
     * @param key 缓存 key
     * @return 操作结果
     */
    @Operation(summary = "删除指定缓存Key")
    @DeleteMapping("/key/{key}")
    public ApiResult<Void> deleteKey(@PathVariable String key) {
        redisTemplate.delete(key);
        log.info("[缓存监控] 删除缓存key: {}", key);
        return ApiResult.ok();
    }
}
