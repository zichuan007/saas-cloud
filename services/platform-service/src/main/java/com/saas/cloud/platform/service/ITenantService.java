package com.saas.cloud.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.platform.api.dto.TenantCreateDTO;
import com.saas.cloud.platform.api.dto.TenantQueryDTO;
import com.saas.cloud.platform.api.vo.TenantVO;
import com.saas.cloud.platform.entity.Tenant;

/**
 * 租户服务接口
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
public interface ITenantService extends IService<Tenant> {

    /**
     * 创建租户
     *
     * @param dto 租户创建请求
     */
    void createTenant(TenantCreateDTO dto);

    /**
     * 创建租户并返回租户实体（供内部注册接口使用）
     *
     * @param dto 租户创建请求
     * @return 新创建的租户实体
     */
    Tenant createTenantAndReturn(TenantCreateDTO dto);

    /**
     * 冻结租户
     *
     * @param tenantId 租户ID
     */
    void freezeTenant(Long tenantId);

    /**
     * 解冻租户
     *
     * @param tenantId 租户ID
     */
    void unfreezeTenant(Long tenantId);

    /**
     * 根据租户编码查询租户
     *
     * @param tenantCode 租户编码
     * @return 租户实体，不存在返回 null
     */
    Tenant getByTenantCode(String tenantCode);

    /**
     * 分页查询租户列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<TenantVO> pageTenants(TenantQueryDTO query);
}
