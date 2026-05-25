# 数据库手册

> 最后更新：2026-05-24

## 数据库列表

| 数据库 | 服务 | 说明 |
|--------|------|------|
| platform | platform-service | 租户、套餐、公告、全局配置 |
| rbac | rbac-service | 用户权限、字典、日志、导出任务 |
| wechat_oa | wechat-oa-service | 微信公众号 |
| notify | notify-service | 通知消息、模板、渠道配置 |
| workflow | workflow-service | 工作流扩展表（Flowable 自身表在同库） |

DDL 脚本位置：`docs/sql/init.sql`（全量）和各库独立的 `docs/sql/{db}.sql`

## platform 库

### sys_tenant — 租户信息
租户核心表，存储租户基本信息、套餐关联、状态控制。
- 关键字段：name, contact_name, contact_phone, package_id, status, expire_time
- 平台级表，不含 tenant_id

### sys_package — 套餐管理
- 关键字段：name, price, user_quota, menu_ids(JSON)
- 平台级表

### sys_platform_user — 平台管理员
- 平台运营端用户（非租户用户）
- 平台级表

### sys_announcement — 系统公告
- 关键字段：title, content, type, status, publish_time
- 平台级表

### sys_global_config — 全局配置
- key-value 存储，如系统参数、开关配置
- 平台级表

### sys_tenant_order — 租户订单
- 套餐购买/续费记录

### sys_api_client — API 客户端
- 外部 API 调用客户端凭证管理

### sys_file — 文件记录
- 上传文件元信息，关联 MinIO 存储路径

## rbac 库

### sys_user — 用户
- 关键字段：username, password, nickname, avatar, phone, email, dept_id, status
- 含 tenant_id（租户隔离）

### sys_role — 角色
- 关键字段：name, code, level, data_scope, status
- data_scope 对应 DataScopeEnum
- 含 tenant_id

### sys_dept — 部门
- 关键字段：name, parent_id, ancestors, sort, status
- ancestors 字段存储完整祖先链（如 "0,1,2"），用于数据权限层级查询
- 含 tenant_id

### sys_menu — 菜单
- 关键字段：name, permission, type(目录/菜单/按钮), parent_id, path, component, icon
- **平台级表，不含 tenant_id**

### sys_user_role — 用户-角色关联
- user_id, role_id, tenant_id

### sys_role_menu — 角色-菜单关联
- role_id, menu_id, tenant_id

### sys_role_dept — 角色-部门关联（数据权限）
- role_id, dept_id, tenant_id

### sys_dict_type — 字典类型
- 关键字段：name, type(编码), status
- 含 tenant_id

### sys_dict_data — 字典数据
- 关键字段：dict_type, label, value, sort, status
- 含 tenant_id

### sys_operation_log — 操作日志
- 关键字段：user_id, module, operation, operate_type, method, request_url, request_method, request_params, change_diff, response_code, error_msg, ip, location, user_agent, duration

### sys_login_log — 登录日志
- 关键字段：user_id, username, login_type, ip, user_agent, status, message

### sys_password_history — 密码历史
- 防止用户重复使用旧密码

### sys_post — 岗位
- 关键字段：name, code, sort, status
- 含 tenant_id

### sys_user_post — 用户-岗位关联
- user_id, post_id

### sys_export_task — 异步导出任务
- 关键字段：name, status, file_url, error_message, progress

### sys_social_user — 三方登录用户
- 关键字段：social_type, social_id, user_id, union_id, nickname, avatar

### sys_sensitive_word — 敏感词
- 关键字段：word, tag, status

### sys_area — 地区数据
- 关键字段：name, parent_id, level
- **平台级表**

### sys_notice — 通知公告
- 关键字段：title, content, type, status

### sys_notice_read — 公告已读记录
- notice_id, user_id

### sys_api_access_log — API 访问日志
- 关键字段：trace_id, user_id, request_url, request_method, http_status, duration, ip
- 索引：tenant_id+create_time, trace_id, user_id

### sys_api_error_log — API 错误日志
- 关键字段：trace_id, user_id, request_url, exception_name, exception_message, exception_stack_trace
- 索引：tenant_id+create_time, trace_id, exception_name

### sys_sms_log — 短信发送日志
- 关键字段：phone, content, status, channel

## wechat_oa 库

### wechat_oa_account — 公众号账号
- 关键字段：name, app_id, app_secret, token, aes_key, account_type

### wechat_oa_material — 素材
- 关键字段：name, media_id, type, url

### wechat_oa_article — 图文
- 关键字段：title, author, content, cover_url, source_url, status

### wechat_oa_fan_user — 粉丝
- 关键字段：openid, nickname, avatar, sex, city, subscribe_time

### wechat_oa_user_tag — 用户标签
- 关键字段：tag_id, name

### wechat_oa_auto_reply_rule — 自动回复规则
- 关键字段：name, type(关注/关键词/默认), match_type, keyword, reply_content

### wechat_oa_menu — 公众号菜单
- 关键字段：name, type, key, url, media_id, parent_id

## notify 库

### notify_message — 通知消息
- 关键字段：title, content, type, channel, sender_id, receiver_id, read_status

### notify_template — 通知模板
- 关键字段：code, name, content, params, channel, status
- **平台级表**

### notify_channel_config — 渠道配置
- 关键字段：channel_type, config_json (邮件SMTP/Webhook URL 等)

## workflow 库

### wf_process_definition_ext — 流程定义扩展
- 关键字段：process_definition_id, form_config, icon, description, category

### wf_process_instance_ext — 流程实例扩展
- 关键字段：process_instance_id, starter_user_id, title, status, form_data, result

### wf_task_ext — 任务扩展
- 关键字段：task_id, process_instance_id, assignee_user_id, comment, result, attachments

### wf_copy — 抄送记录
- 关键字段：process_instance_id, task_id, user_id, task_name

### wf_node_config — 节点配置
- 关键字段：process_definition_id, node_id, node_name, assignee_strategy, config_json

## 约定

### 实体基类字段（所有业务表）
- `id` bigint PK AUTO_INCREMENT
- `create_user_id`, `create_user_name`, `create_time`
- `update_user_id`, `update_user_name`, `update_time`
- `delete_flag` int DEFAULT 0（逻辑删除）
- `data_version` int DEFAULT 0（乐观锁）
- `remark` varchar(255)

### 租户隔离
- 含 `tenant_id` 的表：自动追加 `WHERE tenant_id = ?`
- 平台级表（无 tenant_id）：配置在 `TenantProperties.ignoreTables`

### MyBatis-Plus 配置
- 逻辑删除字段：`deleteFlag`（0=未删除, 1=已删除）
- 下划线转驼峰：自动映射
- 乐观锁：`@Version` 注解在 `dataVersion` 字段
