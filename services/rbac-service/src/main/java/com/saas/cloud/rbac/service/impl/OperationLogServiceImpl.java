package com.saas.cloud.rbac.service.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.rbac.entity.OperationLog;
import com.saas.cloud.rbac.mapper.OperationLogMapper;
import com.saas.cloud.rbac.service.IOperationLogService;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 操作日志表 服务实现类
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@Service
public class OperationLogServiceImpl
        extends ServiceImpl<OperationLogMapper, OperationLog>
        implements IOperationLogService {

    @Override
    public PageResult<OperationLog> pageLogs(String module, String username, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(module), OperationLog::getModule, module)
                .like(StrUtil.isNotBlank(username), OperationLog::getUsername, username)
                .orderByDesc(OperationLog::getCreateTime);

        Page<OperationLog> page = new Page<>(pageNum, pageSize);
        page(page, wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), pageNum, pageSize);
    }

    @Override
    public int cleanLogs(int keepDays) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(keepDays);
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(OperationLog::getCreateTime, threshold);
        int deleted = baseMapper.delete(wrapper);
        log.info("清理操作日志, 保留最近{}天, 删除{}条", keepDays, deleted);
        return deleted;
    }
}
