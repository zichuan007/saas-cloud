package com.saas.cloud.platform.job;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.saas.cloud.common.core.enums.TenantStatusEnum;
import com.saas.cloud.platform.entity.Tenant;
import com.saas.cloud.platform.service.ITenantService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 租户到期检查定时任务
 * <p>检查试用到期的租户，自动冻结并发送通知</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-22
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class TenantExpireCheckJobHandler {

    private final ITenantService tenantService;

    /**
     * 检查试用到期租户
     * <p>建议 Cron: 0 0 9 * * ? (每天上午 9:00)</p>
     */
    @XxlJob("tenantExpireCheckJob")
    public void execute() {

        log.info("[XXL-Job] 开始检查租户到期情况");

        LambdaQueryWrapper<Tenant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tenant::getStatus, (byte) TenantStatusEnum.TRIAL.getCode())
                .le(Tenant::getTrialExpireTime, LocalDateTime.now());

        List<Tenant> expiredTenants = tenantService.list(wrapper);
        int frozenCount = 0;

        for (Tenant tenant : expiredTenants) {
            try {
                tenantService.freezeTenant(tenant.getId());
                frozenCount++;
                log.info("[XXL-Job] 试用到期自动冻结租户, tenantId={}, tenantName={}",
                        tenant.getId(), tenant.getTenantName());
            } catch (Exception e) {
                log.error("[XXL-Job] 冻结租户失败, tenantId={}, error={}",
                        tenant.getId(), e.getMessage());
            }
        }

        String msg = "检查完成, 到期租户: " + expiredTenants.size() + " 个, 成功冻结: " + frozenCount + " 个";
        log.info("[XXL-Job] {}", msg);
        XxlJobHelper.handleSuccess(msg);
    }

}
