package com.saas.cloud.workflow.api.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 流程定义创建/更新请求DTO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
public class ProcessDefinitionCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 流程标识（英文标识，同租户下唯一） */
    @NotBlank(message = "流程标识不能为空")
    @Size(max = 128, message = "流程标识最长128个字符")
    private String processKey;

    /** 流程名称 */
    @NotBlank(message = "流程名称不能为空")
    @Size(max = 255, message = "流程名称最长255个字符")
    private String processName;

    /** 分类 */
    private String category;

    /** 流程图标URL */
    private String icon;

    /** 流程说明 */
    private String description;

    /** 表单类型 0-外链 1-内嵌JSON */
    private Byte formType;

    /** 表单URL（formType=0时使用） */
    private String formUrl;

    /** 表单配置JSON（formType=1时使用） */
    private String formConfig;

    /** 排序 */
    private Integer sortOrder;
}
