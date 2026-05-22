package com.saas.cloud.notify.api.vo;

import java.io.Serializable;

import lombok.Data;

/**
 * 通知渠道配置视图VO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
public class ChannelConfigVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 渠道类型 0-站内信 1-邮件 2-飞书 3-钉钉 4-企业微信 */
    private Byte channelType;

    /** 渠道类型描述 */
    private String channelTypeDesc;

    /** 是否启用 0-否 1-是 */
    private Byte enabled;

    /** 渠道配置(JSON) */
    private String configJson;
}
