# 业务服务手册

> 最后更新：2026-05-24

---

## Gateway 网关服务 (8080)

API 统一入口，负责认证、路由转发、租户校验。

### 核心类

| 类 | 说明 |
|----|------|
| `SaTokenGatewayConfig` | Sa-Token 网关配置：SaReactorFilter（认证拦截） + 白名单路径 |
| `AuthGlobalFilter` | 全局过滤器：剥离伪造 Header → 从 Sa-Token Session 提取用户信息 → 透传到下游 |
| `TenantSecurityFilter` | 租户安全过滤器：校验租户状态（冻结/过期） |

### 路由规则

`/api/{service}/**` → StripPrefix=2 → 对应服务

---

## Platform Service 平台服务 (8084)

平台运营端核心服务，管理租户、套餐、全局配置。**数据库：platform**

### 实体 (6)

| 实体 | 表名 | 说明 |
|------|------|------|
| `Tenant` | sys_tenant | 租户信息（名称/联系人/套餐/状态/过期时间/配额） |
| `Package` | sys_package | 套餐管理（名称/价格/用户数上限/菜单权限） |
| `PlatformUser` | sys_platform_user | 平台管理员用户 |
| `Announcement` | sys_announcement | 系统公告 |
| `GlobalConfig` | sys_global_config | 全局配置（key-value 存储） |
| `SysFile` | sys_file | 文件记录 |

### Controller (7)

| Controller | 路径前缀 | 核心接口 |
|-----------|---------|---------|
| `TenantController` | /tenant | 租户 CRUD / 启用 / 冻结 / 导出 |
| `PackageController` | /package | 套餐 CRUD |
| `PlatformUserController` | /platform-user | 平台用户管理 |
| `AnnouncementController` | /announcement | 公告 CRUD / 发布 |
| `GlobalConfigController` | /global-config | 全局配置管理 |
| `FileController` | /file | 文件上传 / 下载 / 删除 |
| `InternalController` | /internal | Feign 内部接口（租户信息查询/配额校验） |

### Service

| Service | 说明 |
|---------|------|
| `ITenantService` / `TenantServiceImpl` | 租户 CRUD + 状态管理 + 租户初始化链 |
| `IPackageService` / `PackageServiceImpl` | 套餐 CRUD |
| `IPlatformUserService` / `PlatformUserServiceImpl` | 平台用户管理 |
| `IAnnouncementService` / `AnnouncementServiceImpl` | 公告管理 |
| `IGlobalConfigService` / `GlobalConfigServiceImpl` | 全局配置管理 |
| `ISysFileService` / `SysFileServiceImpl` | 文件管理 |
| `TenantCacheService` | 租户信息缓存 |

### 定时任务

| 类 | 说明 |
|----|------|
| `TenantExpireCheckJobHandler` | 检查并处理过期租户 |

### Feign 接口 (platform-api)

`PlatformFeignClient`：租户信息查询、配额校验

### DTO/VO

| 类型 | 包含 |
|------|------|
| DTO | TenantCreateDTO, TenantQueryDTO, PackageCreateDTO, AnnouncementCreateDTO, AnnouncementQueryDTO, PlatformLoginDTO |
| VO | TenantVO, TenantExportVO, PlatformUserVO, SysFileVO |

---

## RBAC Service 权限服务 (8081)

用户/角色/部门/菜单/认证的核心服务，也承载了字典、日志、导出、三方登录、敏感词、地区、公告、岗位等功能。**数据库：rbac**

### 实体 (16)

| 实体 | 表名 | 说明 |
|------|------|------|
| `User` | sys_user | 用户（用户名/密码/昵称/头像/手机/邮箱/部门/状态） |
| `Role` | sys_role | 角色（名称/编码/级别/数据范围/状态） |
| `Dept` | sys_dept | 部门（名称/父ID/ancestors/排序/状态） |
| `Menu` | sys_menu | 菜单（名称/权限标识/类型/路由/组件/图标） |
| `UserRole` | sys_user_role | 用户-角色关联 |
| `RoleMenu` | sys_role_menu | 角色-菜单关联 |
| `RoleDept` | sys_role_dept | 角色-部门关联（数据权限） |
| `Post` | sys_post | 岗位 |
| `UserPost` | sys_user_post | 用户-岗位关联 |
| `DictType` | sys_dict_type | 字典类型 |
| `DictData` | sys_dict_data | 字典数据 |
| `LoginLog` | sys_login_log | 登录日志 |
| `PasswordHistory` | sys_password_history | 密码历史（防重复） |
| `ExportTask` | sys_export_task | 异步导出任务 |
| `SocialUser` | sys_social_user | 三方登录用户 |
| `SensitiveWord` | sys_sensitive_word | 敏感词 |
| `Area` | sys_area | 地区数据 |
| `Notice` | sys_notice | 通知公告 |
| `NoticeRead` | sys_notice_read | 公告已读记录 |

### Controller (27)

| Controller | 路径前缀 | 核心接口 |
|-----------|---------|---------|
| `AuthController` | /auth | 登录 / 注销 / 刷新Token / 注册 |
| `UserController` | /user | 用户 CRUD / 导入 / 导出 / 重置密码 |
| `RoleController` | /role | 角色 CRUD / 导出 |
| `DeptController` | /dept | 部门 CRUD / 树形查询 |
| `MenuController` | /menu | 菜单 CRUD / 树形查询 |
| `PostController` | /post | 岗位 CRUD |
| `DictTypeController` | /dict-type | 字典类型 CRUD |
| `DictDataController` | /dict-data | 字典数据 CRUD |
| `ProfileController` | /profile | 个人信息 / 修改密码 / 修改头像 |
| `CaptchaController` | /captcha | 图形验证码生成 |
| `LoginLogController` | /login-log | 登录日志查询 |
| `OnlineUserController` | /online-user | 在线用户管理 / 强制下线 |
| `OperationLogController` | /operation-log | 操作日志查询 |
| `ExportTaskController` | /export-task | 异步导出任务管理 |
| `ImportTemplateController` | /import-template | 导入模板下载 |
| `SocialLoginController` | /auth/social | 三方登录（获取授权URL / 回调 / 绑定 / 解绑） |
| `SensitiveWordController` | /sensitive-word | 敏感词 CRUD / 检测 |
| `AreaController` | /area | 地区数据查询（树形） |
| `NoticeController` | /notice | 通知公告 CRUD / 已读标记 |
| `TenantSelfController` | /tenant-self | 租户自助信息查询 |
| `CacheMonitorController` | /cache-monitor | Redis 缓存监控 |
| `ServerMonitorController` | /server-monitor | 服务器信息监控 |
| `UserRoleController` | /user-role | 用户-角色绑定 |
| `RoleMenuController` | /role-menu | 角色-菜单绑定 |
| `RoleDeptController` | /role-dept | 角色-部门绑定 |
| `PasswordHistoryController` | /password-history | 密码历史 |
| `InternalController` | /internal | Feign 内部接口 |

### 特殊模块

| 模块 | 类 | 说明 |
|------|-----|------|
| 验证码 | `CaptchaConfig`, `CaptchaRedisService` | EasyCaptcha + Redis 存储 |
| 三方登录 | `SocialLoginConfig`, `ISocialLoginService`, `SocialUser` | JustAuth 集成，支持微信/钉钉/企业微信等 |
| 敏感词 | `ISensitiveWordService`, `SensitiveWordServiceImpl` | 敏感词 CRUD + DfaFilter 检测 |
| 租户初始化 | `AdminUserInitializer`, `DeptInitializer`, `RoleInitializer`, `RoleMenuInitializer`, `UserRoleInitializer` | 5 个初始化器，新建租户时自动执行 |

### Feign 接口 (rbac-api)

`RbacFeignClient`：用户信息查询、权限校验、字典数据等

---

## Workflow Service 工作流服务 (8082)

基于 Flowable 的工作流引擎。**数据库：workflow**

### 实体 (5)

| 实体 | 表名 | 说明 |
|------|------|------|
| `WfProcessDefinitionExt` | wf_process_definition_ext | 流程定义扩展（表单配置/图标/描述） |
| `WfProcessInstanceExt` | wf_process_instance_ext | 流程实例扩展（发起人/标题/状态/表单数据） |
| `WfTaskExt` | wf_task_ext | 任务扩展（审批意见/审批结果/附件） |
| `WfCopy` | wf_copy | 抄送记录 |
| `WfNodeConfig` | wf_node_config | 节点配置（审批人设置） |

### Controller (5)

| Controller | 路径前缀 | 核心接口 |
|-----------|---------|---------|
| `WfProcessDefinitionExtController` | /process-definition | 流程定义管理 / 部署 / 挂起 / 激活 |
| `WfProcessInstanceExtController` | /process-instance | 流程实例 / 发起 / 取消 / 查询 |
| `WfTaskExtController` | /task | 任务审批 / 通过 / 拒绝 / 转办 / 委派 / 加签 |
| `WfCopyController` | /copy | 抄送记录查询 |
| `WfNodeConfigController` | /node-config | 节点配置管理 |
| `WfMonitorController` | /monitor | 流程监控（管理员） |

---

## Notify Service 通知服务 (8085)

站内消息、邮件、Webhook（钉钉/飞书/企微）、短信。**数据库：notify**

### 实体 (4)

| 实体 | 表名 | 说明 |
|------|------|------|
| `NotifyMessage` | notify_message | 通知消息（标题/内容/类型/渠道/接收人/已读状态） |
| `NotifyTemplate` | notify_template | 通知模板（模板编码/内容/变量/渠道/状态） |
| `NotifyChannelConfig` | notify_channel_config | 渠道配置（邮件SMTP/Webhook URL/租户级） |
| `SmsLog` | sys_sms_log | 短信发送日志 |

### Controller (3)

| Controller | 路径前缀 | 核心接口 |
|-----------|---------|---------|
| `NotifyMessageController` | /message | 消息查询 / 标记已读 / 未读计数 |
| `NotifyTemplateController` | /template | 模板 CRUD |
| `NotifyChannelConfigController` | /channel-config | 渠道配置 CRUD |

### 消息发送

| 类 | 说明 |
|----|------|
| `EmailSender` | 邮件发送器 |
| `WebhookSender` | Webhook 发送器 |
| `DingtalkMessageBuilder` | 钉钉消息构造器 |
| `FeishuMessageBuilder` | 飞书消息构造器 |
| `WecomMessageBuilder` | 企业微信消息构造器 |
| `ISmsService` / `SmsServiceImpl` | 短信服务 |
| `NotifyEventConsumer` | Kafka 消费者，接收通知事件并分发 |

---

## Wechat OA Service 微信公众号服务 (8083)

微信公众号管理。**数据库：wechat_oa**

### 实体 (7)

| 实体 | 表名 | 说明 |
|------|------|------|
| `WechatOaAccount` | wechat_oa_account | 公众号账号配置 |
| `WechatOaMaterial` | wechat_oa_material | 素材管理 |
| `WechatOaArticle` | wechat_oa_article | 图文消息 |
| `WechatOaFanUser` | wechat_oa_fan_user | 粉丝用户 |
| `WechatOaUserTag` | wechat_oa_user_tag | 用户标签 |
| `WechatOaAutoReplyRule` | wechat_oa_auto_reply_rule | 自动回复规则 |
| `WechatOaMenu` | wechat_oa_menu | 公众号菜单 |

### Controller (9)

| Controller | 说明 |
|-----------|------|
| `WechatOaAccountController` | 公众号账号管理 |
| `WechatOaMaterialController` | 素材管理 |
| `WechatOaArticleController` | 图文管理 |
| `WechatOaFanUserController` | 粉丝管理 / 同步 |
| `WechatOaUserTagController` | 标签管理 |
| `WechatOaAutoReplyRuleController` | 自动回复管理 |
| `WechatOaMenuController` | 菜单管理 / 发布 |
| `WechatOaDashboardController` | 数据看板 |
| `WechatOaCallbackController` | 微信回调处理 |

### 其他

| 类 | 说明 |
|----|------|
| `WechatApiClient` | 微信 API 客户端封装 |
| `RestTemplateConfig` | RestTemplate 配置 |
| `AccessTokenRefreshTask` | 定时刷新 Access Token |
