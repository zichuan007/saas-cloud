package com.saas.cloud.workflow.api.enums;

/**
 * ApproveMode 枚举
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-14
 */
public enum ApproveModeEnum {

    ANY("ANY", "或签"),
    ALL("ALL", "会签"),
    SEQUENTIAL("SEQUENTIAL", "顺签");

    private final String code;
    private final String desc;

    ApproveModeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }
}