package com.saas.cloud.rbac.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.saas.cloud.rbac.service.IExportTaskService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 清理过期导出任务定时任务
 * <p>清理过期的导出任务记录及对应的 MinIO 文件</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class CleanExpiredExportTaskJobHandler {

    private final IExportTaskService exportTaskService;

    /**
     * 清理过期导出任务
     * <p>建议 Cron: 0 0 3 * * ? (每天凌晨 3:00)</p>
     */
    @XxlJob("cleanExpiredExportTaskJob")
    public void execute() {
        log.info("[XXL-Job] 开始清理过期导出任务");
        int cleaned = exportTaskService.cleanExpiredTasks();
        String msg = "清理完成, 删除过期导出任务: " + cleaned + " 条";
        log.info("[XXL-Job] {}", msg);
        XxlJobHelper.handleSuccess(msg);
    }
}
