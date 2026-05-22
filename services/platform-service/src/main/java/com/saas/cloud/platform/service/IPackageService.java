package com.saas.cloud.platform.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.saas.cloud.platform.api.dto.PackageCreateDTO;
import com.saas.cloud.platform.entity.Package;

/**
 * 套餐服务接口
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
public interface IPackageService extends IService<Package> {

    /**
     * 查询所有启用的套餐列表，按排序字段排序
     *
     * @return 套餐列表
     */
    List<Package> listPackages();

    /**
     * 创建套餐
     *
     * @param dto 套餐创建请求
     */
    void createPackage(PackageCreateDTO dto);

    /**
     * 更新套餐
     *
     * @param id  套餐ID
     * @param dto 套餐更新请求
     */
    void updatePackage(Long id, PackageCreateDTO dto);

    /**
     * 启用/禁用套餐
     *
     * @param id     套餐ID
     * @param status 状态 0-禁用 1-启用
     */
    void updateStatus(Long id, Byte status);
}
