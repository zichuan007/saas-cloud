package com.saas.cloud.common.job;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * XXL-Job 配置属性
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-21
 */
@Data
@ConfigurationProperties(prefix = "xxl.job")
public class XxlJobProperties {

    /** 调度中心地址 */
    private String adminAddresses = "http://127.0.0.1:8088/xxl-job-admin";

    /** 执行器 accessToken */
    private String accessToken;

    /** 执行器 AppName */
    private String executorAppname;

    /** 执行器注册地址（为空则自动获取） */
    private String executorAddress;

    /** 执行器 IP（为空则自动获取） */
    private String executorIp;

    /** 执行器端口 */
    private int executorPort = 9999;

    /** 执行器日志路径 */
    private String executorLogPath = "logs/xxl-job/jobhandler";

    /** 执行器日志保留天数 */
    private int executorLogRetentionDays = 30;
}
