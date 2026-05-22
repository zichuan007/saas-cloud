USE platform;

CREATE TABLE `sys_tenant` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '租户ID',
  `tenant_name` varchar(128) NOT NULL COMMENT '租户名称',
  `tenant_code` varchar(64) NOT NULL COMMENT '租户编码',
  `contact_person` varchar(64) DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `contact_email` varchar(128) DEFAULT NULL COMMENT '联系邮箱',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态 0-试用 1-正常 2-冻结 3-注销',
  `package_id` bigint(20) NOT NULL COMMENT '套餐ID',
  `trial_expire_time` datetime DEFAULT NULL COMMENT '试用到期时间',
  `paid_expire_time` datetime DEFAULT NULL COMMENT '付费到期时间',
  `frozen_time` datetime DEFAULT NULL COMMENT '冻结时间',
  `frozen_reason` varchar(512) DEFAULT NULL COMMENT '冻结原因',
  `admin_user_id` bigint(20) DEFAULT NULL COMMENT '租户管理员用户ID',
  `logo_url` varchar(512) DEFAULT NULL COMMENT '企业Logo',
  `address` varchar(512) DEFAULT NULL COMMENT '企业地址',
  `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(64) DEFAULT NULL COMMENT '更新人姓名',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` int(11) NOT NULL DEFAULT 0 COMMENT '删除标记',
  `data_version` int(11) NOT NULL DEFAULT 0 COMMENT '数据版本号',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_code` (`tenant_code`),
  KEY `idx_status` (`status`),
  KEY `idx_package_id` (`package_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户表';

CREATE TABLE `sys_package` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '套餐ID',
  `package_name` varchar(64) NOT NULL COMMENT '套餐名称',
  `package_code` varchar(32) NOT NULL COMMENT '套餐编码 FREE/BASIC/PRO/ENTERPRISE',
  `price_monthly` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '月价格',
  `price_yearly` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '年价格',
  `max_users` int(11) NOT NULL DEFAULT 0 COMMENT '最大用户数 0-不限',
  `max_roles` int(11) NOT NULL DEFAULT 0 COMMENT '最大角色数 0-不限',
  `max_depts` int(11) NOT NULL DEFAULT 0 COMMENT '最大部门数 0-不限',
  `max_process_definitions` int(11) NOT NULL DEFAULT 0 COMMENT '最大流程定义数 0-不限',
  `max_wechat_accounts` int(11) NOT NULL DEFAULT 0 COMMENT '最大公众号绑定数 0-不限',
  `max_storage_mb` bigint(20) NOT NULL DEFAULT 0 COMMENT '最大存储空间(MB) 0-不限',
  `menu_ids` text COMMENT '该套餐可见的菜单ID列表(JSON数组)',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
  `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(64) DEFAULT NULL COMMENT '更新人姓名',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` int(11) NOT NULL DEFAULT 0 COMMENT '删除标记',
  `data_version` int(11) NOT NULL DEFAULT 0 COMMENT '数据版本号',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_package_code` (`package_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐表';

CREATE TABLE `sys_platform_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(64) NOT NULL COMMENT '用户名',
  `password` varchar(128) NOT NULL COMMENT '密码(BCrypt)',
  `real_name` varchar(64) DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(128) DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(512) DEFAULT NULL COMMENT '头像URL',
  `role_type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '角色类型 0-超级管理员 1-运营人员',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(64) DEFAULT NULL COMMENT '最后登录IP',
  `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(64) DEFAULT NULL COMMENT '更新人姓名',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` int(11) NOT NULL DEFAULT 0 COMMENT '删除标记',
  `data_version` int(11) NOT NULL DEFAULT 0 COMMENT '数据版本号',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台用户表';

CREATE TABLE `sys_announcement` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title` varchar(256) NOT NULL COMMENT '公告标题',
  `content` text NOT NULL COMMENT '公告内容',
  `type` tinyint(4) NOT NULL DEFAULT 0 COMMENT '类型 0-通知 1-维护公告 2-功能更新',
  `target_type` tinyint(4) NOT NULL DEFAULT 0 COMMENT '目标 0-全部租户 1-指定租户',
  `target_tenant_ids` text COMMENT '指定租户ID列表(JSON数组)',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态 0-草稿 1-已发布 2-已下线',
  `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(64) DEFAULT NULL COMMENT '更新人姓名',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` int(11) NOT NULL DEFAULT 0 COMMENT '删除标记',
  `data_version` int(11) NOT NULL DEFAULT 0 COMMENT '数据版本号',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_status_publish` (`status`, `publish_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统公告表';

CREATE TABLE `sys_global_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` varchar(128) NOT NULL COMMENT '配置键',
  `config_value` varchar(2048) NOT NULL COMMENT '配置值',
  `config_type` varchar(32) NOT NULL DEFAULT 'STRING' COMMENT '值类型 STRING/NUMBER/BOOLEAN/JSON',
  `description` varchar(256) DEFAULT NULL COMMENT '配置说明',
  `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(64) DEFAULT NULL COMMENT '更新人姓名',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` int(11) NOT NULL DEFAULT 0 COMMENT '删除标记',
  `data_version` int(11) NOT NULL DEFAULT 0 COMMENT '数据版本号',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全局配置表';

-- ========== 初始数据 ==========

-- 默认套餐
INSERT INTO `sys_package` (`id`, `package_name`, `package_code`, `price_monthly`, `price_yearly`, `max_users`, `max_roles`, `max_depts`, `max_process_definitions`, `max_wechat_accounts`, `max_storage_mb`, `menu_ids`, `sort_order`, `status`, `remark`) VALUES
(1, '免费版', 'FREE', 0.00, 0.00, 10, 5, 10, 5, 1, 1024,
 '[1,101,102,2,201,202,203,204,205,2011,2012,2013,2014,2015,2016,2021,2022,2023,2024,2025,2031,2032,2033,2034,2051,2052,2053]',
 1, 1, '免费体验套餐：仪表盘+系统管理'),
(2, '基础版', 'BASIC', 299.00, 2990.00, 50, 20, 50, 20, 3, 10240,
 '[1,101,102,2,201,202,203,204,205,2011,2012,2013,2014,2015,2016,2021,2022,2023,2024,2025,2031,2032,2033,2034,2051,2052,2053,5,501,502,503,5011,5012,5021,5022,5023,5024,5031,5032]',
 2, 1, '适合小型团队：含通知管理'),
(3, '专业版', 'PRO', 999.00, 9990.00, 200, 50, 200, 0, 10, 102400,
 '[1,101,102,2,201,202,203,204,205,2011,2012,2013,2014,2015,2016,2021,2022,2023,2024,2025,2031,2032,2033,2034,2051,2052,2053,3,301,302,303,304,305,306,3011,3012,3013,3014,3015,3061,3062,4,401,402,403,404,405,406,407,408,4011,4012,4013,4014,4021,4022,4023,4031,4032,4033,4034,4035,4041,4042,4043,4051,4052,4053,4061,4062,4063,4064,4071,4072,4073,5,501,502,503,5011,5012,5021,5022,5023,5024,5031,5032]',
 3, 1, '适合中型企业：含流程+公众号+通知'),
(4, '旗舰版', 'ENTERPRISE', 0.00, 0.00, 0, 0, 0, 0, 0, 0, NULL, 4, 1, '按需定制，所有配额不限');

-- 平台超级管理员 (密码: Admin@2026, BCrypt加密)
INSERT INTO `sys_platform_user` (`id`, `username`, `password`, `real_name`, `role_type`, `status`) VALUES
(1, 'admin', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36PQm1z30YTsVJCj8AuHqFi', '平台管理员', 0, 1);

-- 全局配置
INSERT INTO `sys_global_config` (`config_key`, `config_value`, `config_type`, `description`) VALUES
('default_package_id', '1', 'NUMBER', '默认套餐ID'),
('trial_days', '15', 'NUMBER', '试用天数'),
('max_login_attempts', '5', 'NUMBER', '最大登录尝试次数'),
('password_min_length', '8', 'NUMBER', '密码最小长度'),
('login_lock_minutes', '30', 'NUMBER', '登录锁定时长(分钟)');
