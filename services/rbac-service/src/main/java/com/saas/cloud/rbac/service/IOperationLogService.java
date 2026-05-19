package com.saas.cloud.rbac.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.rbac.entity.OperationLog;

/**
 * 操作日志表 服务接口
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
public interface IOperationLogService extends IService<OperationLog> {

    /**
     * 分页查询操作日志
     *
     * @param module    操作模块（可选）
     * @param username  操作人（可选）
     * @param pageNum   页码
     * @param pageSize  每页大小
     * @return 分页结果
     */
    PageResult<OperationLog> pageLogs(String module, String username, Integer pageNum, Integer pageSize);

    /**
     * 清空操作日志（保留最近N天）
     *
     * @param keepDays 保留天数
     * @return 删除记录数
     */
    int cleanLogs(int keepDays);
}
