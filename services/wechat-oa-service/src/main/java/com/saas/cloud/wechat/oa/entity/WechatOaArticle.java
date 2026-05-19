package com.saas.cloud.wechat.oa.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.saas.cloud.common.data.base.TenantBaseEntity;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 图文表
 * </p>
 *
 * @author saas-cloud
 * @since 2026-05-18
 */
@Getter
@Setter
@TableName("wechat_oa_article")
public class WechatOaArticle extends TenantBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 公众号ID
     */
    @TableField("account_id")
    private Long accountId;

    /**
     * 标题
     */
    @TableField("title")
    private String title;

    /**
     * 作者
     */
    @TableField("author")
    private String author;

    /**
     * 摘要
     */
    @TableField("digest")
    private String digest;

    /**
     * 正文(HTML)
     */
    @TableField("content")
    private String content;

    /**
     * 封面素材ID
     */
    @TableField("thumb_media_id")
    private String thumbMediaId;

    /**
     * 封面URL
     */
    @TableField("thumb_url")
    private String thumbUrl;

    /**
     * 原文链接
     */
    @TableField("content_source_url")
    private String contentSourceUrl;

    /**
     * 微信端图文素材ID
     */
    @TableField("wx_media_id")
    private String wxMediaId;

    /**
     * 状态 0-草稿 1-已发布 2-已下线
     */
    @TableField("status")
    private Byte status;

    /**
     * 发布时间
     */
    @TableField("publish_time")
    private LocalDateTime publishTime;

    /**
     * 阅读数
     */
    @TableField("read_count")
    private Integer readCount;

    /**
     * 分享数
     */
    @TableField("share_count")
    private Integer shareCount;

    /**
     * 点赞数
     */
    @TableField("like_count")
    private Integer likeCount;

    /**
     * 多图文排序
     */
    @TableField("sort_order")
    private Integer sortOrder;
}
