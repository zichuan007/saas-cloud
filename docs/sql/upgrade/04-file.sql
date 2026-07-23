-- 文件管理表（platform 库）
USE `platform`;

CREATE TABLE IF NOT EXISTS `sys_file` (
    `id`               bigint(20)    NOT NULL AUTO_INCREMENT COMMENT '主键',
    `file_name`        varchar(255)  NOT NULL COMMENT '原始文件名',
    `file_path`        varchar(500)  NOT NULL COMMENT '存储路径（MinIO objectName）',
    `file_size`        bigint(20)    NOT NULL DEFAULT 0 COMMENT '文件大小（字节）',
    `file_type`        varchar(100)  DEFAULT NULL COMMENT '文件MIME类型',
    `file_suffix`      varchar(32)   DEFAULT NULL COMMENT '文件后缀',
    `bucket_name`      varchar(100)  NOT NULL COMMENT '存储桶名称',
    `biz_type`         varchar(64)   DEFAULT NULL COMMENT '业务类型（avatar/document/attachment等）',
    `biz_id`           varchar(64)   DEFAULT NULL COMMENT '关联业务ID',
    `tenant_id`        bigint(20)    NOT NULL COMMENT '租户ID',
    `create_user_id`   varchar(64)   DEFAULT NULL COMMENT '创建人ID',
    `create_user_name` varchar(64)   DEFAULT NULL COMMENT '创建人姓名',
    `create_time`      timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_user_id`   varchar(64)   DEFAULT NULL COMMENT '更新人ID',
    `update_user_name` varchar(64)   DEFAULT NULL COMMENT '更新人姓名',
    `update_time`      timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag`      int(11)       NOT NULL DEFAULT 0 COMMENT '删除标记 0-正常 1-删除',
    `data_version`     int(11)       NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `remark`           varchar(255)  DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_biz` (`biz_type`, `biz_id`),
    KEY `idx_file_path` (`file_path`(191))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件管理表';
