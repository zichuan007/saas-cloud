package com.saas.cloud.rbac.api.vo;

import java.util.List;

import lombok.Data;

/**
 * 服务器信息 VO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Data
public class ServerInfoVO {

    /** CPU 信息 */
    private CpuInfo cpu;

    /** JVM 内存信息 */
    private JvmInfo jvm;

    /** 操作系统信息 */
    private OsInfo os;

    /** 磁盘信息 */
    private List<DiskInfo> disks;

    @Data
    public static class CpuInfo {
        /** CPU 核心数 */
        private int cpuNum;
        /** 系统使用率(%) */
        private double systemUsage;
        /** 用户使用率(%) */
        private double userUsage;
        /** 空闲率(%) */
        private double idle;
    }

    @Data
    public static class JvmInfo {
        /** JVM 最大内存(MB) */
        private long maxMemory;
        /** JVM 已分配内存(MB) */
        private long totalMemory;
        /** JVM 已使用内存(MB) */
        private long usedMemory;
        /** JVM 空闲内存(MB) */
        private long freeMemory;
        /** 内存使用率(%) */
        private double usageRate;
        /** Java 版本 */
        private String javaVersion;
        /** JVM 启动时间 */
        private String startTime;
        /** JVM 运行时长 */
        private String runTime;
    }

    @Data
    public static class OsInfo {
        /** 操作系统名称 */
        private String osName;
        /** 系统架构 */
        private String osArch;
        /** 主机名 */
        private String hostName;
        /** 主机IP */
        private String hostIp;
    }

    @Data
    public static class DiskInfo {
        /** 盘符 */
        private String dirName;
        /** 文件系统类型 */
        private String fsType;
        /** 总大小 */
        private String total;
        /** 已用大小 */
        private String used;
        /** 可用大小 */
        private String free;
        /** 使用率(%) */
        private double usageRate;
    }
}
