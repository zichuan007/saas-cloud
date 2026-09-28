package com.saas.cloud.workflow.api.enums;

/**
 * 审批动作枚举
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-14
 */
public enum ApprovalActionEnum {

    /** 审批通过 */
    APPROVED("APPROVED", "审批通过"),

    /** 驳回（走BPMN默认流） */
    REJECTED("REJECTED", "驳回"),

    /** 退回修改（跳回指定节点） */
    RETURNED("RETURNED", "退回修改"),

    /** 转办 */
    TRANSFER("TRANSFER", "转办"),

    /** 委派 */
    DELEGATE("DELEGATE", "委派"),

    /** 加签 */
    ADD_SIGN("ADD_SIGN", "加签"),

    /** 拿回 */
    RECALL("RECALL", "拿回"),

    /** 撤销 */
    CANCEL("CANCEL", "撤销"),

    /** 重新提交 */
    RESUBMIT("RESUBMIT", "重新提交"),

    /** 催办 */
    URGE("URGE", "催办"),

    /** 抄送 */
    CC("CC", "抄送"),

    /** 系统自动提交(发起人节点) */
    AUTO_SUBMIT("AUTO_SUBMIT", "系统自动提交"),

    /** 系统自动跳过(同审批人) */
    AUTO_SKIP("AUTO_SKIP", "系统自动跳过");

    private final String code;
    private final String desc;

    ApprovalActionEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}