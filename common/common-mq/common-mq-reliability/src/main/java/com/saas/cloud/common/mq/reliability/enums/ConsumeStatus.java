package com.saas.cloud.common.mq.reliability.enums;

/**
 * 消费幂等日志状态
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-24
 */
public enum ConsumeStatus {

    /** 初始化（处理中） */
    INIT(0, "处理中"),
    /** 消费成功 */
    CONSUME_SUCCESS(1, "消费成功"),
    /** 消费失败 */
    CONSUME_FAIL(2, "消费失败");

    private final int code;

    private final String desc;

    ConsumeStatus(int code, String desc) {
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
