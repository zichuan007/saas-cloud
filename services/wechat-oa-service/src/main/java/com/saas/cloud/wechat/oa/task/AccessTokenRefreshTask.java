package com.saas.cloud.wechat.oa.task;

import com.saas.cloud.wechat.oa.client.WechatApiClient;
import com.saas.cloud.wechat.oa.entity.WechatOaAccount;
import com.saas.cloud.wechat.oa.service.IWechatOaAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AccessToken 定时刷新任务
 * <p>每 100 分钟执行一次，提前 10 分钟续期（AccessToken 有效期 7200 秒）</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AccessTokenRefreshTask {

    private final IWechatOaAccountService accountService;
    private final WechatApiClient wechatApiClient;

    @Scheduled(fixedRate = 100 * 60 * 1000, initialDelay = 10 * 1000)
    public void refreshAccessTokens() {
        List<WechatOaAccount> accounts = accountService.listAccounts();
        for (WechatOaAccount account : accounts) {
            if (account.getStatus() == null || account.getStatus() != 1) {
                continue;
            }
            try {
                refreshSingleAccount(account);
            } catch (Exception e) {
                log.error("刷新AccessToken失败, appId={}, error={}",
                        account.getAppId(), e.getMessage());
            }
        }
    }

    private void refreshSingleAccount(WechatOaAccount account) {
        if (account.getAccessTokenExpireTime() != null
                && account.getAccessTokenExpireTime().isAfter(LocalDateTime.now().plusMinutes(10))) {
            return;
        }

        Map<String, Object> result = wechatApiClient.getAccessToken(
                account.getAppId(), account.getAppSecret());
        String accessToken = (String) result.get("access_token");
        Integer expiresIn = (Integer) result.get("expires_in");

        account.setAccessToken(accessToken);
        account.setAccessTokenExpireTime(LocalDateTime.now().plusSeconds(expiresIn));
        accountService.updateById(account);
        log.info("刷新AccessToken成功, appId={}, expiresIn={}s", account.getAppId(), expiresIn);
    }
}
