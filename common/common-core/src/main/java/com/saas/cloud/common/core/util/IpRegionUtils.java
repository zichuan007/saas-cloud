package com.saas.cloud.common.core.util;

import java.io.InputStream;

import org.lionsoul.ip2region.xdb.Searcher;

import lombok.extern.slf4j.Slf4j;

/**
 * IP 归属地解析工具
 * <p>基于 ip2region 离线数据库，加载到内存一次查询 ~微秒级</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Slf4j
public final class IpRegionUtils {

    private static Searcher searcher;

    static {
        try (InputStream is = IpRegionUtils.class.getClassLoader().getResourceAsStream("ip2region.xdb")) {
            if (is != null) {
                byte[] buff = is.readAllBytes();
                searcher = Searcher.newWithBuffer(buff);
                log.info("[IpRegion] ip2region.xdb 加载成功, 大小: {} bytes", buff.length);
            } else {
                log.warn("[IpRegion] ip2region.xdb 未找到, IP归属地解析将不可用");
            }
        } catch (Exception e) {
            log.error("[IpRegion] 初始化 ip2region 失败", e);
        }
    }

    private IpRegionUtils() {
    }

    /**
     * 获取 IP 归属地
     *
     * @param ip IP 地址
     * @return 归属地描述，如 "中国|0|上海|上海市|电信"；解析失败返回 "未知"
     */
    public static String getRegion(String ip) {
        if (searcher == null || ip == null || ip.isBlank()) {
            return "未知";
        }
        // 内网 IP 直接返回
        if (isInternalIp(ip)) {
            return "内网IP";
        }
        try {
            String region = searcher.search(ip);
            return formatRegion(region);
        } catch (Exception e) {
            log.debug("[IpRegion] 解析IP归属地失败, ip={}", ip, e);
            return "未知";
        }
    }

    /**
     * 格式化 ip2region 返回结果
     * <p>原始格式：中国|0|上海|上海市|电信 → 去掉 "0" 段，合并为 "中国 上海 上海市 电信"</p>
     */
    private static String formatRegion(String region) {
        if (region == null || region.isBlank()) {
            return "未知";
        }
        String[] parts = region.split("\\|");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!"0".equals(part) && !part.isBlank()) {
                if (sb.length() > 0) {
                    sb.append(" ");
                }
                sb.append(part);
            }
        }
        return sb.length() > 0 ? sb.toString() : "未知";
    }

    /**
     * 判断是否为内网 IP
     */
    private static boolean isInternalIp(String ip) {
        return ip.startsWith("10.") || ip.startsWith("192.168.")
                || ip.startsWith("172.16.") || ip.startsWith("172.17.")
                || ip.startsWith("172.18.") || ip.startsWith("172.19.")
                || ip.startsWith("172.2") || ip.startsWith("172.3")
                || "127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)
                || "localhost".equalsIgnoreCase(ip);
    }
}
