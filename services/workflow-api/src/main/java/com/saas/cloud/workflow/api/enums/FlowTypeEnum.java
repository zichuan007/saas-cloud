package com.saas.cloud.workflow.api.enums;

/**
 * FlowType 枚举
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-14
 */
public enum FlowTypeEnum {

    NORMAL("NORMAL", "正常流转"),
    REJECT("REJECT", "驳回流转"),
    AUTO_SUBMIT("AUTO_SUBMIT", "自动提交");

    private final String code;
    private final String desc;

    FlowTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }
}