package com.saas.cloud.common.data.tenant.job;

import java.util.List;

/**
 * 租户框架服务接口，提供租户ID列表查询能力
 * <p>由各业务服务实现（通常基于 PlatformFeignClient 获取）</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
public interface TenantFrameworkService {

    /**
     * 获取所有启用状态的租户ID列表
     *
     * @return 租户ID列表
     */
    List<Long> getActiveTenantIds();
}
