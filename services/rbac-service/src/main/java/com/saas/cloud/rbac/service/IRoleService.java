package com.saas.cloud.rbac.service;

import com.saas.cloud.rbac.api.dto.RoleCreateDTO;
import com.saas.cloud.rbac.api.dto.RoleUpdateDTO;
import com.saas.cloud.rbac.api.vo.RoleVO;
import com.saas.cloud.rbac.entity.Role;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 角色表 服务类
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
public interface IRoleService extends IService<Role> {

    /**
     * 查询当前租户的角色列表
     *
     * @return 角色VO列表
     */
    List<RoleVO> listRoles();

    /**
     * 创建角色，同时创建RoleMenu关联
     *
     * @param dto 角色创建请求
     */
    void createRole(RoleCreateDTO dto);

    /**
     * 更新角色基本信息
     *
     * @param roleId 角色ID
     * @param dto    角色更新请求
     */
    void updateRole(Long roleId, RoleUpdateDTO dto);

    /**
     * 逻辑删除角色，检查是否有用户关联
     *
     * @param roleId 角色ID
     */
    void deleteRole(Long roleId);

    /**
     * 分配菜单权限（先删后增RoleMenu）
     *
     * @param roleId  角色ID
     * @param menuIds 菜单ID列表
     */
    void assignMenus(Long roleId, List<Long> menuIds);

    /**
     * 设置数据范围，自定义范围时维护RoleDept
     *
     * @param roleId    角色ID
     * @param dataScope 数据范围
     * @param deptIds   部门ID列表（自定义范围时使用）
     */
    void updateDataScope(Long roleId, Byte dataScope, List<Long> deptIds);
}
