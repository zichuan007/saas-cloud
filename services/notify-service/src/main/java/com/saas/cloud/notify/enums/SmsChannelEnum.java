package com.saas.cloud.notify.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 短信通道枚举
 * <p>定义常用短信通道标识，对应 sms4j 配置中的 configId。
 * 调用方使用枚举值作为 channelId 参数，避免硬编码字符串。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
@Getter
@AllArgsConstructor
public enum SmsChannelEnum {

    /** 阿里云短信 */
    ALIYUN("aliyun", "阿里云短信"),

    /** 腾讯云短信 */
    TENCENT("tencent", "腾讯云短信"),

    /** 华为云短信 */
    HUAWEI("huawei", "华为云短信"),

    /** 合一短信 */
    UNISMS("unisms", "合一短信"),

    ;

    /** 通道标识（对应 sms4j configId） */
    private final String channelId;

    /** 通道名称 */
    private final String channelName;
}
