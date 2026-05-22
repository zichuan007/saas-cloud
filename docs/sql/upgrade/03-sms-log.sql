-- 短信发送日志表（notify 库）
USE `saas_notify`;

CREATE TABLE IF NOT EXISTS `sys_sms_log` (
    `id`               bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `phone`            varchar(20)  NOT NULL COMMENT '手机号',
    `content`          varchar(500) NOT NULL COMMENT '短信内容',
    `channel`          varchar(32)  NOT NULL COMMENT '短信通道（aliyun/tencent/huawei）',
    `template_code`    varchar(64)  DEFAULT NULL COMMENT '短信模板编码',
    `status`           tinyint(4)   NOT NULL DEFAULT 0 COMMENT '发送状态 0-失败 1-成功',
    `biz_id`           varchar(128) DEFAULT NULL COMMENT '第三方消息ID',
    `error_msg`        varchar(500) DEFAULT NULL COMMENT '失败原因',
    `send_time`        datetime     DEFAULT NULL COMMENT '发送时间',
    `tenant_id`        bigint(20)   NOT NULL COMMENT '租户ID',
    `create_user_id`   varchar(64)  DEFAULT NULL COMMENT '创建人ID',
    `create_user_name` varchar(64)  DEFAULT NULL COMMENT '创建人姓名',
    `create_time`      timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_user_id`   varchar(64)  DEFAULT NULL COMMENT '更新人ID',
    `update_user_name` varchar(64)  DEFAULT NULL COMMENT '更新人姓名',
    `update_time`      timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag`      int(11)      NOT NULL DEFAULT 0 COMMENT '删除标记 0-正常 1-删除',
    `data_version`     int(11)      NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `remark`           varchar(255) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_phone` (`phone`),
    KEY `idx_channel` (`channel`),
    KEY `idx_send_time` (`send_time`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='短信发送日志表';
