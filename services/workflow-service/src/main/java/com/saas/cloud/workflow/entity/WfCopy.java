package com.saas.cloud.workflow.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.saas.cloud.common.data.base.TenantBaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 流程抄送表
 *
 * @author saas-cloud
 * @since 2026-05-18
 */
@Getter
@Setter
@TableName("wf_copy")
public class WfCopy extends TenantBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 流程实例ID
     */
    @TableField("process_instance_id")
    private String processInstanceId;

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
     * 接收人ID
     */
    @TableField("receiver_id")
    private Long receiverId;

    /**
     * 接收人姓名
     */
    @TableField("receiver_name")
    private String receiverName;

    /**
     * 发生在哪个节点
     */
    @TableField("task_name")
    private String taskName;

    /**
     * 是否已读 0-未读 1-已读
     */
    @TableField("is_read")
    private Byte isRead;

    /**
     * 阅读时间
     */
    @TableField("read_time")
    private LocalDateTime readTime;
}
