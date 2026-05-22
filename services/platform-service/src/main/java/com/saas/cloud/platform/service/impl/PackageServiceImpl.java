package com.saas.cloud.platform.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.saas.cloud.common.core.exception.BusinessException;
import com.saas.cloud.common.core.result.ResultCode;
import com.saas.cloud.platform.api.dto.PackageCreateDTO;
import com.saas.cloud.platform.entity.Package;
import com.saas.cloud.platform.mapper.PackageMapper;
import com.saas.cloud.platform.service.IPackageService;

import lombok.extern.slf4j.Slf4j;

/**
 * 套餐服务实现类
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@Service
public class PackageServiceImpl extends ServiceImpl<PackageMapper, Package> implements IPackageService {

    @Override
    public List<Package> listPackages() {
        return this.lambdaQuery()
                .eq(Package::getStatus, (byte) 1)
                .orderByAsc(Package::getSortOrder)
                .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPackage(PackageCreateDTO dto) {
        // 检查 packageCode 唯一性
        long count = this.lambdaQuery()
                .eq(Package::getPackageCode, dto.getPackageCode())
                .count();
        if (count > 0) {
            throw new BusinessException(ResultCode.CONFLICT, "套餐编码已存在: " + dto.getPackageCode());
        }

        Package pkg = new Package();
        pkg.setPackageName(dto.getPackageName());
        pkg.setPackageCode(dto.getPackageCode());
        pkg.setPriceMonthly(dto.getPriceMonthly());
        pkg.setPriceYearly(dto.getPriceYearly());
        pkg.setMaxUsers(dto.getMaxUsers());
        pkg.setMaxRoles(dto.getMaxRoles());
        pkg.setMaxDepts(dto.getMaxDepts());
        pkg.setMaxProcessDefinitions(dto.getMaxProcessDefinitions() != null ? dto.getMaxProcessDefinitions() : 0);
        pkg.setMaxWechatAccounts(dto.getMaxWechatAccounts() != null ? dto.getMaxWechatAccounts() : 0);
        pkg.setMaxStorageMb(dto.getMaxStorageMb() != null ? dto.getMaxStorageMb() : 0L);
        pkg.setMenuIds(dto.getMenuIds());
        pkg.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        pkg.setStatus((byte) 1);

        this.save(pkg);
        log.info("创建套餐成功, packageId={}, packageCode={}", pkg.getId(), pkg.getPackageCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePackage(Long id, PackageCreateDTO dto) {
        Package pkg = this.getById(id);
        if (pkg == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "套餐不存在, id=" + id);
        }

        // 检查 packageCode 唯一性（排除自身）
        long count = this.lambdaQuery()
                .eq(Package::getPackageCode, dto.getPackageCode())
                .ne(Package::getId, id)
                .count();
        if (count > 0) {
            throw new BusinessException(ResultCode.CONFLICT, "套餐编码已存在: " + dto.getPackageCode());
        }

        pkg.setPackageName(dto.getPackageName());
        pkg.setPackageCode(dto.getPackageCode());
        pkg.setPriceMonthly(dto.getPriceMonthly());
        pkg.setPriceYearly(dto.getPriceYearly());
        pkg.setMaxUsers(dto.getMaxUsers());
        pkg.setMaxRoles(dto.getMaxRoles());
        pkg.setMaxDepts(dto.getMaxDepts());
        pkg.setMaxProcessDefinitions(dto.getMaxProcessDefinitions() != null ? dto.getMaxProcessDefinitions() : 0);
        pkg.setMaxWechatAccounts(dto.getMaxWechatAccounts() != null ? dto.getMaxWechatAccounts() : 0);
        pkg.setMaxStorageMb(dto.getMaxStorageMb() != null ? dto.getMaxStorageMb() : 0L);
        pkg.setMenuIds(dto.getMenuIds());
        pkg.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);

        this.updateById(pkg);
        log.info("更新套餐成功, packageId={}, packageCode={}", id, dto.getPackageCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Byte status) {
        Package pkg = this.getById(id);
        if (pkg == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "套餐不存在, id=" + id);
        }
        pkg.setStatus(status);
        this.updateById(pkg);
        log.info("更新套餐状态成功, packageId={}, status={}", id, status);
    }
}
