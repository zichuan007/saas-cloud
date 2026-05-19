package com.saas.cloud.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.saas.cloud.common.data.base.TenantBaseEntity;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 流程定义扩展表
 * </p>
 *
 * @author saas-cloud
 * @since 2026-05-18
 */
@Getter
@Setter
@TableName("wf_process_definition_ext")
public class WfProcessDefinitionExt extends TenantBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * Flowable流程定义ID
     */
    @TableField("process_definition_id")
    private String processDefinitionId;

    /**
     * 流程标识
     */
    @TableField("process_key")
    private String processKey;

    /**
     * 流程名称
     */
    @TableField("process_name")
    private String processName;

    /**
     * 分类
     */
    @TableField("category")
    private String category;

    /**
     * 流程图标URL
     */
    @TableField("icon")
    private String icon;

    /**
     * 流程说明
     */
    @TableField("description")
    private String description;

    /**
     * 表单类型 0-外链 1-内嵌JSON
     */
    @TableField("form_type")
    private Byte formType;

    /**
     * 表单URL
     */
    @TableField("form_url")
    private String formUrl;

    /**
     * 表单配置(JSON)
     */
    @TableField("form_config")
    private String formConfig;

    /**
     * 是否平台模板 0-自定义 1-模板
     */
    @TableField("is_template")
    private Byte isTemplate;

    /**
     * 版本号
     */
    @TableField("version")
    private Integer version;

    /**
     * 状态 0-挂起 1-激活
     */
    @TableField("status")
    private Byte status;

    /**
     * 排序
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * Flowable Model ID（ACT_RE_MODEL 主键）
     */
    @TableField("model_id")
    private String modelId;
}
