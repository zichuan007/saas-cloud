package com.saas.cloud.rbac.api.vo;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 通知公告 VO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Data
public class NoticeVO {

    /** 公告ID */
    private Long id;

    /** 公告标题 */
    private String title;

    /** 公告内容 */
    private String content;

    /** 类型 1-通知 2-公告 */
    private Byte noticeType;

    /** 状态 0-草稿 1-已发布 2-已撤回 */
    private Byte status;

    /** 发布时间 */
    private LocalDateTime publishTime;

    /** 创建人 */
    private String createUserName;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 当前用户是否已读 */
    private Boolean read;
}
