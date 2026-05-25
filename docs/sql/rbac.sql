USE rbac;

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
  `operate_type` varchar(32) DEFAULT NULL COMMENT '操作类型',
  `method` varchar(256) DEFAULT NULL COMMENT '请求方法',
  `request_url` varchar(512) DEFAULT NULL COMMENT '请求URL',
  `request_method` varchar(16) DEFAULT NULL COMMENT 'HTTP方法',
  `request_params` text COMMENT '请求参数',
  `change_diff` text COMMENT '变更内容',
  `response_code` int(11) DEFAULT NULL COMMENT '响应状态码',
  `error_msg` text COMMENT '错误信息',
  `ip` varchar(64) DEFAULT NULL COMMENT '操作IP',
  `location` varchar(128) DEFAULT NULL COMMENT 'IP归属地',
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

-- ========== 菜单种子数据（平台级） ==========

-- 一级菜单（目录）
INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort_order`, `status`, `visible`, `module`) VALUES
(1,  '仪表盘',     0, 0, '/dashboard',  'BasicLayout', 'lucide:layout-dashboard', 1,  1, 1, 'RBAC'),
(2,  '系统管理',   0, 0, '/system',     'BasicLayout', 'lucide:settings',         10, 1, 1, 'RBAC'),
(3,  '流程管理',   0, 0, '/workflow',   'BasicLayout', 'lucide:git-branch',       20, 1, 1, 'WORKFLOW'),
(4,  '公众号管理', 0, 0, '/wechat',     'BasicLayout', 'lucide:message-circle',   30, 1, 1, 'WECHAT_OA');

-- 仪表盘子菜单
INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort_order`, `status`, `visible`, `module`) VALUES
(101, '分析页', 1, 1, '/dashboard/analytics', '/views/dashboard/analytics/index', 'lucide:bar-chart-3', 1, 1, 1, 'RBAC'),
(102, '工作台', 1, 1, '/dashboard/workspace', '/views/dashboard/workspace/index', 'lucide:monitor',     2, 1, 1, 'RBAC');

-- 系统管理子菜单
INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort_order`, `status`, `visible`, `module`) VALUES
(201, '用户管理', 2, 1, '/system/user',    '/views/system/user/index',    'lucide:users',    1, 1, 1, 'RBAC'),
(202, '角色管理', 2, 1, '/system/role',    '/views/system/role/index',    'lucide:shield',   2, 1, 1, 'RBAC'),
(203, '部门管理', 2, 1, '/system/dept',    '/views/system/dept/index',    'lucide:building',  3, 1, 1, 'RBAC'),
(204, '个人设置', 2, 1, '/system/profile', '/views/system/profile/index', 'lucide:user-cog',  4, 1, 1, 'RBAC');

-- 流程管理子菜单
INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort_order`, `status`, `visible`, `module`) VALUES
(301, '流程定义',   3, 1, '/workflow/definition', '/views/workflow/definition/index', 'lucide:file-text',   1, 1, 1, 'WORKFLOW'),
(302, '发起流程',   3, 1, '/workflow/start',      '/views/workflow/start/index',      'lucide:play-circle', 2, 1, 1, 'WORKFLOW'),
(303, '我的待办',   3, 1, '/workflow/todo',       '/views/workflow/todo/index',       'lucide:clock',       3, 1, 1, 'WORKFLOW'),
(304, '我的已办',   3, 1, '/workflow/done',       '/views/workflow/done/index',       'lucide:check-circle',4, 1, 1, 'WORKFLOW'),
(305, '我发起的',   3, 1, '/workflow/initiated',  '/views/workflow/initiated/index',  'lucide:send',        5, 1, 1, 'WORKFLOW'),
(306, '流程监控',   3, 1, '/workflow/monitor',    '/views/workflow/monitor/index',    'lucide:activity',    6, 1, 1, 'WORKFLOW');

-- 公众号管理子菜单
INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort_order`, `status`, `visible`, `module`) VALUES
(401, '账号管理',   4, 1, '/wechat/account',    '/views/wechat/account/index',    'lucide:key',        1, 1, 1, 'WECHAT_OA'),
(402, '素材管理',   4, 1, '/wechat/material',   '/views/wechat/material/index',   'lucide:image',      2, 1, 1, 'WECHAT_OA'),
(403, '图文管理',   4, 1, '/wechat/article',    '/views/wechat/article/index',    'lucide:newspaper',  3, 1, 1, 'WECHAT_OA'),
(404, '粉丝管理',   4, 1, '/wechat/fan',        '/views/wechat/fan/index',        'lucide:heart',      4, 1, 1, 'WECHAT_OA'),
(405, '标签管理',   4, 1, '/wechat/tag',        '/views/wechat/tag/index',        'lucide:tag',        5, 1, 1, 'WECHAT_OA'),
(406, '自动回复',   4, 1, '/wechat/auto-reply', '/views/wechat/auto-reply/index', 'lucide:reply',      6, 1, 1, 'WECHAT_OA'),
(407, '菜单编辑',   4, 1, '/wechat/menu',       '/views/wechat/menu/index',       'lucide:menu',       7, 1, 1, 'WECHAT_OA'),
(408, '数据看板',   4, 1, '/wechat/dashboard',  '/views/wechat/dashboard/index',  'lucide:pie-chart',  8, 1, 1, 'WECHAT_OA');

-- 按钮权限（系统管理）
INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `menu_type`, `permission`, `sort_order`, `status`, `visible`, `module`) VALUES
(2011, '用户新增', 201, 2, 'system:user:create', 1, 1, 1, 'RBAC'),
(2012, '用户编辑', 201, 2, 'system:user:update', 2, 1, 1, 'RBAC'),
(2013, '用户删除', 201, 2, 'system:user:delete', 3, 1, 1, 'RBAC'),
(2021, '角色新增', 202, 2, 'system:role:create', 1, 1, 1, 'RBAC'),
(2022, '角色编辑', 202, 2, 'system:role:update', 2, 1, 1, 'RBAC'),
(2023, '角色删除', 202, 2, 'system:role:delete', 3, 1, 1, 'RBAC'),
(2031, '部门新增', 203, 2, 'system:dept:create', 1, 1, 1, 'RBAC'),
(2032, '部门编辑', 203, 2, 'system:dept:update', 2, 1, 1, 'RBAC'),
(2033, '部门删除', 203, 2, 'system:dept:delete', 3, 1, 1, 'RBAC');

-- 系统管理 - 代码生成器
INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort_order`, `status`, `visible`, `module`) VALUES
(206, '代码生成', 2, 1, '/system/generator', '/views/tool/generator/index', 'lucide:code', 6, 1, 1, 'RBAC');

-- ============================================================
-- API 访问日志表
-- ============================================================
CREATE TABLE `sys_api_access_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `tenant_id` bigint(20) DEFAULT NULL COMMENT '租户ID',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
  `username` varchar(64) DEFAULT NULL COMMENT '用户名',
  `trace_id` varchar(64) DEFAULT NULL COMMENT '链路追踪ID',
  `request_url` varchar(512) NOT NULL COMMENT '请求URL',
  `request_method` varchar(10) NOT NULL COMMENT 'HTTP方法',
  `query_string` varchar(1024) DEFAULT NULL COMMENT '查询参数',
  `ip` varchar(64) DEFAULT NULL COMMENT '请求IP',
  `user_agent` varchar(512) DEFAULT NULL COMMENT '用户代理',
  `http_status` int(11) NOT NULL COMMENT 'HTTP响应状态码',
  `duration` bigint(20) NOT NULL COMMENT '执行耗时(毫秒)',
  `request_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '请求时间',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_request_time` (`tenant_id`, `request_time`),
  KEY `idx_trace_id` (`trace_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API访问日志表';

-- ============================================================
-- API 错误日志表
-- ============================================================
CREATE TABLE `sys_api_error_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `tenant_id` bigint(20) DEFAULT NULL COMMENT '租户ID',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
  `username` varchar(64) DEFAULT NULL COMMENT '用户名',
  `trace_id` varchar(64) DEFAULT NULL COMMENT '链路追踪ID',
  `request_url` varchar(512) NOT NULL COMMENT '请求URL',
  `request_method` varchar(10) NOT NULL COMMENT 'HTTP方法',
  `query_string` varchar(1024) DEFAULT NULL COMMENT '查询参数',
  `ip` varchar(64) DEFAULT NULL COMMENT '请求IP',
  `user_agent` varchar(512) DEFAULT NULL COMMENT '用户代理',
  `exception_name` varchar(256) NOT NULL COMMENT '异常类名',
  `exception_message` varchar(1024) DEFAULT NULL COMMENT '异常信息',
  `exception_stack_trace` text DEFAULT NULL COMMENT '异常堆栈',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_create_time` (`tenant_id`, `create_time`),
  KEY `idx_trace_id` (`trace_id`),
  KEY `idx_exception_name` (`exception_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API错误日志表';
