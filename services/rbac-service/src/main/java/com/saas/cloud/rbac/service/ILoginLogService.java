package com.saas.cloud.rbac.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.rbac.api.dto.LoginLogQueryDTO;
import com.saas.cloud.rbac.entity.LoginLog;
import com.saas.cloud.rbac.api.vo.LoginLogVO;

/**
 * 登录日志 服务接口
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
public interface ILoginLogService extends IService<LoginLog> {

    /**
     * 分页查询登录日志
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<LoginLogVO> queryPage(LoginLogQueryDTO queryDTO);

    /**
     * 记录登录日志
     *
     * @param loginLog 登录日志实体
     */
    void recordLoginLog(LoginLog loginLog);

    /**
     * 清理登录日志（保留最近N天）
     *
     * @param keepDays 保留天数
     * @return 清理条数
     */
    int cleanLogs(int keepDays);
}
