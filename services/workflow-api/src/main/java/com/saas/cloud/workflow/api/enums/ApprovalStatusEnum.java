package com.saas.cloud.workflow.api.enums;

/**
 * ApprovalStatus 枚举
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-14
 */
public enum ApprovalStatusEnum {

    PENDING("PENDING", "待审批"),
    APPROVED("APPROVED", "已通过"),
    REJECTED("REJECTED", "已驳回"),
    CANCELLED("CANCELLED", "已撤销"),
    WITHDRAWN("WITHDRAWN", "已撤回"),
    TERMINATED("TERMINATED", "已终止");

    private final String code;
    private final String desc;

    ApprovalStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }
}