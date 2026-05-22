package com.saas.cloud.rbac.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.cloud.common.core.exception.BusinessException;
import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.core.result.ResultCode;
import com.saas.cloud.common.security.context.TenantContext;
import com.saas.cloud.platform.api.feign.PlatformFeignClient;
import com.saas.cloud.platform.api.vo.TenantVO;
import com.saas.cloud.rbac.api.dto.RoleCreateDTO;
import com.saas.cloud.rbac.api.dto.RoleUpdateDTO;
import com.saas.cloud.rbac.api.vo.RoleVO;
import com.saas.cloud.rbac.entity.Role;
import com.saas.cloud.rbac.entity.RoleDept;
import com.saas.cloud.rbac.entity.RoleMenu;
import com.saas.cloud.rbac.entity.UserRole;
import com.saas.cloud.rbac.mapper.RoleDeptMapper;
import com.saas.cloud.rbac.mapper.RoleMapper;
import com.saas.cloud.rbac.mapper.RoleMenuMapper;
import com.saas.cloud.rbac.mapper.UserRoleMapper;
import com.saas.cloud.rbac.service.IRoleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 角色表 服务实现类
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements IRoleService {

    private final RoleMenuMapper roleMenuMapper;
    private final RoleDeptMapper roleDeptMapper;
    private final UserRoleMapper userRoleMapper;
    private final PlatformFeignClient platformFeignClient;
    private final ObjectMapper objectMapper;

    /** 自定义数据范围 */
    private static final byte DATA_SCOPE_CUSTOM = 5;

    @Override
    public List<RoleVO> listRoles() {
        Long tenantId = TenantContext.getTenantId();
        log.info("查询角色列表, tenantId={}", tenantId);

        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(tenantId != null, Role::getTenantId, tenantId)
                .orderByAsc(Role::getSortOrder);
        List<Role> roleList = this.list(queryWrapper);

        return roleList.stream()
                .map(role -> {
                    RoleVO vo = new RoleVO();
                    vo.setId(role.getId());
                    vo.setRoleName(role.getRoleName());
                    vo.setRoleCode(role.getRoleCode());
                    vo.setRoleLevel(role.getRoleLevel() != null ? role.getRoleLevel().intValue() : null);
                    vo.setDataScope(role.getDataScope() != null ? role.getDataScope().intValue() : null);
                    vo.setSortOrder(role.getSortOrder());
                    vo.setStatus(role.getStatus() != null ? role.getStatus().intValue() : null);

                    // 查询角色关联的菜单ID
                    List<RoleMenu> roleMenus = roleMenuMapper.selectList(
                            new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, role.getId()));
                    List<Long> menuIds = roleMenus.stream()
                            .map(RoleMenu::getMenuId)
                            .collect(Collectors.toList());
                    vo.setMenuIds(menuIds);

                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createRole(RoleCreateDTO dto) {
        log.info("创建角色, roleName={}", dto.getRoleName());

        // 配额校验（非核心逻辑，Feign 调用失败时降级放行）
        Long tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            try {
                long currentRoleCount = this.count(new LambdaQueryWrapper<Role>()
                        .eq(Role::getTenantId, tenantId));
                ApiResult<Boolean> quotaResult = platformFeignClient.checkQuota(
                        tenantId, "ROLE", (int) currentRoleCount);
                if (quotaResult.isSuccess() && Boolean.FALSE.equals(quotaResult.getData())) {
                    throw new BusinessException(ResultCode.QUOTA_EXCEEDED, "角色数已达套餐上限，请升级套餐");
                }
            } catch (BusinessException e) {
                throw e;
            } catch (Exception e) {
                log.warn("角色配额校验异常, 降级放行, tenantId={}, error={}", tenantId, e.getMessage());
            }
        }

        Role role = new Role();
        role.setRoleName(dto.getRoleName());
        role.setRoleCode(dto.getRoleCode());
        role.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        role.setDataScope(dto.getDataScope() != null ? dto.getDataScope().byteValue() : 4);
        role.setStatus((byte) 1);
        role.setRoleLevel((byte) 2);
        role.setIsSystem((byte) 0);
        this.save(role);
        log.info("角色创建成功, id={}", role.getId());

        // 校验并创建角色菜单关联
        validateMenuIdsInPackage(dto.getMenuIds());
        saveRoleMenus(role.getId(), dto.getMenuIds());

        // 如果是自定义数据范围，维护RoleDept
        if (role.getDataScope() == DATA_SCOPE_CUSTOM) {
            saveRoleDepts(role.getId(), dto.getDeptIds());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(Long roleId, RoleUpdateDTO dto) {
        log.info("更新角色, roleId={}", roleId);
        Role role = this.getById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        // 系统内置角色不允许修改
        if (role.getIsSystem() != null && role.getIsSystem() == 1) {
            throw new BusinessException("系统内置角色不允许修改");
        }

        role.setRoleName(dto.getRoleName());
        if (dto.getRoleCode() != null) {
            role.setRoleCode(dto.getRoleCode());
        }
        if (dto.getSortOrder() != null) {
            role.setSortOrder(dto.getSortOrder());
        }
        if (dto.getStatus() != null) {
            role.setStatus(dto.getStatus());
        }
        this.updateById(role);
        log.info("角色更新成功, roleId={}", roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long roleId) {
        log.info("删除角色, roleId={}", roleId);
        Role role = this.getById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        // 系统内置角色不允许删除
        if (role.getIsSystem() != null && role.getIsSystem() == 1) {
            throw new BusinessException("系统内置角色不允许删除");
        }

        // 检查是否有用户关联
        long userCount = userRoleMapper.selectCount(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getRoleId, roleId));
        if (userCount > 0) {
            throw new BusinessException("该角色已分配给用户，无法删除");
        }

        // 逻辑删除角色
        this.removeById(roleId);

        // 删除角色菜单关联
        roleMenuMapper.delete(new LambdaQueryWrapper<RoleMenu>()
                .eq(RoleMenu::getRoleId, roleId));

        // 删除角色部门关联
        roleDeptMapper.delete(new LambdaQueryWrapper<RoleDept>()
                .eq(RoleDept::getRoleId, roleId));

        log.info("角色删除成功, roleId={}", roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(Long roleId, List<Long> menuIds) {
        log.info("分配菜单权限, roleId={}, menuIds={}", roleId, menuIds);
        Role role = this.getById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        // 校验菜单是否在套餐范围内
        validateMenuIdsInPackage(menuIds);

        // 先删后增
        roleMenuMapper.delete(new LambdaQueryWrapper<RoleMenu>()
                .eq(RoleMenu::getRoleId, roleId));
        saveRoleMenus(roleId, menuIds);
        log.info("菜单权限分配成功, roleId={}", roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDataScope(Long roleId, Byte dataScope, List<Long> deptIds) {
        log.info("设置数据范围, roleId={}, dataScope={}", roleId, dataScope);
        Role role = this.getById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        role.setDataScope(dataScope);
        this.updateById(role);

        // 先删除原有的角色部门关联
        roleDeptMapper.delete(new LambdaQueryWrapper<RoleDept>()
                .eq(RoleDept::getRoleId, roleId));

        // 自定义数据范围时维护RoleDept
        if (dataScope == DATA_SCOPE_CUSTOM) {
            saveRoleDepts(roleId, deptIds);
        }
        log.info("数据范围设置成功, roleId={}", roleId);
    }

    /**
     * 保存角色菜单关联
     *
     * @param roleId  角色ID
     * @param menuIds 菜单ID列表
     */
    private void saveRoleMenus(Long roleId, List<Long> menuIds) {
        if (CollectionUtils.isEmpty(menuIds)) {
            return;
        }
        for (Long menuId : menuIds) {
            RoleMenu roleMenu = new RoleMenu();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            roleMenuMapper.insert(roleMenu);
        }
    }

    /**
     * 校验菜单ID是否在租户套餐可见范围内
     *
     * @param menuIds 待分配的菜单ID列表
     */
    private void validateMenuIdsInPackage(List<Long> menuIds) {
        if (CollectionUtils.isEmpty(menuIds)) {
            return;
        }
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return;
        }
        try {
            ApiResult<TenantVO> result = platformFeignClient.getTenantInfo(tenantId);
            if (result == null || result.getData() == null) {
                return;
            }
            String packageMenuIds = result.getData().getMenuIds();
            if (!StringUtils.hasText(packageMenuIds)) {
                return;
            }
            List<Long> allowedList = objectMapper.readValue(packageMenuIds, new TypeReference<List<Long>>() {});
            Set<Long> allowedSet = new HashSet<>(allowedList);
            List<Long> illegal = menuIds.stream()
                    .filter(id -> !allowedSet.contains(id))
                    .collect(Collectors.toList());
            if (!illegal.isEmpty()) {
                throw new BusinessException("菜单超出套餐范围: " + illegal);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("校验套餐菜单范围异常, 降级放行, tenantId={}, error={}", tenantId, e.getMessage());
        }
    }

    /**
     * 保存角色部门关联
     *
     * @param roleId  角色ID
     * @param deptIds 部门ID列表
     */
    private void saveRoleDepts(Long roleId, List<Long> deptIds) {
        if (CollectionUtils.isEmpty(deptIds)) {
            return;
        }
        for (Long deptId : deptIds) {
            RoleDept roleDept = new RoleDept();
            roleDept.setRoleId(roleId);
            roleDept.setDeptId(deptId);
            roleDeptMapper.insert(roleDept);
        }
    }
}
