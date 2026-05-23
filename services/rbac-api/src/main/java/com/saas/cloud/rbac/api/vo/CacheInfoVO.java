package com.saas.cloud.rbac.api.vo;

import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * Redis 缓存信息 VO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Data
public class CacheInfoVO {

    /** Redis 版本 */
    private String redisVersion;

    /** 运行模式 */
    private String redisMode;

    /** 操作系统 */
    private String os;

    /** 已用内存（人类可读） */
    private String usedMemoryHuman;

    /** 最大内存（人类可读） */
    private String maxMemoryHuman;

    /** 内存使用率(%) */
    private String memoryUsageRate;

    /** 已连接客户端数 */
    private String connectedClients;

    /** 运行天数 */
    private String uptimeInDays;

    /** key 总数 */
    private Long dbSize;

    /** 命令统计 */
    private List<Map<String, String>> commandStats;
}
