package com.saas.cloud.rbac.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.security.context.TenantContext;
import com.saas.cloud.rbac.api.dto.DeptCreateDTO;
import com.saas.cloud.rbac.api.dto.DeptUpdateDTO;
import com.saas.cloud.rbac.api.vo.DeptTreeVO;
import com.saas.cloud.rbac.service.IDeptService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 部门管理 前端控制器
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@RestController
@RequestMapping("/dept")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DeptController {

    private final IDeptService deptService;

    /**
     * 获取部门树形列表
     *
     * @return 部门树
     */
    @GetMapping("/tree")
    public ApiResult<List<DeptTreeVO>> tree() {
        Long tenantId = TenantContext.getTenantId();
        return ApiResult.ok(deptService.buildDeptTree(tenantId));
    }

    /**
     * 创建部门
     *
     * @param dto 部门创建请求
     * @return 操作结果
     */
    @PostMapping
    public ApiResult<Void> create(@Valid @RequestBody DeptCreateDTO dto) {
        deptService.createDept(dto);
        return ApiResult.ok();
    }

    /**
     * 更新部门
     *
     * @param id  部门ID
     * @param dto 部门更新请求
     * @return 操作结果
     */
    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable("id") Long id, @Valid @RequestBody DeptUpdateDTO dto) {
        dto.setId(id);
        deptService.updateDept(dto);
        return ApiResult.ok();
    }

    /**
     * 删除部门（有子部门或用户时禁止）
     *
     * @param id 部门ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable("id") Long id) {
        deptService.deleteDept(id);
        return ApiResult.ok();
    }
}
