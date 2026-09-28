package com.saas.cloud.workflow.api.enums;

/**
 * RejectMode 枚举
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-14
 */
public enum RejectModeEnum {

    BPMN("BPMN", "BPMN默认流"),
    INITIATOR("INITIATOR", "驳回到发起人"),
    PREVIOUS("PREVIOUS", "驳回到上一节点"),
    CUSTOM("CUSTOM", "驳回到自定义节点");

    private final String code;
    private final String desc;

    RejectModeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }
}