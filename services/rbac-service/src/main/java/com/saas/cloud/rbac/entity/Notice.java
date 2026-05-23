package com.saas.cloud.rbac.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.saas.cloud.common.data.base.TenantBaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知公告
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_notice")
public class Notice extends TenantBaseEntity {

    /** 公告标题 */
    @TableField("title")
    private String title;

    /** 公告内容 */
    @TableField("content")
    private String content;

    /** 类型 1-通知 2-公告 */
    @TableField("notice_type")
    private Byte noticeType;

    /** 状态 0-草稿 1-已发布 2-已撤回 */
    @TableField("status")
    private Byte status;

    /** 发布时间 */
    @TableField("publish_time")
    private LocalDateTime publishTime;
}
