package com.saas.cloud.notify.api.event;

import java.io.Serializable;
import java.util.Map;

import lombok.Data;

/**
 * 通知事件（跨服务共享的事件协议）
 * <p>
 * 发送方序列化为 JSON 后通过 Kafka 发送，notify-service 消费并生成站内消息。
 * </p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
public class NotifyEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 租户ID */
    private Long tenantId;

    /** 接收人ID */
    private Long receiverId;

    /** 发送人ID */
    private Long senderId;

    /** 发送人姓名 */
    private String senderName;

    /** 模板编码（用于查模板生成内容，为空时使用 title/content） */
    private String templateCode;

    /** 模板变量，替换 ${key} 占位符 */
    private Map<String, String> params;

    /** 消息标题（无模板时使用） */
    private String title;

    /** 消息内容（无模板时使用） */
    private String content;

    /** 类型 0-系统通知 1-审批通知 2-催办 3-公告 */
    private Byte type;

    /** 业务类型 */
    private String bizType;

    /** 业务ID */
    private String bizId;

    /** 跳转链接 */
    private String jumpUrl;

    /** 接收人手机号（短信通道使用） */
    private String phone;

    /** 是否同步发送短信 */
    private Boolean sendSms;
}
