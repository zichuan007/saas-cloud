-- =====================================================================
-- SaaS Cloud 全量初始化脚本
-- 包含：5 个数据库、26 张表、RBAC 种子数据
-- 密码：Admin@2026（BCrypt 加密）
-- 执行方式：mysql -u root -p < init.sql
-- =====================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================================
-- 1. 创建数据库
-- =====================================================================
CREATE DATABASE IF NOT EXISTS `platform` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS `rbac` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS `wechat_oa` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS `notify` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS `workflow` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- =====================================================================
-- 2. platform 库 - 平台管理（5 张表）
-- =====================================================================
USE `platform`;

DROP TABLE IF EXISTS `sys_global_config`;
DROP TABLE IF EXISTS `sys_announcement`;
DROP TABLE IF EXISTS `sys_platform_user`;
DROP TABLE IF EXISTS `sys_tenant`;
DROP TABLE IF EXISTS `sys_package`;

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

-- =====================================================================
-- 3. rbac 库 - 权限管理（9 张表）
-- =====================================================================
USE `rbac`;

DROP TABLE IF EXISTS `sys_password_history`;
DROP TABLE IF EXISTS `sys_operation_log`;
DROP TABLE IF EXISTS `sys_role_dept`;
DROP TABLE IF EXISTS `sys_role_menu`;
DROP TABLE IF EXISTS `sys_user_role`;
DROP TABLE IF EXISTS `sys_menu`;
DROP TABLE IF EXISTS `sys_dept`;
DROP TABLE IF EXISTS `sys_role`;
DROP TABLE IF EXISTS `sys_user`;

CREATE TABLE `sys_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `username` varchar(64) NOT NULL COMMENT '用户名',
  `password` varchar(128) NOT NULL COMMENT '密码(BCrypt)',
  `real_name` varchar(64) DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(128) DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(512) DEFAULT NULL COMMENT '头像URL',
  `gender` tinyint(4) DEFAULT 0 COMMENT '性别 0-未知 1-男 2-女',
  `dept_id` bigint(20) DEFAULT NULL COMMENT '部门ID',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
  `role_level` tinyint(4) NOT NULL DEFAULT 2 COMMENT '角色等级 0-租户超管 1-部门主管 2-普通',
  `invite_code` varchar(64) DEFAULT NULL COMMENT '邀请码',
  `invite_status` tinyint(4) DEFAULT NULL COMMENT '邀请状态 0-待接受 1-已接受 2-已过期',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(64) DEFAULT NULL COMMENT '最后登录IP',
  `password_update_time` datetime DEFAULT NULL COMMENT '最后修改密码时间',
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
  UNIQUE KEY `uk_tenant_username` (`tenant_id`, `username`, `delete_flag`),
  UNIQUE KEY `uk_tenant_phone` (`tenant_id`, `phone`, `delete_flag`),
  KEY `idx_tenant_dept` (`tenant_id`, `dept_id`),
  KEY `idx_tenant_status` (`tenant_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE `sys_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `role_name` varchar(64) NOT NULL COMMENT '角色名称',
  `role_code` varchar(64) NOT NULL COMMENT '角色编码',
  `role_level` tinyint(4) NOT NULL DEFAULT 2 COMMENT '角色等级 0-超管 1-管理员 2-普通',
  `data_scope` tinyint(4) NOT NULL DEFAULT 4 COMMENT '数据范围 1-全部 2-本部门及下级 3-本部门 4-仅本人 5-自定义',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
  `is_system` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否系统内置 0-否 1-是',
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
  UNIQUE KEY `uk_tenant_role_code` (`tenant_id`, `role_code`, `delete_flag`),
  KEY `idx_tenant_status` (`tenant_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

CREATE TABLE `sys_dept` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '部门ID',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `dept_name` varchar(128) NOT NULL COMMENT '部门名称',
  `parent_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '父部门ID 0-顶级',
  `ancestors` varchar(1024) NOT NULL DEFAULT '0' COMMENT '祖先链',
  `leader_user_id` bigint(20) DEFAULT NULL COMMENT '部门负责人ID',
  `leader` varchar(64) DEFAULT NULL COMMENT '负责人姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
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
  KEY `idx_tenant_parent` (`tenant_id`, `parent_id`),
  KEY `idx_ancestors` (`ancestors`(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

CREATE TABLE `sys_menu` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `tenant_id` bigint(20) DEFAULT NULL COMMENT '租户ID（平台级菜单为空）',
  `menu_name` varchar(64) NOT NULL COMMENT '菜单名称',
  `parent_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '父菜单ID 0-顶级',
  `menu_type` tinyint(4) NOT NULL COMMENT '类型 0-目录 1-菜单 2-按钮',
  `path` varchar(256) DEFAULT NULL COMMENT '路由路径',
  `component` varchar(256) DEFAULT NULL COMMENT '组件路径',
  `permission` varchar(128) DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(128) DEFAULT NULL COMMENT '图标',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
  `visible` tinyint(4) NOT NULL DEFAULT 1 COMMENT '是否可见 0-隐藏 1-显示',
  `is_external` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否外链',
  `is_cached` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否缓存',
  `module` varchar(32) DEFAULT NULL COMMENT '所属模块 RBAC/WORKFLOW/WECHAT_OA',
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
  KEY `idx_parent` (`parent_id`),
  KEY `idx_module` (`module`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表（平台级）';

CREATE TABLE `sys_user_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
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
  UNIQUE KEY `uk_tenant_user_role` (`tenant_id`, `user_id`, `role_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

CREATE TABLE `sys_role_menu` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `menu_id` bigint(20) NOT NULL COMMENT '菜单ID',
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
  UNIQUE KEY `uk_tenant_role_menu` (`tenant_id`, `role_id`, `menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

CREATE TABLE `sys_role_dept` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `dept_id` bigint(20) NOT NULL COMMENT '部门ID',
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
  UNIQUE KEY `uk_tenant_role_dept` (`tenant_id`, `role_id`, `dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色部门关联表';

CREATE TABLE `sys_operation_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `user_id` bigint(20) DEFAULT NULL COMMENT '操作用户ID',
  `username` varchar(64) DEFAULT NULL COMMENT '操作用户名',
  `module` varchar(64) DEFAULT NULL COMMENT '操作模块',
  `operation` varchar(128) DEFAULT NULL COMMENT '操作描述',
  `method` varchar(256) DEFAULT NULL COMMENT '请求方法',
  `request_url` varchar(512) DEFAULT NULL COMMENT '请求URL',
  `request_method` varchar(16) DEFAULT NULL COMMENT 'HTTP方法',
  `request_params` text COMMENT '请求参数',
  `response_code` int(11) DEFAULT NULL COMMENT '响应状态码',
  `error_msg` text COMMENT '错误信息',
  `ip` varchar(64) DEFAULT NULL COMMENT '操作IP',
  `user_agent` varchar(512) DEFAULT NULL COMMENT '用户代理',
  `duration` bigint(20) DEFAULT NULL COMMENT '执行时长(ms)',
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
  KEY `idx_tenant_user` (`tenant_id`, `user_id`),
  KEY `idx_tenant_time` (`tenant_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

CREATE TABLE `sys_password_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `password` varchar(128) NOT NULL COMMENT '历史密码(BCrypt)',
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
  KEY `idx_tenant_user` (`tenant_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='密码历史表';

-- =====================================================================
-- 4. wechat_oa 库 - 公众号管理（7 张表）
-- =====================================================================
USE `wechat_oa`;

DROP TABLE IF EXISTS `wechat_oa_menu`;
DROP TABLE IF EXISTS `wechat_oa_auto_reply_rule`;
DROP TABLE IF EXISTS `wechat_oa_user_tag`;
DROP TABLE IF EXISTS `wechat_oa_fan_user`;
DROP TABLE IF EXISTS `wechat_oa_article`;
DROP TABLE IF EXISTS `wechat_oa_material`;
DROP TABLE IF EXISTS `wechat_oa_account`;

CREATE TABLE `wechat_oa_account` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `account_name` varchar(128) NOT NULL COMMENT '公众号名称',
  `app_id` varchar(64) NOT NULL COMMENT '微信AppID',
  `app_secret` varchar(128) NOT NULL COMMENT '微信AppSecret(加密存储)',
  `token` varchar(128) DEFAULT NULL COMMENT '微信Token',
  `aes_key` varchar(128) DEFAULT NULL COMMENT '消息加密密钥',
  `account_type` tinyint(4) NOT NULL DEFAULT 0 COMMENT '类型 0-订阅号 1-服务号',
  `is_verified` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否认证 0-否 1-是',
  `qr_code_url` varchar(512) DEFAULT NULL COMMENT '公众号二维码URL',
  `access_token` varchar(512) DEFAULT NULL COMMENT '当前AccessToken(加密存储)',
  `access_token_expire_time` datetime DEFAULT NULL COMMENT 'AccessToken过期时间',
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
  UNIQUE KEY `uk_app_id` (`app_id`),
  KEY `idx_tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公众号账号表';

CREATE TABLE `wechat_oa_material` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `account_id` bigint(20) NOT NULL COMMENT '公众号ID',
  `media_id` varchar(128) DEFAULT NULL COMMENT '微信素材MediaID',
  `material_type` tinyint(4) NOT NULL COMMENT '类型 0-图片 1-语音 2-视频 3-缩略图',
  `title` varchar(256) DEFAULT NULL COMMENT '素材标题',
  `file_name` varchar(256) DEFAULT NULL COMMENT '原始文件名',
  `file_url` varchar(512) DEFAULT NULL COMMENT '本地存储URL(MinIO)',
  `file_size` bigint(20) DEFAULT NULL COMMENT '文件大小(字节)',
  `wechat_url` varchar(512) DEFAULT NULL COMMENT '微信端URL',
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
  KEY `idx_tenant_account` (`tenant_id`, `account_id`),
  KEY `idx_media_id` (`media_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='素材表';

CREATE TABLE `wechat_oa_article` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `account_id` bigint(20) NOT NULL COMMENT '公众号ID',
  `title` varchar(256) NOT NULL COMMENT '标题',
  `author` varchar(64) DEFAULT NULL COMMENT '作者',
  `digest` varchar(512) DEFAULT NULL COMMENT '摘要',
  `content` mediumtext COMMENT '正文(HTML)',
  `thumb_media_id` varchar(128) DEFAULT NULL COMMENT '封面素材ID',
  `thumb_url` varchar(512) DEFAULT NULL COMMENT '封面URL',
  `content_source_url` varchar(512) DEFAULT NULL COMMENT '原文链接',
  `wx_media_id` varchar(128) DEFAULT NULL COMMENT '微信端图文素材ID',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态 0-草稿 1-已发布 2-已下线',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `read_count` int(11) NOT NULL DEFAULT 0 COMMENT '阅读数',
  `share_count` int(11) NOT NULL DEFAULT 0 COMMENT '分享数',
  `like_count` int(11) NOT NULL DEFAULT 0 COMMENT '点赞数',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '多图文排序',
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
  KEY `idx_tenant_account` (`tenant_id`, `account_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图文表';

CREATE TABLE `wechat_oa_fan_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `account_id` bigint(20) NOT NULL COMMENT '公众号ID',
  `openid` varchar(64) NOT NULL COMMENT '微信OpenID',
  `unionid` varchar(64) DEFAULT NULL COMMENT '微信UnionID',
  `nickname` varchar(128) DEFAULT NULL COMMENT '昵称',
  `avatar_url` varchar(512) DEFAULT NULL COMMENT '头像URL',
  `gender` tinyint(4) DEFAULT 0 COMMENT '性别 0-未知 1-男 2-女',
  `country` varchar(64) DEFAULT NULL COMMENT '国家',
  `province` varchar(64) DEFAULT NULL COMMENT '省份',
  `city` varchar(64) DEFAULT NULL COMMENT '城市',
  `language` varchar(32) DEFAULT NULL COMMENT '语言',
  `subscribe_status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '关注状态 0-已取关 1-已关注',
  `subscribe_time` datetime DEFAULT NULL COMMENT '关注时间',
  `unsubscribe_time` datetime DEFAULT NULL COMMENT '取关时间',
  `subscribe_scene` varchar(64) DEFAULT NULL COMMENT '关注渠道',
  `is_blacklisted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否拉黑 0-否 1-是',
  `tag_ids` varchar(512) DEFAULT NULL COMMENT '标签ID列表(JSON)',
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
  UNIQUE KEY `uk_account_openid` (`account_id`, `openid`),
  KEY `idx_tenant_account` (`tenant_id`, `account_id`),
  KEY `idx_subscribe` (`subscribe_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='粉丝表';

CREATE TABLE `wechat_oa_user_tag` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `account_id` bigint(20) NOT NULL COMMENT '公众号ID',
  `wx_tag_id` int(11) DEFAULT NULL COMMENT '微信端标签ID',
  `tag_name` varchar(64) NOT NULL COMMENT '标签名称',
  `fan_count` int(11) NOT NULL DEFAULT 0 COMMENT '粉丝数',
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
  KEY `idx_tenant_account` (`tenant_id`, `account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='粉丝标签表';

CREATE TABLE `wechat_oa_auto_reply_rule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `account_id` bigint(20) NOT NULL COMMENT '公众号ID',
  `rule_name` varchar(128) NOT NULL COMMENT '规则名称',
  `rule_type` tinyint(4) NOT NULL COMMENT '类型 0-关注回复 1-关键词回复 2-默认回复',
  `keyword` varchar(256) DEFAULT NULL COMMENT '关键词',
  `match_type` tinyint(4) DEFAULT NULL COMMENT '匹配方式 0-全匹配 1-半匹配',
  `reply_type` tinyint(4) NOT NULL COMMENT '回复类型 0-文本 1-图片 2-图文',
  `reply_content` text COMMENT '回复内容',
  `reply_media_id` varchar(128) DEFAULT NULL COMMENT '回复素材ID',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '排序',
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
  KEY `idx_tenant_account_type` (`tenant_id`, `account_id`, `rule_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='自动回复规则表';

CREATE TABLE `wechat_oa_menu` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `account_id` bigint(20) NOT NULL COMMENT '公众号ID',
  `menu_name` varchar(64) NOT NULL COMMENT '菜单名称',
  `parent_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '父菜单ID 0-一级',
  `menu_type` varchar(32) NOT NULL COMMENT '菜单类型 click/view/miniprogram等',
  `menu_key` varchar(128) DEFAULT NULL COMMENT '菜单KEY(click类型)',
  `menu_url` varchar(512) DEFAULT NULL COMMENT '菜单URL(view类型)',
  `media_id` varchar(128) DEFAULT NULL COMMENT '素材ID',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '排序',
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
  KEY `idx_tenant_account` (`tenant_id`, `account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公众号菜单表';

-- =====================================================================
-- 5. notify 库 - 消息通知（3 张表）
-- =====================================================================
USE `notify`;

DROP TABLE IF EXISTS `notify_channel_config`;
DROP TABLE IF EXISTS `notify_template`;
DROP TABLE IF EXISTS `notify_message`;

CREATE TABLE `notify_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `receiver_id` bigint(20) NOT NULL COMMENT '接收人ID',
  `sender_id` bigint(20) DEFAULT NULL COMMENT '发送人ID',
  `sender_name` varchar(64) DEFAULT NULL COMMENT '发送人姓名',
  `title` varchar(256) NOT NULL COMMENT '消息标题',
  `content` text COMMENT '消息内容',
  `type` tinyint(4) NOT NULL COMMENT '类型 0-系统通知 1-审批通知 2-催办 3-公告',
  `biz_type` varchar(64) DEFAULT NULL COMMENT '业务类型',
  `biz_id` varchar(128) DEFAULT NULL COMMENT '业务ID',
  `jump_url` varchar(512) DEFAULT NULL COMMENT '跳转链接',
  `is_read` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否已读 0-未读 1-已读',
  `read_time` datetime DEFAULT NULL COMMENT '阅读时间',
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
  KEY `idx_tenant_receiver` (`tenant_id`, `receiver_id`, `is_read`),
  KEY `idx_tenant_time` (`tenant_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内消息表';

CREATE TABLE `notify_template` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `template_code` varchar(64) NOT NULL COMMENT '模板编码',
  `template_name` varchar(128) NOT NULL COMMENT '模板名称',
  `type` tinyint(4) NOT NULL COMMENT '渠道 0-站内信 1-邮件 2-IM Webhook',
  `title_template` varchar(256) DEFAULT NULL COMMENT '标题模板',
  `content_template` text COMMENT '内容模板',
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
  UNIQUE KEY `uk_code_type` (`template_code`, `type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知模板表';

CREATE TABLE `notify_channel_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `channel_type` tinyint(4) NOT NULL COMMENT '渠道 0-站内信 1-邮件 2-飞书 3-钉钉 4-企业微信',
  `enabled` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否启用 0-否 1-是',
  `config_json` text COMMENT '渠道配置(JSON)',
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
  UNIQUE KEY `uk_tenant_channel` (`tenant_id`, `channel_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户通知渠道配置表';

-- =====================================================================
-- 6. workflow 库 - 流程管理（5 张表）
-- =====================================================================
USE `workflow`;

DROP TABLE IF EXISTS `wf_node_config`;
DROP TABLE IF EXISTS `wf_copy`;
DROP TABLE IF EXISTS `wf_task_ext`;
DROP TABLE IF EXISTS `wf_process_instance_ext`;
DROP TABLE IF EXISTS `wf_process_definition_ext`;

CREATE TABLE `wf_process_definition_ext` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `process_definition_id` varchar(128) NOT NULL COMMENT 'Flowable流程定义ID',
  `process_key` varchar(128) NOT NULL COMMENT '流程标识',
  `process_name` varchar(256) NOT NULL COMMENT '流程名称',
  `category` varchar(64) DEFAULT NULL COMMENT '分类',
  `icon` varchar(512) DEFAULT NULL COMMENT '流程图标URL',
  `description` varchar(1024) DEFAULT NULL COMMENT '流程说明',
  `form_type` tinyint(4) NOT NULL DEFAULT 0 COMMENT '表单类型 0-外链 1-内嵌JSON',
  `form_url` varchar(512) DEFAULT NULL COMMENT '表单URL',
  `form_config` text COMMENT '表单配置(JSON)',
  `is_template` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否平台模板 0-自定义 1-模板',
  `version` int(11) NOT NULL DEFAULT 1 COMMENT '版本号',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 0-挂起 1-激活',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '排序',
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
  KEY `idx_tenant_key` (`tenant_id`, `process_key`),
  KEY `idx_flowable_def_id` (`process_definition_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程定义扩展表';

CREATE TABLE `wf_process_instance_ext` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `process_instance_id` varchar(128) NOT NULL COMMENT 'Flowable流程实例ID',
  `process_definition_id` varchar(128) NOT NULL COMMENT 'Flowable流程定义ID',
  `process_key` varchar(128) DEFAULT NULL COMMENT '流程标识',
  `process_name` varchar(256) DEFAULT NULL COMMENT '流程名称',
  `title` varchar(256) DEFAULT NULL COMMENT '流程标题',
  `initiator_id` bigint(20) NOT NULL COMMENT '发起人ID',
  `initiator_name` varchar(64) DEFAULT NULL COMMENT '发起人姓名',
  `initiator_dept_id` bigint(20) DEFAULT NULL COMMENT '发起人部门ID',
  `business_key` varchar(256) DEFAULT NULL COMMENT '业务关联键',
  `form_data` text COMMENT '表单数据(JSON)',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态 0-进行中 1-已完成 2-已撤回 3-已终止',
  `result` tinyint(4) DEFAULT NULL COMMENT '结果 1-通过 2-驳回',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `duration` bigint(20) DEFAULT NULL COMMENT '耗时(ms)',
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
  UNIQUE KEY `uk_instance_id` (`process_instance_id`),
  KEY `idx_tenant_initiator` (`tenant_id`, `initiator_id`),
  KEY `idx_tenant_status` (`tenant_id`, `status`),
  KEY `idx_tenant_time` (`tenant_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程实例扩展表';

CREATE TABLE `wf_task_ext` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `task_id` varchar(128) NOT NULL COMMENT 'Flowable任务ID',
  `process_instance_id` varchar(128) NOT NULL COMMENT '流程实例ID',
  `task_name` varchar(256) DEFAULT NULL COMMENT '任务名称',
  `assignee_id` bigint(20) DEFAULT NULL COMMENT '当前处理人ID',
  `assignee_name` varchar(64) DEFAULT NULL COMMENT '当前处理人姓名',
  `owner_id` bigint(20) DEFAULT NULL COMMENT '任务所有人ID',
  `action` tinyint(4) DEFAULT NULL COMMENT '操作 1-通过 2-驳回 3-转办 4-委派 5-加签',
  `comment` varchar(2048) DEFAULT NULL COMMENT '审批意见',
  `duration` bigint(20) DEFAULT NULL COMMENT '处理耗时(ms)',
  `complete_time` datetime DEFAULT NULL COMMENT '完成时间',
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
  KEY `idx_task_id` (`task_id`),
  KEY `idx_tenant_assignee` (`tenant_id`, `assignee_id`),
  KEY `idx_instance_id` (`process_instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务扩展表';

CREATE TABLE `wf_copy` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `process_instance_id` varchar(128) NOT NULL COMMENT '流程实例ID',
  `process_name` varchar(256) DEFAULT NULL COMMENT '流程名称',
  `title` varchar(256) DEFAULT NULL COMMENT '流程标题',
  `initiator_id` bigint(20) NOT NULL COMMENT '发起人ID',
  `initiator_name` varchar(64) DEFAULT NULL COMMENT '发起人姓名',
  `receiver_id` bigint(20) NOT NULL COMMENT '接收人ID',
  `receiver_name` varchar(64) DEFAULT NULL COMMENT '接收人姓名',
  `task_name` varchar(256) DEFAULT NULL COMMENT '发生在哪个节点',
  `is_read` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否已读 0-未读 1-已读',
  `read_time` datetime DEFAULT NULL COMMENT '阅读时间',
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
  KEY `idx_tenant_receiver` (`tenant_id`, `receiver_id`, `is_read`),
  KEY `idx_instance_id` (`process_instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程抄送表';

CREATE TABLE `wf_node_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `process_definition_id` varchar(128) NOT NULL COMMENT '流程定义ID',
  `node_id` varchar(128) NOT NULL COMMENT 'BPMN节点ID',
  `node_name` varchar(256) DEFAULT NULL COMMENT '节点名称',
  `assignee_type` tinyint(4) NOT NULL COMMENT '审批人类型 1-指定用户 2-指定角色 3-部门负责人 4-发起人自选',
  `assignee_ids` varchar(1024) DEFAULT NULL COMMENT '审批人/角色ID列表(JSON)',
  `approval_mode` tinyint(4) NOT NULL DEFAULT 1 COMMENT '审批模式 1-或签 2-会签 3-依次',
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
  UNIQUE KEY `uk_def_node` (`process_definition_id`, `node_id`),
  KEY `idx_tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程节点审批人配置表';

-- #####################################################################
--                        种子数据（SEED DATA）
-- #####################################################################

-- =====================================================================
-- 7. platform 种子数据
-- =====================================================================
USE `platform`;

-- 7.1 套餐
INSERT INTO `sys_package` (`id`, `package_name`, `package_code`, `price_monthly`, `price_yearly`, `max_users`, `max_roles`, `max_depts`, `max_process_definitions`, `max_wechat_accounts`, `max_storage_mb`, `menu_ids`, `sort_order`, `status`, `remark`) VALUES
(1, '免费版',   'FREE',       0.00,    0.00,    10,  5,   10,  5,   1,  1024,   NULL, 1, 1, '免费体验套餐'),
(2, '基础版',   'BASIC',      299.00,  2990.00, 50,  20,  50,  20,  3,  10240,  NULL, 2, 1, '适合小型团队'),
(3, '专业版',   'PRO',        999.00,  9990.00, 200, 50,  200, 0,   10, 102400, NULL, 3, 1, '适合中型企业'),
(4, '旗舰版',   'ENTERPRISE', 0.00,    0.00,    0,   0,   0,   0,   0,  0,      NULL, 4, 1, '按需定制，所有配额不限');

-- 7.2 默认租户
INSERT INTO `sys_tenant` (`id`, `tenant_name`, `tenant_code`, `contact_person`, `contact_phone`, `contact_email`, `status`, `package_id`, `admin_user_id`, `remark`) VALUES
(1, '默认租户', 'DEFAULT', '系统管理员', '13800000000', 'admin@saas-cloud.com', 1, 4, 1, '系统初始化默认租户');

-- 7.3 平台超级管理员 (密码: Admin@2026)
INSERT INTO `sys_platform_user` (`id`, `username`, `password`, `real_name`, `role_type`, `status`) VALUES
(1, 'admin', '$2a$10$hrXlewp4KX10seMt1TBO4ehzSO0SGTWMsVti87zVKOP0K0NjGF2kC', '平台管理员', 0, 1);

-- 7.4 全局配置
INSERT INTO `sys_global_config` (`config_key`, `config_value`, `config_type`, `description`) VALUES
('default_package_id',   '1',   'NUMBER',  '默认套餐ID'),
('trial_days',           '15',  'NUMBER',  '试用天数'),
('max_login_attempts',   '5',   'NUMBER',  '最大登录尝试次数'),
('password_min_length',  '8',   'NUMBER',  '密码最小长度'),
('login_lock_minutes',   '30',  'NUMBER',  '登录锁定时长(分钟)');

-- =====================================================================
-- 8. rbac 种子数据
-- =====================================================================
USE `rbac`;

-- 8.1 部门（树形结构）
INSERT INTO `sys_dept` (`id`, `tenant_id`, `dept_name`, `parent_id`, `ancestors`, `leader_user_id`, `leader`, `sort_order`, `status`, `create_user_id`, `create_user_name`) VALUES
(1, 1, '总部',     0, '0',     1, '系统管理员', 1, 1, '1', '系统管理员'),
(2, 1, '技术部',   1, '0,1',   NULL, NULL,       1, 1, '1', '系统管理员'),
(3, 1, '产品部',   1, '0,1',   NULL, NULL,       2, 1, '1', '系统管理员'),
(4, 1, '运营部',   1, '0,1',   NULL, NULL,       3, 1, '1', '系统管理员'),
(5, 1, '人事行政部', 1, '0,1', NULL, NULL,       4, 1, '1', '系统管理员');

-- 8.2 角色
INSERT INTO `sys_role` (`id`, `tenant_id`, `role_name`, `role_code`, `role_level`, `data_scope`, `sort_order`, `status`, `is_system`, `create_user_id`, `create_user_name`, `remark`) VALUES
(1, 1, '超级管理员', 'SUPER_ADMIN',  0, 1, 1, 1, 1, '1', '系统管理员', '拥有全部权限，不可删除'),
(2, 1, '管理员',     'ADMIN',        1, 1, 2, 1, 1, '1', '系统管理员', '管理员角色，可管理用户和部门'),
(3, 1, '普通用户',   'USER',         2, 4, 3, 1, 1, '1', '系统管理员', '普通用户角色，仅查看权限');

-- 8.3 租户超级管理员用户 (密码: Admin@2026)
INSERT INTO `sys_user` (`id`, `tenant_id`, `username`, `password`, `real_name`, `phone`, `email`, `gender`, `dept_id`, `status`, `role_level`, `create_user_id`, `create_user_name`) VALUES
(1, 1, 'admin', '$2a$10$hrXlewp4KX10seMt1TBO4ehzSO0SGTWMsVti87zVKOP0K0NjGF2kC', '系统管理员', '13800000000', 'admin@saas-cloud.com', 1, 1, 1, 0, '1', '系统管理员');

-- 8.4 用户-角色关联
INSERT INTO `sys_user_role` (`id`, `tenant_id`, `user_id`, `role_id`, `create_user_id`, `create_user_name`) VALUES
(1, 1, 1, 1, '1', '系统管理员');

-- 8.5 菜单（平台级 - 全量）

-- === 一级目录 ===
INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort_order`, `status`, `visible`, `module`) VALUES
(1,  '仪表盘',     0, 0, '/dashboard',  'BasicLayout', 'lucide:layout-dashboard', 1,  1, 1, 'RBAC'),
(2,  '系统管理',   0, 0, '/system',     'BasicLayout', 'lucide:settings',         10, 1, 1, 'RBAC'),
(3,  '流程管理',   0, 0, '/workflow',   'BasicLayout', 'lucide:git-branch',       20, 1, 1, 'WORKFLOW'),
(4,  '公众号管理', 0, 0, '/wechat',     'BasicLayout', 'lucide:message-circle',   30, 1, 1, 'WECHAT_OA'),
(5,  '通知管理',   0, 0, '/notify',     'BasicLayout', 'lucide:bell',             40, 1, 1, 'RBAC');

-- === 仪表盘子菜单 ===
INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort_order`, `status`, `visible`, `module`) VALUES
(101, '分析页', 1, 1, '/dashboard/analytics', '/views/dashboard/analytics/index', 'lucide:bar-chart-3', 1, 1, 1, 'RBAC'),
(102, '工作台', 1, 1, '/dashboard/workspace', '/views/dashboard/workspace/index', 'lucide:monitor',     2, 1, 1, 'RBAC');

-- === 系统管理子菜单 ===
INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort_order`, `status`, `visible`, `module`) VALUES
(201, '用户管理', 2, 1, '/system/user',     '/views/system/user/index',     'lucide:users',      1, 1, 1, 'RBAC'),
(202, '角色管理', 2, 1, '/system/role',     '/views/system/role/index',     'lucide:shield',     2, 1, 1, 'RBAC'),
(203, '部门管理', 2, 1, '/system/dept',     '/views/system/dept/index',     'lucide:building',   3, 1, 1, 'RBAC'),
(204, '个人设置', 2, 1, '/system/profile',  '/views/system/profile/index',  'lucide:user-cog',   4, 1, 1, 'RBAC'),
(205, '操作日志', 2, 1, '/system/log',      '/views/system/log/index',      'lucide:file-clock', 5, 1, 1, 'RBAC');

-- === 流程管理子菜单 ===
INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort_order`, `status`, `visible`, `module`) VALUES
(301, '流程定义',   3, 1, '/workflow/definition', '/views/workflow/definition/index', 'lucide:file-text',    1, 1, 1, 'WORKFLOW'),
(302, '发起流程',   3, 1, '/workflow/start',      '/views/workflow/start/index',      'lucide:play-circle',  2, 1, 1, 'WORKFLOW'),
(303, '我的待办',   3, 1, '/workflow/todo',       '/views/workflow/todo/index',       'lucide:clock',        3, 1, 1, 'WORKFLOW'),
(304, '我的已办',   3, 1, '/workflow/done',       '/views/workflow/done/index',       'lucide:check-circle', 4, 1, 1, 'WORKFLOW'),
(305, '我发起的',   3, 1, '/workflow/initiated',  '/views/workflow/initiated/index',  'lucide:send',         5, 1, 1, 'WORKFLOW'),
(306, '流程监控',   3, 1, '/workflow/monitor',    '/views/workflow/monitor/index',    'lucide:activity',     6, 1, 1, 'WORKFLOW');

-- === 公众号管理子菜单 ===
INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort_order`, `status`, `visible`, `module`) VALUES
(401, '账号管理',   4, 1, '/wechat/account',    '/views/wechat/account/index',    'lucide:key',       1, 1, 1, 'WECHAT_OA'),
(402, '素材管理',   4, 1, '/wechat/material',   '/views/wechat/material/index',   'lucide:image',     2, 1, 1, 'WECHAT_OA'),
(403, '图文管理',   4, 1, '/wechat/article',    '/views/wechat/article/index',    'lucide:newspaper', 3, 1, 1, 'WECHAT_OA'),
(404, '粉丝管理',   4, 1, '/wechat/fan',        '/views/wechat/fan/index',        'lucide:heart',     4, 1, 1, 'WECHAT_OA'),
(405, '标签管理',   4, 1, '/wechat/tag',        '/views/wechat/tag/index',        'lucide:tag',       5, 1, 1, 'WECHAT_OA'),
(406, '自动回复',   4, 1, '/wechat/auto-reply', '/views/wechat/auto-reply/index', 'lucide:reply',     6, 1, 1, 'WECHAT_OA'),
(407, '菜单编辑',   4, 1, '/wechat/menu',       '/views/wechat/menu/index',       'lucide:menu',      7, 1, 1, 'WECHAT_OA'),
(408, '数据看板',   4, 1, '/wechat/dashboard',  '/views/wechat/dashboard/index',  'lucide:pie-chart', 8, 1, 1, 'WECHAT_OA');

-- === 通知管理子菜单 ===
INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort_order`, `status`, `visible`, `module`) VALUES
(501, '站内消息', 5, 1, '/notify/message',  '/views/notify/message/index',  'lucide:mail',          1, 1, 1, 'RBAC'),
(502, '通知模板', 5, 1, '/notify/template', '/views/notify/template/index', 'lucide:file-template', 2, 1, 1, 'RBAC'),
(503, '渠道配置', 5, 1, '/notify/channel',  '/views/notify/channel/index',  'lucide:radio',         3, 1, 1, 'RBAC');

-- === 按钮权限 - 系统管理 ===
INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `menu_type`, `permission`, `sort_order`, `status`, `visible`, `module`) VALUES
-- 用户管理按钮
(2011, '用户查询', 201, 2, 'system:user:query',    1, 1, 1, 'RBAC'),
(2012, '用户新增', 201, 2, 'system:user:create',   2, 1, 1, 'RBAC'),
(2013, '用户编辑', 201, 2, 'system:user:update',   3, 1, 1, 'RBAC'),
(2014, '用户删除', 201, 2, 'system:user:delete',   4, 1, 1, 'RBAC'),
(2015, '重置密码', 201, 2, 'system:user:resetpwd', 5, 1, 1, 'RBAC'),
(2016, '用户导出', 201, 2, 'system:user:export',   6, 1, 1, 'RBAC'),
-- 角色管理按钮
(2021, '角色查询', 202, 2, 'system:role:query',    1, 1, 1, 'RBAC'),
(2022, '角色新增', 202, 2, 'system:role:create',   2, 1, 1, 'RBAC'),
(2023, '角色编辑', 202, 2, 'system:role:update',   3, 1, 1, 'RBAC'),
(2024, '角色删除', 202, 2, 'system:role:delete',   4, 1, 1, 'RBAC'),
(2025, '分配权限', 202, 2, 'system:role:assign',   5, 1, 1, 'RBAC'),
-- 部门管理按钮
(2031, '部门查询', 203, 2, 'system:dept:query',    1, 1, 1, 'RBAC'),
(2032, '部门新增', 203, 2, 'system:dept:create',   2, 1, 1, 'RBAC'),
(2033, '部门编辑', 203, 2, 'system:dept:update',   3, 1, 1, 'RBAC'),
(2034, '部门删除', 203, 2, 'system:dept:delete',   4, 1, 1, 'RBAC'),
-- 操作日志按钮
(2051, '日志查询', 205, 2, 'system:log:query',     1, 1, 1, 'RBAC'),
(2052, '日志导出', 205, 2, 'system:log:export',    2, 1, 1, 'RBAC'),
(2053, '日志删除', 205, 2, 'system:log:delete',    3, 1, 1, 'RBAC');

-- === 按钮权限 - 流程管理 ===
INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `menu_type`, `permission`, `sort_order`, `status`, `visible`, `module`) VALUES
(3011, '流程定义查询', 301, 2, 'workflow:definition:query',  1, 1, 1, 'WORKFLOW'),
(3012, '流程定义新增', 301, 2, 'workflow:definition:create', 2, 1, 1, 'WORKFLOW'),
(3013, '流程定义编辑', 301, 2, 'workflow:definition:update', 3, 1, 1, 'WORKFLOW'),
(3014, '流程定义删除', 301, 2, 'workflow:definition:delete', 4, 1, 1, 'WORKFLOW'),
(3015, '流程部署',     301, 2, 'workflow:definition:deploy', 5, 1, 1, 'WORKFLOW'),
(3061, '流程监控查询', 306, 2, 'workflow:monitor:query',     1, 1, 1, 'WORKFLOW'),
(3062, '流程终止',     306, 2, 'workflow:monitor:terminate', 2, 1, 1, 'WORKFLOW');

-- === 按钮权限 - 公众号管理 ===
INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `menu_type`, `permission`, `sort_order`, `status`, `visible`, `module`) VALUES
(4011, '账号查询', 401, 2, 'wechat:account:query',    1, 1, 1, 'WECHAT_OA'),
(4012, '账号新增', 401, 2, 'wechat:account:create',   2, 1, 1, 'WECHAT_OA'),
(4013, '账号编辑', 401, 2, 'wechat:account:update',   3, 1, 1, 'WECHAT_OA'),
(4014, '账号删除', 401, 2, 'wechat:account:delete',   4, 1, 1, 'WECHAT_OA'),
(4021, '素材查询', 402, 2, 'wechat:material:query',   1, 1, 1, 'WECHAT_OA'),
(4022, '素材上传', 402, 2, 'wechat:material:upload',  2, 1, 1, 'WECHAT_OA'),
(4023, '素材删除', 402, 2, 'wechat:material:delete',  3, 1, 1, 'WECHAT_OA'),
(4031, '图文查询', 403, 2, 'wechat:article:query',    1, 1, 1, 'WECHAT_OA'),
(4032, '图文新增', 403, 2, 'wechat:article:create',   2, 1, 1, 'WECHAT_OA'),
(4033, '图文编辑', 403, 2, 'wechat:article:update',   3, 1, 1, 'WECHAT_OA'),
(4034, '图文删除', 403, 2, 'wechat:article:delete',   4, 1, 1, 'WECHAT_OA'),
(4035, '图文发布', 403, 2, 'wechat:article:publish',  5, 1, 1, 'WECHAT_OA'),
(4041, '粉丝查询', 404, 2, 'wechat:fan:query',        1, 1, 1, 'WECHAT_OA'),
(4042, '粉丝拉黑', 404, 2, 'wechat:fan:blacklist',    2, 1, 1, 'WECHAT_OA'),
(4043, '粉丝同步', 404, 2, 'wechat:fan:sync',         3, 1, 1, 'WECHAT_OA'),
(4051, '标签查询', 405, 2, 'wechat:tag:query',        1, 1, 1, 'WECHAT_OA'),
(4052, '标签新增', 405, 2, 'wechat:tag:create',       2, 1, 1, 'WECHAT_OA'),
(4053, '标签删除', 405, 2, 'wechat:tag:delete',       3, 1, 1, 'WECHAT_OA'),
(4061, '回复规则查询', 406, 2, 'wechat:reply:query',  1, 1, 1, 'WECHAT_OA'),
(4062, '回复规则新增', 406, 2, 'wechat:reply:create', 2, 1, 1, 'WECHAT_OA'),
(4063, '回复规则编辑', 406, 2, 'wechat:reply:update', 3, 1, 1, 'WECHAT_OA'),
(4064, '回复规则删除', 406, 2, 'wechat:reply:delete', 4, 1, 1, 'WECHAT_OA'),
(4071, '菜单查询',   407, 2, 'wechat:menu:query',     1, 1, 1, 'WECHAT_OA'),
(4072, '菜单编辑',   407, 2, 'wechat:menu:update',    2, 1, 1, 'WECHAT_OA'),
(4073, '菜单同步',   407, 2, 'wechat:menu:sync',      3, 1, 1, 'WECHAT_OA');

-- === 按钮权限 - 通知管理 ===
INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `menu_type`, `permission`, `sort_order`, `status`, `visible`, `module`) VALUES
(5011, '消息查询', 501, 2, 'notify:message:query',     1, 1, 1, 'RBAC'),
(5012, '消息删除', 501, 2, 'notify:message:delete',    2, 1, 1, 'RBAC'),
(5021, '模板查询', 502, 2, 'notify:template:query',    1, 1, 1, 'RBAC'),
(5022, '模板新增', 502, 2, 'notify:template:create',   2, 1, 1, 'RBAC'),
(5023, '模板编辑', 502, 2, 'notify:template:update',   3, 1, 1, 'RBAC'),
(5024, '模板删除', 502, 2, 'notify:template:delete',   4, 1, 1, 'RBAC'),
(5031, '渠道查询', 503, 2, 'notify:channel:query',     1, 1, 1, 'RBAC'),
(5032, '渠道编辑', 503, 2, 'notify:channel:update',    2, 1, 1, 'RBAC');

-- 8.6 超级管理员角色 - 分配所有菜单权限
INSERT INTO `sys_role_menu` (`tenant_id`, `role_id`, `menu_id`, `create_user_id`, `create_user_name`) VALUES
-- 一级目录
(1, 1, 1, '1', '系统管理员'),
(1, 1, 2, '1', '系统管理员'),
(1, 1, 3, '1', '系统管理员'),
(1, 1, 4, '1', '系统管理员'),
(1, 1, 5, '1', '系统管理员'),
-- 仪表盘
(1, 1, 101, '1', '系统管理员'),
(1, 1, 102, '1', '系统管理员'),
-- 系统管理
(1, 1, 201, '1', '系统管理员'),
(1, 1, 202, '1', '系统管理员'),
(1, 1, 203, '1', '系统管理员'),
(1, 1, 204, '1', '系统管理员'),
(1, 1, 205, '1', '系统管理员'),
-- 流程管理
(1, 1, 301, '1', '系统管理员'),
(1, 1, 302, '1', '系统管理员'),
(1, 1, 303, '1', '系统管理员'),
(1, 1, 304, '1', '系统管理员'),
(1, 1, 305, '1', '系统管理员'),
(1, 1, 306, '1', '系统管理员'),
-- 公众号管理
(1, 1, 401, '1', '系统管理员'),
(1, 1, 402, '1', '系统管理员'),
(1, 1, 403, '1', '系统管理员'),
(1, 1, 404, '1', '系统管理员'),
(1, 1, 405, '1', '系统管理员'),
(1, 1, 406, '1', '系统管理员'),
(1, 1, 407, '1', '系统管理员'),
(1, 1, 408, '1', '系统管理员'),
-- 通知管理
(1, 1, 501, '1', '系统管理员'),
(1, 1, 502, '1', '系统管理员'),
(1, 1, 503, '1', '系统管理员'),
-- 用户管理按钮
(1, 1, 2011, '1', '系统管理员'),
(1, 1, 2012, '1', '系统管理员'),
(1, 1, 2013, '1', '系统管理员'),
(1, 1, 2014, '1', '系统管理员'),
(1, 1, 2015, '1', '系统管理员'),
(1, 1, 2016, '1', '系统管理员'),
-- 角色管理按钮
(1, 1, 2021, '1', '系统管理员'),
(1, 1, 2022, '1', '系统管理员'),
(1, 1, 2023, '1', '系统管理员'),
(1, 1, 2024, '1', '系统管理员'),
(1, 1, 2025, '1', '系统管理员'),
-- 部门管理按钮
(1, 1, 2031, '1', '系统管理员'),
(1, 1, 2032, '1', '系统管理员'),
(1, 1, 2033, '1', '系统管理员'),
(1, 1, 2034, '1', '系统管理员'),
-- 操作日志按钮
(1, 1, 2051, '1', '系统管理员'),
(1, 1, 2052, '1', '系统管理员'),
(1, 1, 2053, '1', '系统管理员'),
-- 流程管理按钮
(1, 1, 3011, '1', '系统管理员'),
(1, 1, 3012, '1', '系统管理员'),
(1, 1, 3013, '1', '系统管理员'),
(1, 1, 3014, '1', '系统管理员'),
(1, 1, 3015, '1', '系统管理员'),
(1, 1, 3061, '1', '系统管理员'),
(1, 1, 3062, '1', '系统管理员'),
-- 公众号管理按钮
(1, 1, 4011, '1', '系统管理员'),
(1, 1, 4012, '1', '系统管理员'),
(1, 1, 4013, '1', '系统管理员'),
(1, 1, 4014, '1', '系统管理员'),
(1, 1, 4021, '1', '系统管理员'),
(1, 1, 4022, '1', '系统管理员'),
(1, 1, 4023, '1', '系统管理员'),
(1, 1, 4031, '1', '系统管理员'),
(1, 1, 4032, '1', '系统管理员'),
(1, 1, 4033, '1', '系统管理员'),
(1, 1, 4034, '1', '系统管理员'),
(1, 1, 4035, '1', '系统管理员'),
(1, 1, 4041, '1', '系统管理员'),
(1, 1, 4042, '1', '系统管理员'),
(1, 1, 4043, '1', '系统管理员'),
(1, 1, 4051, '1', '系统管理员'),
(1, 1, 4052, '1', '系统管理员'),
(1, 1, 4053, '1', '系统管理员'),
(1, 1, 4061, '1', '系统管理员'),
(1, 1, 4062, '1', '系统管理员'),
(1, 1, 4063, '1', '系统管理员'),
(1, 1, 4064, '1', '系统管理员'),
(1, 1, 4071, '1', '系统管理员'),
(1, 1, 4072, '1', '系统管理员'),
(1, 1, 4073, '1', '系统管理员'),
-- 通知管理按钮
(1, 1, 5011, '1', '系统管理员'),
(1, 1, 5012, '1', '系统管理员'),
(1, 1, 5021, '1', '系统管理员'),
(1, 1, 5022, '1', '系统管理员'),
(1, 1, 5023, '1', '系统管理员'),
(1, 1, 5024, '1', '系统管理员'),
(1, 1, 5031, '1', '系统管理员'),
(1, 1, 5032, '1', '系统管理员');

-- 8.7 管理员角色 - 分配系统管理 + 通知管理权限（不含流程监控、公众号管理）
INSERT INTO `sys_role_menu` (`tenant_id`, `role_id`, `menu_id`, `create_user_id`, `create_user_name`) VALUES
(1, 2, 1, '1', '系统管理员'),
(1, 2, 2, '1', '系统管理员'),
(1, 2, 3, '1', '系统管理员'),
(1, 2, 5, '1', '系统管理员'),
(1, 2, 101, '1', '系统管理员'),
(1, 2, 102, '1', '系统管理员'),
(1, 2, 201, '1', '系统管理员'),
(1, 2, 202, '1', '系统管理员'),
(1, 2, 203, '1', '系统管理员'),
(1, 2, 204, '1', '系统管理员'),
(1, 2, 205, '1', '系统管理员'),
(1, 2, 301, '1', '系统管理员'),
(1, 2, 302, '1', '系统管理员'),
(1, 2, 303, '1', '系统管理员'),
(1, 2, 304, '1', '系统管理员'),
(1, 2, 305, '1', '系统管理员'),
(1, 2, 501, '1', '系统管理员'),
(1, 2, 502, '1', '系统管理员'),
(1, 2, 503, '1', '系统管理员'),
(1, 2, 2011, '1', '系统管理员'),
(1, 2, 2012, '1', '系统管理员'),
(1, 2, 2013, '1', '系统管理员'),
(1, 2, 2014, '1', '系统管理员'),
(1, 2, 2015, '1', '系统管理员'),
(1, 2, 2021, '1', '系统管理员'),
(1, 2, 2022, '1', '系统管理员'),
(1, 2, 2023, '1', '系统管理员'),
(1, 2, 2024, '1', '系统管理员'),
(1, 2, 2031, '1', '系统管理员'),
(1, 2, 2032, '1', '系统管理员'),
(1, 2, 2033, '1', '系统管理员'),
(1, 2, 2034, '1', '系统管理员'),
(1, 2, 2051, '1', '系统管理员'),
(1, 2, 3011, '1', '系统管理员'),
(1, 2, 5011, '1', '系统管理员'),
(1, 2, 5021, '1', '系统管理员'),
(1, 2, 5022, '1', '系统管理员'),
(1, 2, 5023, '1', '系统管理员'),
(1, 2, 5024, '1', '系统管理员'),
(1, 2, 5031, '1', '系统管理员'),
(1, 2, 5032, '1', '系统管理员');

-- 8.8 普通用户角色 - 仅仪表盘 + 个人设置 + 流程使用 + 消息查看
INSERT INTO `sys_role_menu` (`tenant_id`, `role_id`, `menu_id`, `create_user_id`, `create_user_name`) VALUES
(1, 3, 1, '1', '系统管理员'),
(1, 3, 2, '1', '系统管理员'),
(1, 3, 3, '1', '系统管理员'),
(1, 3, 5, '1', '系统管理员'),
(1, 3, 101, '1', '系统管理员'),
(1, 3, 102, '1', '系统管理员'),
(1, 3, 204, '1', '系统管理员'),
(1, 3, 302, '1', '系统管理员'),
(1, 3, 303, '1', '系统管理员'),
(1, 3, 304, '1', '系统管理员'),
(1, 3, 305, '1', '系统管理员'),
(1, 3, 501, '1', '系统管理员'),
(1, 3, 5011, '1', '系统管理员');

-- 8.9 初始密码历史记录
INSERT INTO `sys_password_history` (`tenant_id`, `user_id`, `password`, `create_user_id`, `create_user_name`) VALUES
(1, 1, '$2a$10$hrXlewp4KX10seMt1TBO4ehzSO0SGTWMsVti87zVKOP0K0NjGF2kC', '1', '系统管理员');

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
-- 初始化完成
-- =====================================================================
-- 数据库总览:
--   platform  : sys_package, sys_tenant, sys_platform_user, sys_announcement, sys_global_config
--   rbac      : sys_user, sys_role, sys_dept, sys_menu, sys_user_role, sys_role_menu, sys_role_dept, sys_operation_log, sys_password_history
--   wechat_oa : wechat_oa_account, wechat_oa_material, wechat_oa_article, wechat_oa_fan_user, wechat_oa_user_tag, wechat_oa_auto_reply_rule, wechat_oa_menu
--   notify    : notify_message, notify_template, notify_channel_config
--   workflow  : wf_process_definition_ext, wf_process_instance_ext, wf_task_ext, wf_copy, wf_node_config
--
-- 默认账号:
--   平台管理员: admin / Admin@2026 (platform.sys_platform_user)
--   租户管理员: admin / Admin@2026 (rbac.sys_user, tenant_id=1)
--
-- 默认角色:
--   超级管理员(SUPER_ADMIN) - 全部权限
--   管理员(ADMIN)           - 系统管理 + 流程查看 + 通知管理
--   普通用户(USER)          - 仪表盘 + 个人设置 + 流程使用 + 消息查看
-- =====================================================================
