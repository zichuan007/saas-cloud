package com.saas.cloud.notify.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通知渠道类型枚举
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Getter
@AllArgsConstructor
public enum NotifyChannelType {

    /** 站内信 */
    STATION_MAIL((byte) 0, "站内信"),
    /** 邮件 */
    EMAIL((byte) 1, "邮件"),
    /** 飞书 */
    FEISHU((byte) 2, "飞书"),
    /** 钉钉 */
    DINGTALK((byte) 3, "钉钉"),
    /** 企业微信 */
    WECOM((byte) 4, "企业微信");

    /** 渠道编码 */
    private final Byte code;
    /** 渠道描述 */
    private final String desc;

    /**
     * 根据编码获取枚举
     *
     * @param code 渠道编码
     * @return 枚举值，不存在返回 null
     */
    public static NotifyChannelType getByCode(Byte code) {
        if (code == null) {
            return null;
        }
        for (NotifyChannelType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
