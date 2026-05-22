package com.saas.cloud.wechat.oa.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.saas.cloud.common.data.base.TenantBaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 自动回复规则表
 *
 * @author saas-cloud
 * @since 2026-05-18
 */
@Getter
@Setter
@TableName("wechat_oa_auto_reply_rule")
public class WechatOaAutoReplyRule extends TenantBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 公众号ID
     */
    @TableField("account_id")
    private Long accountId;

    /**
     * 规则名称
     */
    @TableField("rule_name")
    private String ruleName;

    /**
     * 类型 0-关注回复 1-关键词回复 2-默认回复
     */
    @TableField("rule_type")
    private Byte ruleType;

    /**
     * 关键词
     */
    @TableField("keyword")
    private String keyword;

    /**
     * 匹配方式 0-全匹配 1-半匹配
     */
    @TableField("match_type")
    private Byte matchType;

    /**
     * 回复类型 0-文本 1-图片 2-图文
     */
    @TableField("reply_type")
    private Byte replyType;

    /**
     * 回复内容
     */
    @TableField("reply_content")
    private String replyContent;

    /**
     * 回复素材ID
     */
    @TableField("reply_media_id")
    private String replyMediaId;

    /**
     * 状态 0-禁用 1-启用
     */
    @TableField("status")
    private Byte status;

    /**
     * 排序
     */
    @TableField("sort_order")
    private Integer sortOrder;
}
