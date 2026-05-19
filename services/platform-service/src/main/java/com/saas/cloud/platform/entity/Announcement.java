package com.saas.cloud.platform.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.saas.cloud.common.data.base.BaseEntity;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 系统公告表
 * </p>
 *
 * @author saas-cloud
 * @since 2026-05-18
 */
@Getter
@Setter
@TableName("sys_announcement")
public class Announcement extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 公告标题
     */
    @TableField("title")
    private String title;

    /**
     * 公告内容
     */
    @TableField("content")
    private String content;

    /**
     * 类型 0-通知 1-维护 2-功能更新
     */
    @TableField("type")
    private Byte type;

    /**
     * 目标 0-全部租户 1-指定租户
     */
    @TableField("target_type")
    private Byte targetType;

    /**
     * 指定租户ID列表(JSON)
     */
    @TableField("target_tenant_ids")
    private String targetTenantIds;

    /**
     * 发布时间
     */
    @TableField("publish_time")
    private LocalDateTime publishTime;

    /**
     * 过期时间
     */
    @TableField("expire_time")
    private LocalDateTime expireTime;

    /**
     * 状态 0-草稿 1-已发布 2-已下线
     */
    @TableField("status")
    private Byte status;
}
