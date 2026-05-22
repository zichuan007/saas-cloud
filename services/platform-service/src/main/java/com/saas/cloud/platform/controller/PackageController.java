package com.saas.cloud.platform.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.platform.api.dto.PackageCreateDTO;
import com.saas.cloud.platform.entity.Package;
import com.saas.cloud.platform.service.IPackageService;

import lombok.RequiredArgsConstructor;

/**
 * 套餐管理控制器
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@RestController
@RequestMapping("/package")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class PackageController {

    private final IPackageService packageService;

    /**
     * 查询启用的套餐列表
     *
     * @return 套餐列表
     */
    @GetMapping("/list")
    public ApiResult<List<Package>> listPackages() {
        return ApiResult.ok(packageService.listPackages());
    }

    /**
     * 创建套餐
     *
     * @param dto 套餐创建请求
     * @return 操作结果
     */
    @PostMapping
    public ApiResult<Void> createPackage(@Validated @RequestBody PackageCreateDTO dto) {
        packageService.createPackage(dto);
        return ApiResult.ok();
    }

    /**
     * 更新套餐
     *
     * @param id  套餐ID
     * @param dto 套餐更新请求
     * @return 操作结果
     */
    @PutMapping("/{id}")
    public ApiResult<Void> updatePackage(@PathVariable("id") Long id,
                                         @Validated @RequestBody PackageCreateDTO dto) {
        packageService.updatePackage(id, dto);
        return ApiResult.ok();
    }

    /**
     * 启用/禁用套餐
     *
     * @param id     套餐ID
     * @param status 状态 0-禁用 1-启用
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    public ApiResult<Void> updateStatus(@PathVariable("id") Long id,
                                        @RequestParam("status") Byte status) {
        packageService.updateStatus(id, status);
        return ApiResult.ok();
    }
}
