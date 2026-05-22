package com.saas.cloud.rbac.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.saas.cloud.rbac.entity.UserRole;
import com.saas.cloud.rbac.mapper.UserRoleMapper;
import com.saas.cloud.rbac.service.IUserRoleService;

/**
 * 用户角色关联表 服务实现类
 *
 * @author saas-cloud
 * @since 2026-05-18
 */
@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements IUserRoleService {

}
