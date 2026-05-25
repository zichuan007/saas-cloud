package com.saas.cloud.notify.service;

import java.util.Map;

/**
 * 短信发送服务
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-22
 */
public interface ISmsService {

    /**
     * 发送短信
     *
     * @param phone        手机号
     * @param templateCode 短信模板编码
     * @param params       模板参数
     */
    void sendSms(String phone, String templateCode, Map<String, String> params);

    /**
     * 发送短信（指定通道）
     *
     * @param phone        手机号
     * @param templateCode 短信模板编码
     * @param params       模板参数
     * @param channelId    短信通道标识（对应 sms4j 配置的 configId）
     */
    void sendSms(String phone, String templateCode, Map<String, String> params, String channelId);

    /**
     * 发送短信（直接发送内容）
     *
     * @param phone   手机号
     * @param content 短信内容
     */
    void sendSmsContent(String phone, String content);

}
