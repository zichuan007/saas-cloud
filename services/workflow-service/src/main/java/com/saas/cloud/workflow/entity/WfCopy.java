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
 * æµç¨‹æŠ„é€è¡¨
 * </p>
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
     * æµç¨‹å®žä¾‹ID
     */
    @TableField("process_instance_id")
    private String processInstanceId;

    /**
     * æµç¨‹åç§°
     */
    @TableField("process_name")
    private String processName;

    /**
     * æµç¨‹æ ‡é¢˜
     */
    @TableField("title")
    private String title;

    /**
     * å‘èµ·äººID
     */
    @TableField("initiator_id")
    private Long initiatorId;

    /**
     * å‘èµ·äººå§“å
     */
    @TableField("initiator_name")
    private String initiatorName;

    /**
     * æŽ¥æ”¶äººID
     */
    @TableField("receiver_id")
    private Long receiverId;

    /**
     * æŽ¥æ”¶äººå§“å
     */
    @TableField("receiver_name")
    private String receiverName;

    /**
     * å‘ç”Ÿåœ¨å“ªä¸ªèŠ‚ç‚¹
     */
    @TableField("task_name")
    private String taskName;

    /**
     * æ˜¯å¦å·²è¯» 0-æœªè¯» 1-å·²è¯»
     */
    @TableField("is_read")
    private Byte isRead;

    /**
     * é˜…è¯»æ—¶é—´
     */
    @TableField("read_time")
    private LocalDateTime readTime;
}
