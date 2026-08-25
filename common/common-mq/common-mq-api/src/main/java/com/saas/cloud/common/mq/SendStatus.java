package com.saas.cloud.common.mq;

/**
 * 消息投递状态
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-24
 */
public enum SendStatus {

    /** 投递成功 */
    SUCCESS,

    /** 投递失败 */
    FAIL,

    /** 已入 outbox 待异步投递 */
    PENDING
}
