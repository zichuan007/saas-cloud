package com.saas.cloud.notify.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.saas.cloud.common.data.base.TenantBaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 站内消息表
 * </p>
 *
 * @author saas-cloud
 * @since 2026-05-18
 */
@Getter
@Setter
@TableName("notify_message")
public class NotifyMessage extends TenantBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 接收人ID
     */
    @TableField("receiver_id")
    private Long receiverId;

    /**
     * 发送人ID
     */
    @TableField("sender_id")
    private Long senderId;

    /**
     * 发送人姓名
     */
    @TableField("sender_name")
    private String senderName;

    /**
     * 消息标题
     */
    @TableField("title")
    private String title;

    /**
     * 消息内容
     */
    @TableField("content")
    private String content;

    /**
     * 类型 0-系统通知 1-审批通知 2-催办 3-公告
     */
    @TableField("type")
    private Byte type;

    /**
     * 业务类型
     */
    @TableField("biz_type")
    private String bizType;

    /**
     * 业务ID
     */
    @TableField("biz_id")
    private String bizId;

    /**
     * 跳转链接
     */
    @TableField("jump_url")
    private String jumpUrl;

    /**
     * 是否已读 0-未读 1-已读
     */
    @TableField("is_read")
    private Byte isRead;

    /**
     * 阅读时间
     */
    @TableField("read_time")
    private LocalDateTime readTime;
}
