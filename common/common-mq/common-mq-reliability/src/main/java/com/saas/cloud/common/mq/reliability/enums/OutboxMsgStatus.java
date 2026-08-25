package com.saas.cloud.common.mq.reliability.enums;

/**
 * Outbox 消息状态
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-24
 */
public enum OutboxMsgStatus {

    /** 初始化待投递 */
    INIT(0, "初始化"),
    /** 投递成功 */
    SEND_SUCCESS(1, "投递成功"),
    /** 投递失败待重试 */
    SEND_FAIL(2, "投递失败"),
    /** 重试超限放弃 */
    SEND_GIVE_UP(3, "放弃投递");

    private final int code;

    private final String desc;

    OutboxMsgStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
