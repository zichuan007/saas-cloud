package com.saas.cloud.notify.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.saas.cloud.common.data.base.BaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 通知模板表
 *
 * @author saas-cloud
 * @since 2026-05-18
 */
@Getter
@Setter
@TableName("notify_template")
public class NotifyTemplate extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 模板编码
     */
    @TableField("template_code")
    private String templateCode;

    /**
     * 模板名称
     */
    @TableField("template_name")
    private String templateName;

    /**
     * 渠道 0-站内信 1-邮件 2-IM Webhook
     */
    @TableField("type")
    private Byte type;

    /**
     * 标题模板
     */
    @TableField("title_template")
    private String titleTemplate;

    /**
     * 内容模板
     */
    @TableField("content_template")
    private String contentTemplate;

    /**
     * 状态 0-禁用 1-启用
     */
    @TableField("status")
    private Byte status;
}
