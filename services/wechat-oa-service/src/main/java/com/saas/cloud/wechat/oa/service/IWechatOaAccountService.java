package com.saas.cloud.wechat.oa.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.saas.cloud.wechat.oa.entity.WechatOaAccount;

/**
 * 公众号账号表 服务接口
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
public interface IWechatOaAccountService extends IService<WechatOaAccount> {

    List<WechatOaAccount> listAccounts();

    WechatOaAccount getAccountDetail(Long id);

    void bindAccount(WechatOaAccount account);

    void updateAccount(Long id, WechatOaAccount account);

    void unbindAccount(Long id);

    WechatOaAccount getByAppId(String appId);

    String getValidAccessToken(Long accountId);
}
