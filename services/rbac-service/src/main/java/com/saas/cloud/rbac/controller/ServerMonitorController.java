package com.saas.cloud.rbac.controller;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.rbac.api.vo.ServerInfoVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.util.Util;

/**
 * 服务器监控
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Slf4j
@Tag(name = "服务器监控")
@RestController
@RequestMapping("/monitor/server")
public class ServerMonitorController {

    private static final long MB = 1024 * 1024;
    private static final long GB = 1024 * 1024 * 1024;

    /**
     * 获取服务器信息
     *
     * @return 服务器信息
     */
    @Operation(summary = "获取服务器信息")
    @GetMapping
    public ApiResult<ServerInfoVO> serverInfo() {
        ServerInfoVO vo = new ServerInfoVO();
        vo.setCpu(buildCpuInfo());
        vo.setJvm(buildJvmInfo());
        vo.setOs(buildOsInfo());
        vo.setDisks(buildDiskInfo());
        return ApiResult.ok(vo);
    }

    private ServerInfoVO.CpuInfo buildCpuInfo() {
        SystemInfo si = new SystemInfo();
        CentralProcessor processor = si.getHardware().getProcessor();
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        Util.sleep(500);
        long[] ticks = processor.getSystemCpuLoadTicks();

        long user = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];
        long system = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
        long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
        long iowait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
        long total = user + system + idle + iowait;

        ServerInfoVO.CpuInfo cpu = new ServerInfoVO.CpuInfo();
        cpu.setCpuNum(processor.getLogicalProcessorCount());
        cpu.setSystemUsage(total > 0 ? round(system * 100.0 / total) : 0);
        cpu.setUserUsage(total > 0 ? round(user * 100.0 / total) : 0);
        cpu.setIdle(total > 0 ? round(idle * 100.0 / total) : 0);
        return cpu;
    }

    private ServerInfoVO.JvmInfo buildJvmInfo() {
        Runtime runtime = Runtime.getRuntime();
        long jvmMax = runtime.maxMemory();
        long jvmTotal = runtime.totalMemory();
        long jvmFree = runtime.freeMemory();
        long jvmUsed = jvmTotal - jvmFree;

        long startTimeMs = ManagementFactory.getRuntimeMXBean().getStartTime();
        Instant startInstant = Instant.ofEpochMilli(startTimeMs);
        Duration uptime = Duration.between(startInstant, Instant.now());

        ServerInfoVO.JvmInfo jvm = new ServerInfoVO.JvmInfo();
        jvm.setMaxMemory(jvmMax / MB);
        jvm.setTotalMemory(jvmTotal / MB);
        jvm.setUsedMemory(jvmUsed / MB);
        jvm.setFreeMemory(jvmFree / MB);
        jvm.setUsageRate(jvmMax > 0 ? round(jvmUsed * 100.0 / jvmMax) : 0);
        jvm.setJavaVersion(System.getProperty("java.version"));
        jvm.setStartTime(LocalDateTime.ofInstant(startInstant, ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        jvm.setRunTime(String.format("%d天%d小时%d分钟",
                uptime.toDays(), uptime.toHoursPart(), uptime.toMinutesPart()));
        return jvm;
    }

    private ServerInfoVO.OsInfo buildOsInfo() {
        ServerInfoVO.OsInfo os = new ServerInfoVO.OsInfo();
        os.setOsName(System.getProperty("os.name"));
        os.setOsArch(System.getProperty("os.arch"));
        try {
            InetAddress addr = InetAddress.getLocalHost();
            os.setHostName(addr.getHostName());
            os.setHostIp(addr.getHostAddress());
        } catch (Exception e) {
            os.setHostName("未知");
            os.setHostIp("未知");
        }
        return os;
    }

    private List<ServerInfoVO.DiskInfo> buildDiskInfo() {
        SystemInfo si = new SystemInfo();
        FileSystem fileSystem = si.getOperatingSystem().getFileSystem();
        List<OSFileStore> stores = fileSystem.getFileStores();
        List<ServerInfoVO.DiskInfo> disks = new ArrayList<>();
        for (OSFileStore store : stores) {
            long total = store.getTotalSpace();
            long usable = store.getUsableSpace();
            if (total <= 0) {
                continue;
            }
            long used = total - usable;
            ServerInfoVO.DiskInfo disk = new ServerInfoVO.DiskInfo();
            disk.setDirName(store.getMount());
            disk.setFsType(store.getType());
            disk.setTotal(formatSize(total));
            disk.setUsed(formatSize(used));
            disk.setFree(formatSize(usable));
            disk.setUsageRate(round(used * 100.0 / total));
            disks.add(disk);
        }
        return disks;
    }

    private String formatSize(long bytes) {
        if (bytes >= GB) {
            return String.format("%.2f GB", (double) bytes / GB);
        }
        return String.format("%.2f MB", (double) bytes / MB);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
