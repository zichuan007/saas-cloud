package com.saas.cloud.platform.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.cloud.common.core.enums.TenantStatusEnum;
import com.saas.cloud.platform.api.vo.TenantVO;
import com.saas.cloud.platform.entity.Package;
import com.saas.cloud.platform.entity.Tenant;
import com.saas.cloud.platform.service.IPackageService;
import com.saas.cloud.platform.service.ITenantService;

import lombok.extern.slf4j.Slf4j;

/**
 * 租户缓存服务
 * 使用 Redis Hash 结构缓存租户信息，避免每次登录/配额校验都查库。
 * Key 结构：
 * - tenant:cache（Hash，field=tenantId，value=TenantVO JSON）
 * - tenant:code:index（Hash，field=tenantCode，value=tenantId）
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-20
 */
@Slf4j
@Service
public class TenantCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    private final ITenantService tenantService;

    private final IPackageService packageService;

    private final ObjectMapper objectMapper;

    @Autowired
    public TenantCacheService(RedisTemplate<String, Object> redisTemplate,
                               @Lazy ITenantService tenantService,
                               IPackageService packageService,
                               ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.tenantService = tenantService;
        this.packageService = packageService;
        this.objectMapper = objectMapper;
    }

    private static final String CACHE_KEY = "tenant:cache";

    private static final String CODE_INDEX_KEY = "tenant:code:index";

    /**
     * 应用启动后预热租户缓存
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {

        log.info("开始预热租户缓存...");
        refreshAll();
        log.info("租户缓存预热完成");
    }

    /**
     * 根据租户ID从缓存获取租户信息
     *
     * @param tenantId 租户ID
     * @return 租户视图对象，未命中时查库并写入缓存
     */
    public TenantVO getTenantById(Long tenantId) {

        Object cached = redisTemplate.opsForHash().get(CACHE_KEY, String.valueOf(tenantId));
        if (cached != null) {
            return deserialize(cached.toString());
        }
        // 缓存未命中，查库并写入
        Tenant tenant = tenantService.getById(tenantId);
        if (tenant == null) {
            return null;
        }
        TenantVO vo = convertToVO(tenant);
        putCache(vo);
        return vo;
    }

    /**
     * 根据租户编码从缓存获取租户信息
     *
     * @param tenantCode 租户编码
     * @return 租户视图对象
     */
    public TenantVO getTenantByCode(String tenantCode) {

        Object tenantIdObj = redisTemplate.opsForHash().get(CODE_INDEX_KEY, tenantCode);
        if (tenantIdObj != null) {
            return getTenantById(Long.valueOf(tenantIdObj.toString()));
        }
        // 索引未命中，查库
        Tenant tenant = tenantService.getByTenantCode(tenantCode);
        if (tenant == null) {
            return null;
        }
        TenantVO vo = convertToVO(tenant);
        putCache(vo);
        return vo;
    }

    /**
     * 失效单条租户缓存
     *
     * @param tenantId 租户ID
     */
    public void evictTenant(Long tenantId) {
        // 先获取 tenantCode 用于删除索引
        Object cached = redisTemplate.opsForHash().get(CACHE_KEY, String.valueOf(tenantId));
        if (cached != null) {
            TenantVO vo = deserialize(cached.toString());
            if (vo != null && vo.getTenantCode() != null) {
                redisTemplate.opsForHash().delete(CODE_INDEX_KEY, vo.getTenantCode());
            }
        }
        redisTemplate.opsForHash().delete(CACHE_KEY, String.valueOf(tenantId));
        log.debug("失效租户缓存, tenantId={}", tenantId);
    }

    /**
     * 全量刷新租户缓存
     */
    public void refreshAll() {

        List<Tenant> tenants = tenantService.list();
        // 批量查询套餐
        List<Long> packageIds = tenants.stream()
                .map(Tenant::getPackageId)
                .filter(pid -> pid != null)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Package> packageMap = packageIds.isEmpty()
                ? java.util.Collections.emptyMap()
                : packageService.listByIds(packageIds).stream()
                .collect(Collectors.toMap(Package::getId, p -> p));

        // 清除旧缓存
        redisTemplate.delete(CACHE_KEY);
        redisTemplate.delete(CODE_INDEX_KEY);

        // 写入新缓存
        for (Tenant tenant : tenants) {
            TenantVO vo = convertToVO(tenant, packageMap);
            putCache(vo);
        }
        log.info("全量刷新租户缓存完成, 共 {} 条", tenants.size());
    }

    private void putCache(TenantVO vo) {

        String json = serialize(vo);
        if (json != null) {
            redisTemplate.opsForHash().put(CACHE_KEY, String.valueOf(vo.getId()), json);
            if (vo.getTenantCode() != null) {
                redisTemplate.opsForHash().put(CODE_INDEX_KEY, vo.getTenantCode(), String.valueOf(vo.getId()));
            }
        }
    }

    private TenantVO convertToVO(Tenant tenant) {

        return convertToVO(tenant, java.util.Collections.emptyMap());
    }

    private TenantVO convertToVO(Tenant tenant, Map<Long, Package> packageMap) {

        TenantVO vo = new TenantVO();
        vo.setId(tenant.getId());
        vo.setTenantCode(tenant.getTenantCode());
        vo.setTenantName(tenant.getTenantName());
        vo.setContactName(tenant.getContactPerson());
        vo.setContactPhone(tenant.getContactPhone());
        vo.setContactEmail(tenant.getContactEmail());
        vo.setStatus(tenant.getStatus().intValue());
        vo.setStatusDesc(TenantStatusEnum.of(tenant.getStatus()).getDesc());
        vo.setCreateTime(tenant.getCreateTime());

        if (tenant.getStatus() == (byte) TenantStatusEnum.TRIAL.getCode()) {
            vo.setExpireTime(tenant.getTrialExpireTime());
        } else {
            vo.setExpireTime(tenant.getPaidExpireTime());
        }

        vo.setPackageId(tenant.getPackageId());
        if (tenant.getPackageId() != null && packageMap.containsKey(tenant.getPackageId())) {
            Package pkg = packageMap.get(tenant.getPackageId());
            vo.setPackageName(pkg.getPackageName());
            vo.setMaxUsers(pkg.getMaxUsers());
            vo.setMenuIds(pkg.getMenuIds());
        }
        return vo;
    }

    private String serialize(TenantVO vo) {

        try {
            return objectMapper.writeValueAsString(vo);
        } catch (JsonProcessingException e) {
            log.error("序列化租户信息失败, tenantId={}", vo.getId(), e);
            return null;
        }
    }

    private TenantVO deserialize(String json) {

        try {
            return objectMapper.readValue(json, TenantVO.class);
        } catch (JsonProcessingException e) {
            log.error("反序列化租户信息失败", e);
            return null;
        }
    }

}
