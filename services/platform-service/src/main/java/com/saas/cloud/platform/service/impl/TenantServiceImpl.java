package com.saas.cloud.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.saas.cloud.common.core.enums.TenantStatusEnum;
import com.saas.cloud.common.core.exception.BusinessException;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.common.core.result.ResultCode;
import com.saas.cloud.platform.api.dto.TenantCreateDTO;
import com.saas.cloud.platform.api.dto.TenantQueryDTO;
import com.saas.cloud.platform.api.vo.TenantVO;
import com.saas.cloud.platform.entity.GlobalConfig;
import com.saas.cloud.platform.entity.Package;
import com.saas.cloud.platform.entity.Tenant;
import com.saas.cloud.platform.mapper.TenantMapper;
import com.saas.cloud.platform.service.IGlobalConfigService;
import com.saas.cloud.platform.service.IPackageService;
import com.saas.cloud.platform.service.ITenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 租户服务实现类
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class TenantServiceImpl extends ServiceImpl<TenantMapper, Tenant> implements ITenantService {

    private final IGlobalConfigService globalConfigService;
    private final IPackageService packageService;
    private final RedisTemplate<String, Object> redisTemplate;

    /** 全局配置键：试用天数 */
    private static final String CONFIG_KEY_TRIAL_DAYS = "trial_days";
    /** 全局配置键：默认套餐ID */
    private static final String CONFIG_KEY_DEFAULT_PACKAGE_ID = "default_package_id";
    /** 默认试用天数 */
    private static final int DEFAULT_TRIAL_DAYS = 15;
    /** 租户编码前缀 */
    private static final String TENANT_CODE_PREFIX = "T";
    /** 租户编码日期格式 */
    private static final DateTimeFormatter CODE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    /** Redis 冻结标记 key 前缀 */
    private static final String REDIS_FROZEN_KEY_PREFIX = "tenant:frozen:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createTenant(TenantCreateDTO dto) {
        createTenantAndReturn(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Tenant createTenantAndReturn(TenantCreateDTO dto) {
        // 读取全局配置
        int trialDays = getConfigIntValue(CONFIG_KEY_TRIAL_DAYS, DEFAULT_TRIAL_DAYS);
        Long defaultPackageId = getConfigLongValue(CONFIG_KEY_DEFAULT_PACKAGE_ID, null);

        Tenant tenant = new Tenant();
        tenant.setTenantName(dto.getTenantName());
        // 生成租户编码: T + yyyyMMdd + 3位序号
        tenant.setTenantCode(generateTenantCode());
        tenant.setContactPerson(dto.getContactName());
        tenant.setContactPhone(dto.getContactPhone());
        tenant.setContactEmail(dto.getContactEmail());
        tenant.setStatus((byte) TenantStatusEnum.TRIAL.getCode());
        tenant.setTrialExpireTime(LocalDateTime.now().plusDays(trialDays));

        // 套餐优先使用传入值，否则用默认配置
        Long packageId = dto.getPackageId() != null ? dto.getPackageId() : defaultPackageId;
        tenant.setPackageId(packageId);

        this.save(tenant);
        log.info("创建租户成功, tenantId={}, tenantCode={}, tenantName={}, trialDays={}",
                tenant.getId(), tenant.getTenantCode(), tenant.getTenantName(), trialDays);
        return tenant;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void freezeTenant(Long tenantId) {
        Tenant tenant = getById(tenantId);
        if (tenant == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "租户不存在, tenantId=" + tenantId);
        }
        // 只有试用或正常状态可冻结
        byte currentStatus = tenant.getStatus();
        if (currentStatus != (byte) TenantStatusEnum.TRIAL.getCode()
                && currentStatus != (byte) TenantStatusEnum.ACTIVE.getCode()) {
            throw new BusinessException(ResultCode.CONFLICT,
                    "当前状态不允许冻结, tenantId=" + tenantId + ", status=" + currentStatus);
        }
        tenant.setStatus((byte) TenantStatusEnum.FROZEN.getCode());
        tenant.setFrozenTime(LocalDateTime.now());
        this.updateById(tenant);

        // 发布租户冻结事件到 Redis
        String redisKey = REDIS_FROZEN_KEY_PREFIX + tenantId;
        redisTemplate.opsForValue().set(redisKey, "1");
        log.info("冻结租户成功, tenantId={}", tenantId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfreezeTenant(Long tenantId) {
        Tenant tenant = getById(tenantId);
        if (tenant == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "租户不存在, tenantId=" + tenantId);
        }
        if (tenant.getStatus() != (byte) TenantStatusEnum.FROZEN.getCode()) {
            throw new BusinessException(ResultCode.CONFLICT, "租户非冻结状态，无法解冻, tenantId=" + tenantId);
        }
        tenant.setStatus((byte) TenantStatusEnum.ACTIVE.getCode());
        tenant.setFrozenTime(null);
        tenant.setFrozenReason(null);
        this.updateById(tenant);

        // 删除 Redis 中的冻结标记
        String redisKey = REDIS_FROZEN_KEY_PREFIX + tenantId;
        redisTemplate.delete(redisKey);
        log.info("解冻租户成功, tenantId={}", tenantId);
    }

    @Override
    public Tenant getByTenantCode(String tenantCode) {
        return this.lambdaQuery()
                .eq(Tenant::getTenantCode, tenantCode)
                .one();
    }

    @Override
    public PageResult<TenantVO> pageTenants(TenantQueryDTO query) {
        LambdaQueryWrapper<Tenant> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getTenantName()), Tenant::getTenantName, query.getTenantName());
        wrapper.eq(query.getStatus() != null, Tenant::getStatus, query.getStatus());
        wrapper.orderByDesc(Tenant::getCreateTime);

        Page<Tenant> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<Tenant> result = this.page(page, wrapper);

        // 批量查询套餐信息
        List<Long> packageIds = result.getRecords().stream()
                .map(Tenant::getPackageId)
                .filter(pid -> pid != null)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Package> packageMap = Collections.emptyMap();
        if (!packageIds.isEmpty()) {
            packageMap = packageService.listByIds(packageIds).stream()
                    .collect(Collectors.toMap(Package::getId, p -> p));
        }

        Map<Long, Package> finalPackageMap = packageMap;
        List<TenantVO> voList = result.getRecords().stream()
                .map(tenant -> convertToVO(tenant, finalPackageMap))
                .collect(Collectors.toList());

        return PageResult.of(voList, result.getTotal(), query.getPageNum(), query.getPageSize());
    }

    /**
     * 生成租户编码: T + yyyyMMdd + 3位序号
     * 例如: T20260518001
     *
     * @return 租户编码
     */
    private String generateTenantCode() {
        String dateStr = LocalDate.now().format(CODE_DATE_FORMATTER);
        String prefix = TENANT_CODE_PREFIX + dateStr;

        // 查询当天已有的最大编码
        long count = this.lambdaQuery()
                .likeRight(Tenant::getTenantCode, prefix)
                .count();

        int sequence = (int) (count + 1);
        return prefix + String.format("%03d", sequence);
    }

    /**
     * 将 Tenant 实体转换为 TenantVO
     *
     * @param tenant     租户实体
     * @param packageMap 套餐映射
     * @return 租户视图对象
     */
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

        // 根据状态设置过期时间
        if (tenant.getStatus() == (byte) TenantStatusEnum.TRIAL.getCode()) {
            vo.setExpireTime(tenant.getTrialExpireTime());
        } else {
            vo.setExpireTime(tenant.getPaidExpireTime());
        }

        // 套餐信息
        if (tenant.getPackageId() != null && packageMap.containsKey(tenant.getPackageId())) {
            Package pkg = packageMap.get(tenant.getPackageId());
            vo.setPackageName(pkg.getPackageName());
            vo.setMaxUsers(pkg.getMaxUsers());
        }

        // TODO 当前用户数需从 rbac-service 获取
        vo.setCurrentUsers(0);

        return vo;
    }

    /**
     * 从全局配置读取整数值
     *
     * @param configKey    配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    private int getConfigIntValue(String configKey, int defaultValue) {
        GlobalConfig config = globalConfigService.lambdaQuery()
                .eq(GlobalConfig::getConfigKey, configKey)
                .one();
        if (config != null && StringUtils.hasText(config.getConfigValue())) {
            try {
                return Integer.parseInt(config.getConfigValue());
            } catch (NumberFormatException e) {
                log.warn("全局配置值解析失败, key={}, value={}", configKey, config.getConfigValue());
            }
        }
        return defaultValue;
    }

    /**
     * 从全局配置读取长整数值
     *
     * @param configKey    配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    private Long getConfigLongValue(String configKey, Long defaultValue) {
        GlobalConfig config = globalConfigService.lambdaQuery()
                .eq(GlobalConfig::getConfigKey, configKey)
                .one();
        if (config != null && StringUtils.hasText(config.getConfigValue())) {
            try {
                return Long.parseLong(config.getConfigValue());
            } catch (NumberFormatException e) {
                log.warn("全局配置值解析失败, key={}, value={}", configKey, config.getConfigValue());
            }
        }
        return defaultValue;
    }
}
