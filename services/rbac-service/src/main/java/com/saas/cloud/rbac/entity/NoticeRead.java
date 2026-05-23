package com.saas.cloud.rbac.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 公告已读记录
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Data
@TableName("sys_notice_read")
public class NoticeRead implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 公告ID */
    @TableField("notice_id")
    private Long noticeId;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 阅读时间 */
    @TableField("read_time")
    private LocalDateTime readTime;
}
