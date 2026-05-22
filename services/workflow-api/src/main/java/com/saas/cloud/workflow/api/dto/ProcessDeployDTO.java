package com.saas.cloud.workflow.api.dto;

import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 流程部署请求DTO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
public class ProcessDeployDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** BPMN XML 内容 */
    @NotBlank(message = "BPMN XML 不能为空")
    private String bpmnXml;
}
