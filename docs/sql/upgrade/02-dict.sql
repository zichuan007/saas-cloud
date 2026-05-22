-- 数据字典类型表
CREATE TABLE IF NOT EXISTS `sys_dict_type` (
    `id`               bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `dict_name`        varchar(100) NOT NULL COMMENT '字典名称',
    `dict_type`        varchar(100) NOT NULL COMMENT '字典类型编码',
    `status`           tinyint(4)   NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
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
    UNIQUE KEY `uk_dict_type_tenant` (`dict_type`, `tenant_id`, `delete_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典类型表';

-- 数据字典数据表
CREATE TABLE IF NOT EXISTS `sys_dict_data` (
    `id`               bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `dict_type`        varchar(100) NOT NULL COMMENT '字典类型编码',
    `dict_label`       varchar(100) NOT NULL COMMENT '字典标签',
    `dict_value`       varchar(100) NOT NULL COMMENT '字典键值',
    `sort_order`       int(11)      NOT NULL DEFAULT 0 COMMENT '排序',
    `status`           tinyint(4)   NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    `css_class`        varchar(100) DEFAULT NULL COMMENT '样式属性',
    `list_class`       varchar(100) DEFAULT NULL COMMENT '表格回显样式',
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
    KEY `idx_dict_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典数据表';
