package com.saas.cloud.rbac.service;

import java.io.OutputStream;
import java.util.function.Consumer;

import com.baomidou.mybatisplus.extension.service.IService;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.rbac.api.vo.ExportTaskVO;
import com.saas.cloud.rbac.entity.ExportTask;

/**
 * 导出任务 服务接口
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
public interface IExportTaskService extends IService<ExportTask> {

    /**
     * 提交导出任务并异步执行
     *
     * @param taskName      任务名称
     * @param fileName      文件名（不含后缀）
     * @param exportAction  导出逻辑回调，接收 OutputStream 写入 Excel 数据
     * @return 任务ID
     */
    Long submitTask(String taskName, String fileName, Consumer<OutputStream> exportAction);

    /**
     * 查询当前用户的导出任务列表
     *
     * @param pageNum  页码
     * @param pageSize 页大小
     * @return 分页结果
     */
    PageResult<ExportTaskVO> queryMyTasks(Integer pageNum, Integer pageSize);

    /**
     * 获取下载链接（MinIO 预签名 URL）
     *
     * @param taskId 任务ID
     * @return 预签名下载 URL
     */
    String getDownloadUrl(Long taskId);

    /**
     * 删除导出任务
     *
     * @param taskId 任务ID
     */
    void deleteTask(Long taskId);

    /**
     * 清理过期导出任务
     *
     * @return 清理数量
     */
    int cleanExpiredTasks();
}
