package com.saas.cloud.workflow.api.enums;

/**
 * TaskRelationType 枚举
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-14
 */
public enum TaskRelationTypeEnum {

    TRANSFER("TRANSFER", "转办"),
    DELEGATE("DELEGATE", "委派"),
    COUNTER_SIGN("COUNTER_SIGN", "加签");

    private final String code;
    private final String desc;

    TaskRelationTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }
}