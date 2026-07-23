package com.saas.cloud.rbac.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.common.data.annotation.DataScope;
import com.saas.cloud.common.security.context.TenantContext;
import com.saas.cloud.rbac.api.dto.LoginLogQueryDTO;
import com.saas.cloud.rbac.entity.LoginLog;
import com.saas.cloud.rbac.mapper.LoginLogMapper;
import com.saas.cloud.rbac.service.ILoginLogService;
import com.saas.cloud.rbac.api.vo.LoginLogVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 登录日志 服务实现
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class LoginLogServiceImpl extends ServiceImpl<LoginLogMapper, LoginLog> implements ILoginLogService {

    @DataScope
    @Override
    public PageResult<LoginLogVO> queryPage(LoginLogQueryDTO queryDTO) {
        Page<LoginLog> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<LoginLog> wrapper = buildQueryWrapper(queryDTO);
        Page<LoginLog> result = baseMapper.selectPage(page, wrapper);
        List<LoginLogVO> voList = result.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return PageResult.of(voList, result.getTotal(), queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    @Async
    @Override
    public void recordLoginLog(LoginLog loginLog) {
        try {
            // @Async 线程未经 TtlExecutors 包装，上下文不会透传；
            // 从实体 tenantId 还原上下文，避免 INSERT 被 MyBatis-Plus 追加 tenant_id=-1
            if (loginLog.getTenantId() != null) {
                TenantContext.TenantInfo info = new TenantContext.TenantInfo();
                info.setTenantId(loginLog.getTenantId());
                TenantContext.set(info);
            }
            baseMapper.insert(loginLog);
        } catch (Exception e) {
            log.error("记录登录日志失败: {}", e.getMessage(), e);
        } finally {
            TenantContext.clear();
        }
    }

    @Override
    public int cleanLogs(int keepDays) {
        LocalDateTime deadline = LocalDateTime.now().minusDays(keepDays);
        LambdaQueryWrapper<LoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(LoginLog::getLoginTime, deadline);
        return baseMapper.delete(wrapper);
    }

    private LambdaQueryWrapper<LoginLog> buildQueryWrapper(LoginLogQueryDTO queryDTO) {
        LambdaQueryWrapper<LoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(queryDTO.getUsername() != null && !queryDTO.getUsername().isEmpty(),
                        LoginLog::getUsername, queryDTO.getUsername())
                .eq(queryDTO.getStatus() != null, LoginLog::getStatus, queryDTO.getStatus())
                .like(queryDTO.getIp() != null && !queryDTO.getIp().isEmpty(),
                        LoginLog::getIp, queryDTO.getIp())
                .ge(queryDTO.getBeginTime() != null, LoginLog::getLoginTime, queryDTO.getBeginTime())
                .le(queryDTO.getEndTime() != null, LoginLog::getLoginTime, queryDTO.getEndTime())
                .orderByDesc(LoginLog::getLoginTime);
        return wrapper;
    }

    private LoginLogVO toVO(LoginLog entity) {
        LoginLogVO vo = new LoginLogVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
