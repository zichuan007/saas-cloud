package com.saas.cloud.rbac.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.saas.cloud.rbac.entity.RoleDept;
import com.saas.cloud.rbac.mapper.RoleDeptMapper;
import com.saas.cloud.rbac.service.IRoleDeptService;

/**
 * 角色部门关联表 服务实现类
 *
 * @author saas-cloud
 * @since 2026-05-18
 */
@Service
public class RoleDeptServiceImpl extends ServiceImpl<RoleDeptMapper, RoleDept> implements IRoleDeptService {

}
