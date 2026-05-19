# 03 — 数据库设计

## 1. 数据库拆分策略

| 数据库 | 服务 | 表数 | 说明 |
|--------|------|------|------|
| `platform` | platform-service | 5 | 租户、套餐、平台用户、系统公告、全局配置 |
| `rbac` | rbac-service | 9 | 用户、角色、部门、菜单、权限关联、操作日志、密码历史 |
| `workflow` | workflow-service | 5 | 流程业务扩展表 + Flowable 引擎表（自动生成） |
| `wechat_oa` | wechat-oa-service | 7 | 公众号、素材、图文、粉丝、标签、自动回复、菜单 |
| `notify` | notify-service | 3 | 站内消息、通知模板、渠道配置 |

**共计 26 张业务表 + Flowable 引擎表（`act_*` 前缀，自动创建）**

**隔离规则：**
- `platform` 库的表 **不带** `tenant_id`（平台级）
- 其余库的业务表 **全部带** `tenant_id`（租户级）
- `sys_menu` 带可空的 `tenant_id`（平台级菜单 tenant_id 为 NULL，租户自定义菜单可填值）

---

## 2. 公共字段约定

所有业务表必须包含以下审计字段：

```sql
-- 主键
`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',

-- 创建信息
`create_user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
`create_user_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
`create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

-- 更新信息
`update_user_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
`update_user_name` varchar(64) DEFAULT NULL COMMENT '更新人姓名',
`update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

-- 逻辑删除
`delete_flag` int(11) NOT NULL DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',

-- 乐观锁
`data_version` int(11) NOT NULL DEFAULT 0 COMMENT '数据版本号',

-- 备注
`remark` varchar(512) DEFAULT NULL COMMENT '备注',
```

---

## 3. platform 库（平台级）

### 3.1 sys_tenant — 租户表

```sql
CREATE TABLE `sys_tenant` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '租户ID',
  `tenant_name` varchar(128) NOT NULL COMMENT '租户名称（企业名称）',
  `tenant_code` varchar(64) NOT NULL COMMENT '租户编码（唯一标识，自动生成）',
  `contact_person` varchar(64) DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `contact_email` varchar(128) DEFAULT NULL COMMENT '联系邮箱',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态 0-试用 1-正常 2-冻结 3-注销',
  `package_id` bigint(20) NOT NULL COMMENT '套餐ID',
  `trial_expire_time` datetime DEFAULT NULL COMMENT '试用到期时间',
  `paid_expire_time` datetime DEFAULT NULL COMMENT '付费到期时间',
  `frozen_time` datetime DEFAULT NULL COMMENT '冻结时间',
  `frozen_reason` varchar(512) DEFAULT NULL COMMENT '冻结原因',
  `admin_user_id` bigint(20) DEFAULT NULL COMMENT '租户管理员用户ID（rbac库中的用户ID）',
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
```

### 3.2 sys_package — 套餐表

```sql
CREATE TABLE `sys_package` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '套餐ID',
  `package_name` varchar(64) NOT NULL COMMENT '套餐名称',
  `package_code` varchar(32) NOT NULL COMMENT '套餐编码 FREE/BASIC/PRO/ENTERPRISE',
  `price_monthly` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '月价格（元）',
  `price_yearly` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '年价格（元）',
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
```

### 3.3 sys_platform_user — 平台用户表

```sql
CREATE TABLE `sys_platform_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(64) NOT NULL COMMENT '用户名',
  `password` varchar(128) NOT NULL COMMENT '密码（BCrypt）',
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
```

### 3.4 sys_announcement — 系统公告表

```sql
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
```

### 3.5 sys_global_config — 全局配置表

```sql
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
```

---

## 4. rbac 库（租户级）

### 4.1 sys_user — 用户表

```sql
CREATE TABLE `sys_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `username` varchar(64) NOT NULL COMMENT '用户名',
  `password` varchar(128) NOT NULL COMMENT '密码（BCrypt）',
  `real_name` varchar(64) DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(128) DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(512) DEFAULT NULL COMMENT '头像URL',
  `gender` tinyint(4) DEFAULT 0 COMMENT '性别 0-未知 1-男 2-女',
  `dept_id` bigint(20) DEFAULT NULL COMMENT '部门ID',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
  `role_level` tinyint(4) NOT NULL DEFAULT 2 COMMENT '角色等级 0-租户超管 1-部门主管 2-普通用户',
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
```

### 4.2 sys_role — 角色表

```sql
CREATE TABLE `sys_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `role_name` varchar(64) NOT NULL COMMENT '角色名称',
  `role_code` varchar(64) NOT NULL COMMENT '角色编码',
  `role_level` tinyint(4) NOT NULL DEFAULT 2 COMMENT '角色等级 0-超管(系统内置) 1-管理员 2-普通',
  `data_scope` tinyint(4) NOT NULL DEFAULT 4 COMMENT '数据范围 1-全部 2-本部门及下级 3-本部门 4-仅本人 5-自定义',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
  `is_system` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否系统内置 0-否 1-是（不可删除）',
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
```

### 4.3 sys_dept — 部门表

```sql
CREATE TABLE `sys_dept` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '部门ID',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `dept_name` varchar(128) NOT NULL COMMENT '部门名称',
  `parent_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '父部门ID 0-顶级',
  `ancestors` varchar(1024) NOT NULL DEFAULT '0' COMMENT '祖先链（逗号分隔）',
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
```

### 4.4 sys_menu — 菜单表

```sql
CREATE TABLE `sys_menu` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `tenant_id` bigint(20) DEFAULT NULL COMMENT '租户ID（平台级菜单为空）',
  `menu_name` varchar(64) NOT NULL COMMENT '菜单名称',
  `parent_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '父菜单ID 0-顶级',
  `menu_type` tinyint(4) NOT NULL COMMENT '类型 0-目录 1-菜单 2-按钮',
  `path` varchar(256) DEFAULT NULL COMMENT '路由路径',
  `component` varchar(256) DEFAULT NULL COMMENT '组件路径',
  `permission` varchar(128) DEFAULT NULL COMMENT '权限标识 如 sys:user:list',
  `icon` varchar(128) DEFAULT NULL COMMENT '图标',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
  `visible` tinyint(4) NOT NULL DEFAULT 1 COMMENT '是否可见 0-隐藏 1-显示',
  `is_external` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否外链 0-否 1-是',
  `is_cached` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否缓存 0-否 1-是',
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
```

### 4.5 关联表

```sql
-- 用户-角色关联
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

-- 角色-菜单关联（租户级：受套餐约束）
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

-- 角色-部门关联（自定义数据范围时使用）
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色部门关联表（自定义数据范围）';
```

### 4.6 sys_operation_log — 操作日志表

```sql
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
```

### 4.7 sys_password_history — 密码历史表

```sql
CREATE TABLE `sys_password_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `password` varchar(128) NOT NULL COMMENT '历史密码（BCrypt）',
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
```

---

## 5. workflow 库（租户级）

### 5.1 wf_process_definition_ext — 流程定义扩展表

```sql
CREATE TABLE `wf_process_definition_ext` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `process_definition_id` varchar(128) NOT NULL COMMENT 'Flowable流程定义ID',
  `process_key` varchar(128) NOT NULL COMMENT '流程标识',
  `process_name` varchar(256) NOT NULL COMMENT '流程名称',
  `category` varchar(64) DEFAULT NULL COMMENT '分类',
  `icon` varchar(512) DEFAULT NULL COMMENT '流程图标URL',
  `description` varchar(1024) DEFAULT NULL COMMENT '流程说明',
  `form_type` tinyint(4) NOT NULL DEFAULT 0 COMMENT '表单类型 0-外链 1-内嵌JSON表单',
  `form_url` varchar(512) DEFAULT NULL COMMENT '表单URL（外链模式）',
  `form_config` text COMMENT '表单配置（JSON模式）',
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
```

### 5.2 wf_process_instance_ext — 流程实例扩展表

```sql
CREATE TABLE `wf_process_instance_ext` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `process_instance_id` varchar(128) NOT NULL COMMENT 'Flowable流程实例ID',
  `process_definition_id` varchar(128) NOT NULL COMMENT 'Flowable流程定义ID',
  `process_key` varchar(128) DEFAULT NULL COMMENT '流程标识',
  `process_name` varchar(256) DEFAULT NULL COMMENT '流程名称',
  `title` varchar(256) DEFAULT NULL COMMENT '流程标题（申请人填写）',
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
```

### 5.3 wf_task_ext — 任务扩展表

```sql
CREATE TABLE `wf_task_ext` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `task_id` varchar(128) NOT NULL COMMENT 'Flowable任务ID',
  `process_instance_id` varchar(128) NOT NULL COMMENT '流程实例ID',
  `task_name` varchar(256) DEFAULT NULL COMMENT '任务名称',
  `assignee_id` bigint(20) DEFAULT NULL COMMENT '当前处理人ID',
  `assignee_name` varchar(64) DEFAULT NULL COMMENT '当前处理人姓名',
  `owner_id` bigint(20) DEFAULT NULL COMMENT '任务所有人ID（委派场景）',
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
```

### 5.4 wf_copy — 抄送表

```sql
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
```

### 5.5 wf_node_config — 节点审批人配置表

```sql
CREATE TABLE `wf_node_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `process_definition_id` varchar(128) NOT NULL COMMENT '流程定义ID',
  `node_id` varchar(128) NOT NULL COMMENT 'BPMN节点ID',
  `node_name` varchar(256) DEFAULT NULL COMMENT '节点名称',
  `assignee_type` tinyint(4) NOT NULL COMMENT '审批人类型 1-指定用户 2-指定角色 3-部门负责人 4-发起人自选',
  `assignee_ids` varchar(1024) DEFAULT NULL COMMENT '审批人/角色ID列表(JSON数组)',
  `approval_mode` tinyint(4) NOT NULL DEFAULT 1 COMMENT '审批模式 1-或签(任一) 2-会签(全部) 3-依次',
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
```

**说明：** Flowable 引擎自身的表（`act_*` 前缀）由 Flowable 自动创建和管理，此处不列出。需要注意 Flowable 引擎表默认不支持多租户行级隔离，通过 Flowable 的 `TenantId` 字段来实现租户隔离。

---

## 6. wechat_oa 库（租户级）

### 6.1 wechat_oa_account — 公众号账号表

```sql
CREATE TABLE `wechat_oa_account` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `account_name` varchar(128) NOT NULL COMMENT '公众号名称',
  `app_id` varchar(64) NOT NULL COMMENT '微信AppID',
  `app_secret` varchar(128) NOT NULL COMMENT '微信AppSecret（加密存储）',
  `token` varchar(128) DEFAULT NULL COMMENT '微信Token（回调验证）',
  `aes_key` varchar(128) DEFAULT NULL COMMENT '消息加密密钥',
  `account_type` tinyint(4) NOT NULL DEFAULT 0 COMMENT '类型 0-订阅号 1-服务号',
  `is_verified` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否认证 0-否 1-是',
  `qr_code_url` varchar(512) DEFAULT NULL COMMENT '公众号二维码URL',
  `access_token` varchar(512) DEFAULT NULL COMMENT '当前AccessToken（加密存储）',
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
```

### 6.2 wechat_oa_material — 素材表

```sql
CREATE TABLE `wechat_oa_material` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `account_id` bigint(20) NOT NULL COMMENT '公众号ID',
  `media_id` varchar(128) DEFAULT NULL COMMENT '微信素材MediaID',
  `material_type` tinyint(4) NOT NULL COMMENT '类型 0-图片 1-语音 2-视频 3-缩略图',
  `title` varchar(256) DEFAULT NULL COMMENT '素材标题',
  `file_name` varchar(256) DEFAULT NULL COMMENT '原始文件名',
  `file_url` varchar(512) DEFAULT NULL COMMENT '本地存储URL（MinIO）',
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
```

### 6.3 wechat_oa_article — 图文表

```sql
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
```

### 6.4 wechat_oa_fan_user — 粉丝表

```sql
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
  `tag_ids` varchar(512) DEFAULT NULL COMMENT '标签ID列表(JSON数组)',
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
```

### 6.5 wechat_oa_user_tag — 粉丝标签表

```sql
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
```

### 6.6 wechat_oa_auto_reply_rule — 自动回复规则表

```sql
CREATE TABLE `wechat_oa_auto_reply_rule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `account_id` bigint(20) NOT NULL COMMENT '公众号ID',
  `rule_name` varchar(128) NOT NULL COMMENT '规则名称',
  `rule_type` tinyint(4) NOT NULL COMMENT '类型 0-关注回复 1-关键词回复 2-默认回复',
  `keyword` varchar(256) DEFAULT NULL COMMENT '关键词（关键词回复时使用）',
  `match_type` tinyint(4) DEFAULT NULL COMMENT '匹配方式 0-全匹配 1-半匹配',
  `reply_type` tinyint(4) NOT NULL COMMENT '回复类型 0-文本 1-图片 2-图文',
  `reply_content` text COMMENT '回复内容（文本或JSON）',
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
```

### 6.7 wechat_oa_menu — 公众号菜单表

```sql
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
```

---

## 7. notify 库（租户级）

### 7.1 notify_message — 站内消息表

```sql
CREATE TABLE `notify_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `receiver_id` bigint(20) NOT NULL COMMENT '接收人ID',
  `sender_id` bigint(20) DEFAULT NULL COMMENT '发送人ID（系统通知为空）',
  `sender_name` varchar(64) DEFAULT NULL COMMENT '发送人姓名',
  `title` varchar(256) NOT NULL COMMENT '消息标题',
  `content` text COMMENT '消息内容',
  `type` tinyint(4) NOT NULL COMMENT '类型 0-系统通知 1-审批通知 2-催办 3-公告',
  `biz_type` varchar(64) DEFAULT NULL COMMENT '业务类型（如 workflow/wechat-oa）',
  `biz_id` varchar(128) DEFAULT NULL COMMENT '业务ID（如流程实例ID）',
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
```

### 7.2 notify_template — 通知模板表

```sql
CREATE TABLE `notify_template` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `template_code` varchar(64) NOT NULL COMMENT '模板编码',
  `template_name` varchar(128) NOT NULL COMMENT '模板名称',
  `type` tinyint(4) NOT NULL COMMENT '渠道 0-站内信 1-邮件 2-IM Webhook',
  `title_template` varchar(256) DEFAULT NULL COMMENT '标题模板（支持变量占位）',
  `content_template` text COMMENT '内容模板（支持变量占位）',
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
```

### 7.3 notify_channel_config — 租户通知渠道配置表

```sql
CREATE TABLE `notify_channel_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `channel_type` tinyint(4) NOT NULL COMMENT '渠道 0-站内信 1-邮件 2-飞书 3-钉钉 4-企业微信',
  `enabled` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否启用 0-否 1-是',
  `config_json` text COMMENT '渠道配置(JSON)，如SMTP信息、Webhook地址等',
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
```

---

## 8. 索引策略总结

### 8.1 租户隔离索引规则

所有带 `tenant_id` 的表，查询条件中 **必须以 `tenant_id` 开头**，索引设计遵循：

```
规则: 联合索引的第一列必须是 tenant_id
示例:
  OK   KEY idx_tenant_status (tenant_id, status)       -- 先租户后状态
  OK   KEY idx_tenant_user   (tenant_id, user_id)       -- 先租户后用户
  BAD  KEY idx_status        (status)                    -- 缺少租户前缀
  BAD  KEY idx_user_tenant   (user_id, tenant_id)        -- 顺序反了
```

### 8.2 唯一约束考量

多租户下的唯一约束必须带 `tenant_id`：

```
OK   UNIQUE KEY uk_tenant_username (tenant_id, username, delete_flag)
     -- 同一租户内用户名不可重复
     -- 不同租户可以有相同用户名

BAD  UNIQUE KEY uk_username (username)
     -- 所有租户共享一个用户名空间，B 公司不能用 A 公司已占用的用户名
```

### 8.3 大表分区策略（未来）

当单表超过 1000 万行时，考虑按 `tenant_id` 做 Range/Hash 分区：

```sql
-- 示例：操作日志表按月+租户分区（未来需要时启用）
ALTER TABLE sys_operation_log PARTITION BY RANGE (YEAR(create_time) * 100 + MONTH(create_time)) (
    PARTITION p202601 VALUES LESS THAN (202602),
    PARTITION p202602 VALUES LESS THAN (202603),
    ...
);
```

---

## 9. 种子数据（init.sql）

完整初始化脚本位于 `docs/sql/init.sql`，包含 5 个数据库、26 张表定义及以下种子数据。

### 9.1 默认账号

| 身份 | 所在库/表 | 用户名 | 密码 | 说明 |
|------|-----------|--------|------|------|
| 平台管理员 | platform.sys_platform_user | admin | Admin@2026 | role_type=0 超级管理员 |
| 租户管理员 | rbac.sys_user (tenant_id=1) | admin | Admin@2026 | role_level=0 租户超管 |

密码使用 BCrypt 加密存储。

### 9.2 套餐

| ID | 名称 | 编码 | 月价 | 年价 | 用户上限 | 角色上限 | 部门上限 | 流程上限 | 公众号上限 | 存储(MB) |
|----|------|------|------|------|----------|----------|----------|----------|-----------|----------|
| 1 | 免费版 | FREE | 0 | 0 | 10 | 5 | 10 | 5 | 1 | 1024 |
| 2 | 基础版 | BASIC | 299 | 2990 | 50 | 20 | 50 | 20 | 3 | 10240 |
| 3 | 专业版 | PRO | 999 | 9990 | 200 | 50 | 200 | 不限 | 10 | 102400 |
| 4 | 旗舰版 | ENTERPRISE | 0 | 0 | 不限 | 不限 | 不限 | 不限 | 不限 | 不限 |

### 9.3 默认租户

| ID | 名称 | 编码 | 套餐 | 状态 |
|----|------|------|------|------|
| 1 | 默认租户 | DEFAULT | 旗舰版(4) | 正常(1) |

### 9.4 部门树

```
总部 (id=1, ancestors=0)
  ├── 技术部     (id=2, ancestors=0,1)
  ├── 产品部     (id=3, ancestors=0,1)
  ├── 运营部     (id=4, ancestors=0,1)
  └── 人事行政部 (id=5, ancestors=0,1)
```

### 9.5 角色与权限

| ID | 名称 | 编码 | 等级 | 数据范围 | 系统内置 | 权限范围 |
|----|------|------|------|----------|----------|----------|
| 1 | 超级管理员 | SUPER_ADMIN | 0-超管 | 全部(1) | 是 | 所有菜单和按钮（87项） |
| 2 | 管理员 | ADMIN | 1-管理员 | 全部(1) | 是 | 系统管理 + 流程查看 + 通知管理（无公众号、无流程监控操作） |
| 3 | 普通用户 | USER | 2-普通 | 仅本人(4) | 是 | 仪表盘 + 个人设置 + 流程使用 + 消息查看 |

### 9.6 菜单树（87 项：5 目录 + 24 页面 + 58 按钮）

```
[1] 仪表盘 (RBAC)
  ├── [101] 分析页
  └── [102] 工作台

[2] 系统管理 (RBAC)
  ├── [201] 用户管理 → 按钮: 查询/新增/编辑/删除/重置密码/导出 (2011-2016)
  ├── [202] 角色管理 → 按钮: 查询/新增/编辑/删除/分配权限 (2021-2025)
  ├── [203] 部门管理 → 按钮: 查询/新增/编辑/删除 (2031-2034)
  ├── [204] 个人设置
  └── [205] 操作日志 → 按钮: 查询/导出/删除 (2051-2053)

[3] 流程管理 (WORKFLOW)
  ├── [301] 流程定义 → 按钮: 查询/新增/编辑/删除/部署 (3011-3015)
  ├── [302] 发起流程
  ├── [303] 我的待办
  ├── [304] 我的已办
  ├── [305] 我发起的
  └── [306] 流程监控 → 按钮: 查询/终止 (3061-3062)

[4] 公众号管理 (WECHAT_OA)
  ├── [401] 账号管理   → 按钮: 查询/新增/编辑/删除 (4011-4014)
  ├── [402] 素材管理   → 按钮: 查询/上传/删除 (4021-4023)
  ├── [403] 图文管理   → 按钮: 查询/新增/编辑/删除/发布 (4031-4035)
  ├── [404] 粉丝管理   → 按钮: 查询/拉黑/同步 (4041-4043)
  ├── [405] 标签管理   → 按钮: 查询/新增/删除 (4051-4053)
  ├── [406] 自动回复   → 按钮: 查询/新增/编辑/删除 (4061-4064)
  ├── [407] 菜单编辑   → 按钮: 查询/编辑/同步 (4071-4073)
  └── [408] 数据看板

[5] 通知管理 (RBAC)
  ├── [501] 站内消息   → 按钮: 查询/删除 (5011-5012)
  ├── [502] 通知模板   → 按钮: 查询/新增/编辑/删除 (5021-5024)
  └── [503] 渠道配置   → 按钮: 查询/编辑 (5031-5032)
```

### 9.7 全局配置

| 配置键 | 值 | 类型 | 说明 |
|--------|-----|------|------|
| default_package_id | 1 | NUMBER | 默认套餐ID |
| trial_days | 15 | NUMBER | 试用天数 |
| max_login_attempts | 5 | NUMBER | 最大登录尝试次数 |
| password_min_length | 8 | NUMBER | 密码最小长度 |
| login_lock_minutes | 30 | NUMBER | 登录锁定时长(分钟) |
