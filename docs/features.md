# 功能清单

> 最后更新：2026-05-24
> 
> 此文档记录项目已具备的所有框架级能力，避免重复建设。

## 安全与认证

| 功能 | 实现方式 | 位置 |
|------|---------|------|
| 用户认证 | Sa-Token（Gateway SaReactorFilter + 各服务 StpUtil） | gateway + common-data |
| 功能权限 | `@RequirePermission` 注解 + AOP | common-data |
| 数据权限 | `@DataScope` 注解 + MyBatis-Plus SQL 拦截器，基于部门 ancestors | common-data |
| 内部调用认证 | `@InnerApi` + HMAC 签名验证 | common-data |
| XSS 防护 | XssFilter + XssStringJsonDeserializer | common-core |
| 验证码 | EasyCaptcha + Redis 存储 | rbac-service |
| 三方登录 | JustAuth 集成（微信/钉钉/企业微信等） | rbac-service |
| 密码安全 | 密码历史记录，防重复使用 | rbac-service |
| 数据脱敏 | `@MobileDesensitize` / `@IdCardDesensitize` / `@EmailDesensitize` / `@BankCardDesensitize` | common-core |

## 多租户

| 功能 | 实现方式 | 位置 |
|------|---------|------|
| 行级隔离 | MyBatis-Plus TenantLineInnerInterceptor | common-data |
| 上下文传播（HTTP） | Gateway Header → TenantContextFilter → TenantContext(TTL) | common-data |
| 上下文传播（Feign） | TenantFeignInterceptor | common-data |
| 上下文传播（Kafka） | TenantKafkaProducerInterceptor / ConsumerInterceptor | common-log |
| 上下文传播（Redis） | TenantRedisCacheManager（Cache Name 加租户后缀） | common-redis |
| 跳过租户过滤 | `@TenantIgnore` 注解 / `TenantContext.executeWithoutTenant()` | common-data |
| 定时任务租户遍历 | `@TenantJob` + TenantJobAspect | common-data |
| 租户初始化链 | TenantInitializerRegistry + 5 个初始化器 | rbac-service |
| 租户状态校验 | TenantSecurityFilter（冻结/过期检查） | gateway |

## 日志与可观测

| 功能 | 实现方式 | 位置 |
|------|---------|------|
| 操作日志 | `@OperationLog` 注解 + AOP + Kafka 异步，支持 OperateType 枚举 + `recordDiff` 字段变更追踪 | common-log |
| 操作日志 Diff | `@DiffField` 标注字段中文名 + `DiffUtil` 对比 + `OperationLogContext` ThreadLocal 传递 | common-log |
| 登录日志 | LoginLog 实体，记录登录/登出/失败 | rbac-service |
| API 访问日志 | ApiAccessLogFilter + Kafka 异步 + `@ApiAccessLog` 接口粒度控制（跳过/脱敏） | common-log |
| API 错误日志 | ApiErrorLogFilter + Kafka 异步 | common-log |
| 链路追踪 | Micrometer Tracing (Brave) + Zipkin 采集，TraceResponseFilter 写入响应 Header | common-log |
| API 文档 | Knife4j 4.5（OpenAPI 3）各服务独立文档 + Gateway 聚合 | 各 service + gateway |
| 服务监控 | Spring Boot Admin 3.4.5（各服务集成 admin-client） | 各 service |
| 指标暴露 | Micrometer + Prometheus Registry + Actuator | common-log + 各 service |

## 限流与保护

| 功能 | 实现方式 | 位置 |
|------|---------|------|
| 流控/熔断 | **Sentinel**（SentinelAutoConfiguration + SentinelBlockExceptionHandler） | common-core |
| Sentinel 规则初始化 | 各服务 SentinelRuleInitializer | 各 service |
| 幂等防重 | `@Idempotent` 注解 + Redis SETNX + SpEL key | common-redis |
| 分布式锁 | `@DistributedLock` 注解 + Redisson | common-redis |

## 消息与通知

| 功能 | 实现方式 | 位置 |
|------|---------|------|
| Kafka 封装 | KafkaProducerService + KafkaConfig（Topic 管理） | common-log |
| 站内消息 | NotifyMessage + 模板系统 | notify-service |
| 邮件发送 | EmailSender | notify-service |
| Webhook（钉钉/飞书/企微） | WebhookSender + 各平台 MessageBuilder | notify-service |
| 短信 | ISmsService + SmsLog + sms4j 多平台（SmsChannelEnum 指定通道） | notify-service |
| WebSocket | common-websocket 框架（SessionManager + MessageSender + Token 认证 + 可选 Kafka 集群广播） | common-websocket |

## 数据与存储

| 功能 | 实现方式 | 位置 |
|------|---------|------|
| ORM | MyBatis-Plus 3.5.16 | common-data |
| 审计字段自动填充 | AuditFieldHandler（MetaObjectHandler） | common-data |
| 逻辑删除 | deleteFlag 字段，MyBatis-Plus 自动处理 | common-data |
| 乐观锁 | dataVersion 字段 + @Version | common-data |
| 文件存储 | MinioService（上传/下载/删除/预签名 URL） | common-storage |
| 文件记录 | SysFile 实体 + FileController | platform-service |
| IP 地区解析 | IpRegionUtils（ip2region.xdb） | common-core |
| 敏感词过滤 | DfaFilter（Trie 算法）+ 敏感词管理 CRUD | common-core + rbac-service |
| 二级缓存 | TwoLevelCacheManager（Caffeine 本地 + Redis 远程 + Pub/Sub 多实例同步失效） | common-redis |

## Excel

| 功能 | 实现方式 | 位置 |
|------|---------|------|
| Excel 导入导出 | ExcelUtils（FastExcel 封装） | common-excel |
| 字典转换 | `@DictFormat` + DictFormatConverter + DictDataProvider SPI | common-excel |
| 异步导出 | ExportTask 实体 + ExportTaskController | rbac-service |
| 导入模板 | ImportTemplateController | rbac-service |

## 工作流

| 功能 | 实现方式 | 位置 |
|------|---------|------|
| 流程引擎 | Flowable | workflow-service |
| 流程定义管理 | 部署/挂起/激活 + 扩展表 | workflow-service |
| 流程实例 | 发起/取消/查询 + 扩展表（表单数据） | workflow-service |
| 任务审批 | 通过/拒绝/转办/委派/加签 | workflow-service |
| 抄送 | WfCopy 记录 | workflow-service |
| 节点配置 | 审批人策略配置 | workflow-service |

## 微信公众号

| 功能 | 实现方式 | 位置 |
|------|---------|------|
| 账号管理 | 多公众号配置 | wechat-oa-service |
| 素材管理 | 图片/语音/视频/图文素材 | wechat-oa-service |
| 粉丝管理 | 同步/查询/标签 | wechat-oa-service |
| 自动回复 | 关注/关键词/默认回复规则 | wechat-oa-service |
| 菜单管理 | 自定义菜单 + 发布 | wechat-oa-service |
| 消息回调 | 微信回调处理 | wechat-oa-service |
| Token 刷新 | 定时任务自动刷新 | wechat-oa-service |

## 系统管理

| 功能 | 实现方式 | 位置 |
|------|---------|------|
| 字典管理 | DictType + DictData CRUD | rbac-service |
| 地区数据 | Area 实体 + 树形查询 | rbac-service |
| 在线用户 | 查询 / 强制下线 | rbac-service |
| 缓存监控 | Redis 缓存信息查询 | rbac-service |
| 服务器监控 | JVM / OS / 磁盘信息 | rbac-service |
| 通知公告 | Notice + 已读记录 | rbac-service |

## 定时任务

| 功能 | 实现方式 | 位置 |
|------|---------|------|
| 任务调度平台 | XXL-Job（XxlJobAutoConfiguration + XxlJobProperties） | common-core |
| 租户遍历执行 | `@TenantJob` + TenantJobAspect，自动遍历所有租户执行 | common-data |

**已注册的 Job：**

| 任务 | 服务 | 说明 |
|------|------|------|
| `CleanExpiredTokenJobHandler` | rbac-service | 清理过期 Token |
| `CleanExpiredExportTaskJobHandler` | rbac-service | 清理过期导出任务 |
| `TenantExpireCheckJobHandler` | platform-service | 检查过期租户 |
| `CleanReadMessageJobHandler` | notify-service | 清理已读消息 |
| `AccessTokenRefreshTask` | wechat-oa-service | 刷新微信 Access Token |

## 代码生成器

| 功能 | 说明 |
|------|------|
| 模板 | Velocity 模板，生成 Controller/Service/ServiceImpl/Mapper/Entity/DTO/VO |
| 位置 | `code-generator/src/main/resources/templates/` |
| 包含 | controller.java.vm, service.java.vm, serviceImpl.java.vm, mapper.java.vm, entity.java.vm, createDTO.java.vm, updateDTO.java.vm, queryDTO.java.vm, vo.java.vm, converter.java.vm |
