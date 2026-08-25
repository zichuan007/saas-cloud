package com.saas.cloud.common.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消息投递结果
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendResult {

    /** 投递状态 */
    private SendStatus status;

    /** 消息唯一 ID */
    private String msgId;

    /** 失败原因，成功时为 null */
    private String error;

    /**
     * 构造成功结果
     *
     * @param msgId 消息 ID
     * @return 成功结果
     */
    public static SendResult success(String msgId) {
        return new SendResult(SendStatus.SUCCESS, msgId, null);
    }

    /**
     * 构造失败结果
     *
     * @param msgId 消息 ID
     * @param error 失败原因
     * @return 失败结果
     */
    public static SendResult fail(String msgId, String error) {
        return new SendResult(SendStatus.FAIL, msgId, error);
    }

    /**
     * 构造待投递结果（已入 outbox）
     *
     * @param msgId 消息 ID
     * @return 待投递结果
     */
    public static SendResult pending(String msgId) {
        return new SendResult(SendStatus.PENDING, msgId, null);
    }
}
