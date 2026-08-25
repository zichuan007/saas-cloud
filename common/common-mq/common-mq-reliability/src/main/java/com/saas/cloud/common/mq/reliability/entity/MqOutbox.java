package com.saas.cloud.common.mq.reliability.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * MQ 生产 outbox 实体（对标 tuk-mq MsgProducer）
 * <p>本地消息表：先落库再实投，失败由补偿 Job 重试，保证至少一次送达。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-24
 */
@Data
@TableName("mq_outbox")
public class MqOutbox {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 消息唯一 ID（幂等键） */
    private String msgId;

    /** 业务标识 */
    private String bizId;

    /** 主题 */
    private String topic;

    /** 分区/路由键 */
    private String msgKey;

    /** 序列化报文 */
    private String payload;

    /** 消息状态：0 INIT 1 SEND_SUCCESS 2 SEND_FAIL 3 SEND_GIVE_UP */
    private Integer msgStatus;

    /** 已重试次数 */
    private Integer retryCount;

    /** 下次重试时间 */
    private LocalDateTime nextRetryTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
