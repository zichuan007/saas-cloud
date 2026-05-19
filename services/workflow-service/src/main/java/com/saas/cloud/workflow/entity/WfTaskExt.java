package com.saas.cloud.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.saas.cloud.common.data.base.TenantBaseEntity;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 任务扩展表
 * </p>
 *
 * @author saas-cloud
 * @since 2026-05-18
 */
@Getter
@Setter
@TableName("wf_task_ext")
public class WfTaskExt extends TenantBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * Flowable任务ID
     */
    @TableField("task_id")
    private String taskId;

    /**
     * 流程实例ID
     */
    @TableField("process_instance_id")
    private String processInstanceId;

    /**
     * 任务名称
     */
    @TableField("task_name")
    private String taskName;

    /**
     * 当前处理人ID
     */
    @TableField("assignee_id")
    private Long assigneeId;

    /**
     * 当前处理人姓名
     */
    @TableField("assignee_name")
    private String assigneeName;

    /**
     * 任务所有人ID
     */
    @TableField("owner_id")
    private Long ownerId;

    /**
     * 操作 1-通过 2-驳回 3-转办 4-委派 5-加签
     */
    @TableField("action")
    private Byte action;

    /**
     * 审批意见
     */
    @TableField("comment")
    private String comment;

    /**
     * 处理耗时(ms)
     */
    @TableField("duration")
    private Long duration;

    /**
     * 完成时间
     */
    @TableField("complete_time")
    private LocalDateTime completeTime;
}
