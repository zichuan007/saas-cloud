package com.saas.cloud.workflow.api.enums;

/**
 * AssignType 枚举
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-14
 */
public enum AssignTypeEnum {

    USER("USER", "指定用户"),
    ROLE("ROLE", "指定角色"),
    DEPT("DEPT", "部门负责人"),
    GROUP("GROUP", "审批组"),
    EXPRESSION("EXPRESSION", "表达式"),
    API("API", "API回调"),
    INITIATOR("INITIATOR", "发起人");

    private final String code;
    private final String desc;

    AssignTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }
}