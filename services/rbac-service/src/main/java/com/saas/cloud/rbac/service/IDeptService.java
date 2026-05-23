package com.saas.cloud.rbac.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.saas.cloud.rbac.api.dto.DeptCreateDTO;
import com.saas.cloud.rbac.api.dto.DeptUpdateDTO;
import com.saas.cloud.rbac.api.vo.DeptTreeVO;
import com.saas.cloud.rbac.entity.Dept;

/**
 * 部门表 服务类
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
public interface IDeptService extends IService<Dept> {

    /**
     * 构建部门树形结构（租户隔离由拦截器自动处理）
     *
     * @return 部门树列表
     */
    List<DeptTreeVO> buildDeptTree();

    /**
     * 创建部门，自动维护 ancestors 祖先链
     *
     * @param dto 部门创建请求
     */
    void createDept(DeptCreateDTO dto);

    /**
     * 更新部门，若 parentId 变更则同步更新本部门及所有子部门的 ancestors
     *
     * @param dto 部门更新请求
     */
    void updateDept(DeptUpdateDTO dto);

    /**
     * 删除部门，有子部门或用户时禁止删除
     *
     * @param deptId 部门ID
     */
    void deleteDept(Long deptId);
}
