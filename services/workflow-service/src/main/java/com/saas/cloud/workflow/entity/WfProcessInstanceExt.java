package com.saas.cloud.workflow.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.saas.cloud.common.data.base.TenantBaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 流程实例扩展表
 *
 * @author saas-cloud
 * @since 2026-05-18
 */
@Getter
@Setter
@TableName("wf_process_instance_ext")
public class WfProcessInstanceExt extends TenantBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * Flowable流程实例ID
     */
    @TableField("process_instance_id")
    private String processInstanceId;

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
     * 流程标题
     */
    @TableField("title")
    private String title;

    /**
     * 发起人ID
     */
    @TableField("initiator_id")
    private Long initiatorId;

    /**
     * 发起人姓名
     */
    @TableField("initiator_name")
    private String initiatorName;

    /**
     * 发起人部门ID
     */
    @TableField("initiator_dept_id")
    private Long initiatorDeptId;

    /**
     * 业务关联键
     */
    @TableField("business_key")
    private String businessKey;

    /**
     * 表单数据(JSON)
     */
    @TableField("form_data")
    private String formData;

    /**
     * 状态 0-进行中 1-已完成 2-已撤回 3-已终止
     */
    @TableField("status")
    private Byte status;

    /**
     * 结果 1-通过 2-驳回
     */
    @TableField("result")
    private Byte result;

    /**
     * 结束时间
     */
    @TableField("end_time")
    private LocalDateTime endTime;

    /**
     * 耗时(ms)
     */
    @TableField("duration")
    private Long duration;
}
