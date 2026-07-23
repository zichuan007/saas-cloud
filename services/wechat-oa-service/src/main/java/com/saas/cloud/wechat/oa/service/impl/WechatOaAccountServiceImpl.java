package com.saas.cloud.wechat.oa.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.saas.cloud.common.core.exception.BusinessException;
import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.data.tenant.annotation.TenantIgnore;
import com.saas.cloud.common.security.context.TenantContext;
import com.saas.cloud.platform.api.feign.PlatformFeignClient;
import com.saas.cloud.wechat.oa.client.WechatApiClient;
import com.saas.cloud.wechat.oa.entity.WechatOaAccount;
import com.saas.cloud.wechat.oa.mapper.WechatOaAccountMapper;
import com.saas.cloud.wechat.oa.service.IWechatOaAccountService;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 公众号账号表 服务实现类
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class WechatOaAccountServiceImpl
        extends ServiceImpl<WechatOaAccountMapper, WechatOaAccount>
        implements IWechatOaAccountService {

    private final WechatApiClient wechatApiClient;
    private final PlatformFeignClient platformFeignClient;

    @Override
    public List<WechatOaAccount> listAccounts() {
        LambdaQueryWrapper<WechatOaAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(WechatOaAccount::getCreateTime);
        return list(wrapper);
    }

    @Override
    public WechatOaAccount getAccountDetail(Long id) {
        WechatOaAccount account = getById(id);
        if (account == null) {
            throw new BusinessException("公众号不存在");
        }
        return account;
    }

    @Override
    public void bindAccount(WechatOaAccount account) {
        long count = lambdaQuery()
                .eq(WechatOaAccount::getAppId, account.getAppId())
                .count();
        if (count > 0) {
            throw new BusinessException("该AppID已绑定");
        }

        Long tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            try {
                long currentCount = lambdaQuery().count();
                ApiResult<Boolean> quotaResult = platformFeignClient.checkQuota(
                        tenantId, "WECHAT_ACCOUNT", (int) currentCount);
                if (quotaResult.isSuccess() && Boolean.FALSE.equals(quotaResult.getData())) {
                    throw new BusinessException("公众号绑定数已达套餐上限，请升级套餐");
                }
            } catch (BusinessException e) {
                throw e;
            } catch (Exception e) {
                log.warn("公众号配额校验异常, 降级放行, tenantId={}, error={}", tenantId, e.getMessage());
            }
        }

        account.setStatus((byte) 1);
        save(account);
        log.info("绑定公众号成功, appId={}, accountName={}", account.getAppId(), account.getAccountName());
    }

    @Override
    public void updateAccount(Long id, WechatOaAccount account) {
        WechatOaAccount existing = getById(id);
        if (existing == null) {
            throw new BusinessException("公众号不存在");
        }

        existing.setAccountName(account.getAccountName());
        existing.setToken(account.getToken());
        existing.setAesKey(account.getAesKey());
        existing.setAccountType(account.getAccountType());
        existing.setIsVerified(account.getIsVerified());
        existing.setQrCodeUrl(account.getQrCodeUrl());
        updateById(existing);
        log.info("更新公众号信息, id={}", id);
    }

    @Override
    public void unbindAccount(Long id) {
        WechatOaAccount existing = getById(id);
        if (existing == null) {
            throw new BusinessException("公众号不存在");
        }
        removeById(id);
        log.info("解绑公众号成功, id={}, appId={}", id, existing.getAppId());
    }

    @TenantIgnore
    @Override
    public WechatOaAccount getByAppId(String appId) {
        return lambdaQuery()
                .eq(WechatOaAccount::getAppId, appId)
                .one();
    }

    @Override
    public String getValidAccessToken(Long accountId) {
        WechatOaAccount account = getById(accountId);
        if (account == null) {
            throw new BusinessException("公众号不存在");
        }
        if (StrUtil.isNotBlank(account.getAccessToken())
                && account.getAccessTokenExpireTime() != null
                && account.getAccessTokenExpireTime().isAfter(LocalDateTime.now().plusMinutes(5))) {
            return account.getAccessToken();
        }
        Map<String, Object> result = wechatApiClient.getAccessToken(
                account.getAppId(), account.getAppSecret());
        String accessToken = (String) result.get("access_token");
        Integer expiresIn = (Integer) result.get("expires_in");
        account.setAccessToken(accessToken);
        account.setAccessTokenExpireTime(LocalDateTime.now().plusSeconds(expiresIn));
        updateById(account);
        return accessToken;
    }
}
