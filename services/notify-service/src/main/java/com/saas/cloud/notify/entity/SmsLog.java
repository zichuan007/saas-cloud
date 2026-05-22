package com.saas.cloud.notify.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.saas.cloud.common.data.base.TenantBaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 短信发送日志表
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-21
 */
@Getter
@Setter
@TableName("sys_sms_log")
public class SmsLog extends TenantBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 手机号 */
    @TableField("phone")
    private String phone;

    /** 短信内容 */
    @TableField("content")
    private String content;

    /** 短信通道（aliyun/tencent/huawei） */
    @TableField("channel")
    private String channel;

    /** 短信模板编码 */
    @TableField("template_code")
    private String templateCode;

    /** 发送状态 0-失败 1-成功 */
    @TableField("status")
    private Byte status;

    /** 第三方消息ID */
    @TableField("biz_id")
    private String bizId;

    /** 失败原因 */
    @TableField("error_msg")
    private String errorMsg;

    /** 发送时间 */
    @TableField("send_time")
    private LocalDateTime sendTime;
}
