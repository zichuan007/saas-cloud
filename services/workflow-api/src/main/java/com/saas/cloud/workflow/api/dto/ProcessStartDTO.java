package com.saas.cloud.workflow.api.dto;

import java.io.Serializable;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 流程发起请求DTO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
public class ProcessStartDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 流程定义扩展表ID */
    @NotNull(message = "流程定义ID不能为空")
    private Long processDefinitionExtId;

    /** 流程标题 */
    @NotBlank(message = "流程标题不能为空")
    private String title;

    /** 表单数据(JSON) */
    private String formData;

    /** 抄送人ID列表 */
    private List<Long> copyUserIds;
}
