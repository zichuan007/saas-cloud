package com.saas.cloud.rbac.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.saas.cloud.rbac.entity.RoleMenu;
import com.saas.cloud.rbac.mapper.RoleMenuMapper;
import com.saas.cloud.rbac.service.IRoleMenuService;

/**
 * 角色菜单关联表 服务实现类
 *
 * @author saas-cloud
 * @since 2026-05-18
 */
@Service
public class RoleMenuServiceImpl extends ServiceImpl<RoleMenuMapper, RoleMenu> implements IRoleMenuService {

}
