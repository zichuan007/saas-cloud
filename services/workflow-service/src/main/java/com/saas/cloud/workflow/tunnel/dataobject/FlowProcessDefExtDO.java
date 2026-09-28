package com.saas.cloud.workflow.tunnel.dataobject;

import com.saas.cloud.common.data.base.TenantBaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 流程定义扩展 DO — 映射 wf_process_definition_ext 表
 */
@TableName("wf_process_definition_ext")
@Data
@EqualsAndHashCode(callSuper = true)
public class FlowProcessDefExtDO extends TenantBaseEntity {
    private String processDefinitionId;
    private String processKey;
    private String processName;
    private String category;
    private String icon;
    private String description;
    private Integer formType;
    private String formUrl;
    private String formConfig;
    private Integer isTemplate;
    private Integer version;
    private Integer status;
    private Integer sortOrder;
    private String modelId;
}