package com.saas.cloud.common.mq.reliability.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * MQ 消费幂等日志实体（对标 tuk-mq MsgConsumer）
 * <p>按 (msg_id, group_id) 去重：成功后重复消息跳过；失败/初始化态允许重试。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-24
 */
@Data
@TableName("mq_consume_log")
public class MqConsumeLog {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 消息唯一 ID（幂等键） */
    private String msgId;

    /** 主题 */
    private String topic;

    /** 消费组 */
    private String groupId;

    /** 消费状态：0 INIT 1 CONSUME_SUCCESS 2 CONSUME_FAIL */
    private Integer consumeStatus;

    /** 失败原因 */
    private String errorMsg;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
