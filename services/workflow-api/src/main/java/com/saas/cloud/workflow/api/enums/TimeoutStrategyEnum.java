package com.saas.cloud.workflow.api.enums;

/**
 * TimeoutStrategy 枚举
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-14
 */
public enum TimeoutStrategyEnum {

    APPROVE("APPROVE", "自动通过"),
    REJECT("REJECT", "自动驳回"),
    TRANSFER("TRANSFER", "自动转办"),
    REMIND("REMIND", "仅提醒");

    private final String code;
    private final String desc;

    TimeoutStrategyEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }
}