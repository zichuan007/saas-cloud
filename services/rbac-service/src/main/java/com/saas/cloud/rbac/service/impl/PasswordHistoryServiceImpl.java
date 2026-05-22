package com.saas.cloud.rbac.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.saas.cloud.rbac.entity.PasswordHistory;
import com.saas.cloud.rbac.mapper.PasswordHistoryMapper;
import com.saas.cloud.rbac.service.IPasswordHistoryService;

/**
 * 密码历史表 服务实现类
 *
 * @author saas-cloud
 * @since 2026-05-18
 */
@Service
public class PasswordHistoryServiceImpl extends ServiceImpl<PasswordHistoryMapper, PasswordHistory> implements IPasswordHistoryService {

}
