package com.saas.cloud.workflow.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 流程定义详情视图对象
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
public class ProcessDefinitionDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 流程标识 */
    private String processKey;

    /** 流程名称 */
    private String processName;

    /** 分类 */
    private String category;

    /** 流程图标URL */
    private String icon;

    /** 流程说明 */
    private String description;

    /** 表单类型 0-外链 1-内嵌JSON */
    private Byte formType;

    /** 表单URL */
    private String formUrl;

    /** 表单配置JSON */
    private String formConfig;

    /** Flowable流程定义ID */
    private String processDefinitionId;

    /** 版本号 */
    private Integer version;

    /** 状态 0-挂起 1-激活 */
    private Byte status;

    /** 状态描述 */
    private String statusDesc;

    /** 排序 */
    private Integer sortOrder;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 节点配置列表 */
    private List<NodeConfigVO> nodeConfigs;
}
