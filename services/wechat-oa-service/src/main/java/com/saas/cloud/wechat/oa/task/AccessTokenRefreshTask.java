package com.saas.cloud.wechat.oa.task;

import com.saas.cloud.wechat.oa.client.WechatApiClient;
import com.saas.cloud.wechat.oa.entity.WechatOaAccount;
import com.saas.cloud.wechat.oa.service.IWechatOaAccountService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AccessToken 定时刷新任务
 * <p>每 90 分钟执行一次，提前 10 分钟续期（AccessToken 有效期 7200 秒）</p>
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

    /**
     * 刷新所有启用公众号的 AccessToken
     * <p>建议 Cron: 0 0/90 * * * ? (每 90 分钟)</p>
     */
    @XxlJob("refreshWechatAccessTokenJob")
    public void refreshAccessTokens() {
        List<WechatOaAccount> accounts = accountService.listAccounts();
        int successCount = 0;
        int skipCount = 0;

        for (WechatOaAccount account : accounts) {
            if (account.getStatus() == null || account.getStatus() != 1) {
                continue;
            }
            try {
                if (refreshSingleAccount(account)) {
                    successCount++;
                } else {
                    skipCount++;
                }
            } catch (Exception e) {
                log.error("刷新AccessToken失败, appId={}, error={}",
                        account.getAppId(), e.getMessage());
            }
        }

        String msg = "刷新完成, 成功: " + successCount + " 个, 跳过(未过期): " + skipCount + " 个";
        log.info("[XXL-Job] {}", msg);
        XxlJobHelper.handleSuccess(msg);
    }

    /**
     * @return true=已刷新, false=未过期跳过
     */
    private boolean refreshSingleAccount(WechatOaAccount account) {
        if (account.getAccessTokenExpireTime() != null
                && account.getAccessTokenExpireTime().isAfter(LocalDateTime.now().plusMinutes(10))) {
            return false;
        }

        Map<String, Object> result = wechatApiClient.getAccessToken(
                account.getAppId(), account.getAppSecret());
        String accessToken = (String) result.get("access_token");
        Integer expiresIn = (Integer) result.get("expires_in");

        account.setAccessToken(accessToken);
        account.setAccessTokenExpireTime(LocalDateTime.now().plusSeconds(expiresIn));
        accountService.updateById(account);
        log.info("刷新AccessToken成功, appId={}, expiresIn={}s", account.getAppId(), expiresIn);
        return true;
    }
}
