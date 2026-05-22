package com.saas.cloud.notify.api.vo;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 站内消息视图VO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
public class MessageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 消息ID */
    private Long id;

    /** 消息标题 */
    private String title;

    /** 消息内容 */
    private String content;

    /** 类型 0-系统通知 1-审批通知 2-催办 3-公告 */
    private Byte type;

    /** 类型描述 */
    private String typeDesc;

    /** 发送人姓名 */
    private String senderName;

    /** 业务类型 */
    private String bizType;

    /** 业务ID */
    private String bizId;

    /** 跳转链接 */
    private String jumpUrl;

    /** 是否已读 0-未读 1-已读 */
    private Byte isRead;

    /** 创建时间 */
    private LocalDateTime createTime;
}
