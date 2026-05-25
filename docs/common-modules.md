# 公共模块手册

> 最后更新：2026-05-24

## 模块依赖关系

```
common-core (最底层，无项目内依赖)
  ↑
common-data (依赖 common-core)
  ↑
common-redis (依赖 common-core)
common-log (依赖 common-core, spring-kafka)
common-excel (依赖 common-core)
common-storage (依赖 common-core)
common-websocket (依赖 common-core, optional: common-data, spring-kafka)
```

---

## common-core

基础公共模块，所有其他模块和服务都依赖它。

### 响应体系

| 类 | 说明 |
|----|------|
| `ApiResult<T>` | 统一 API 响应封装，包含 code/message/data |
| `PageResult<T>` | 分页响应封装 |
| `ResultCode` | 响应状态码枚举（200/400/401/403/404/409/429/500 + 业务码） |

### 异常体系

| 类 | 说明 |
|----|------|
| `BusinessException` | 业务异常，携带 ResultCode |
| `ForbiddenException` | 权限不足异常 |
| `GlobalExceptionHandler` | 全局异常处理器（@RestControllerAdvice） |

### 常量

| 类 | 说明 |
|----|------|
| `SecurityConstants` | 安全相关 Header 名称（X-Tenant-Id, X-User-Id, X-Username, X-Permissions 等） |

### 枚举

| 类 | 说明 |
|----|------|
| `TenantStatusEnum` | 租户状态枚举 |
| `DataScopeEnum` | 数据权限范围枚举（ALL / DEPT_AND_BELOW / DEPT_ONLY / SELF） |

### XSS 防护

| 类 | 说明 |
|----|------|
| `XssFilter` | Servlet Filter，拦截请求进行 XSS 过滤 |
| `XssHttpServletRequestWrapper` | 请求包装器，对参数值进行 HTML 转义 |
| `XssStringJsonDeserializer` | Jackson 反序列化器，对 JSON 字符串值进行 XSS 过滤 |
| `XssUtil` | XSS 清理工具类 |
| `XssAutoConfiguration` | XSS 自动配置 |

### 数据脱敏

| 类 | 说明 |
|----|------|
| `@DesensitizeBy` | 元注解，标注在具体脱敏注解上，指定 Handler |
| `DesensitizeHandler<T>` | 脱敏处理器接口 |
| `DesensitizeSerializer` | Jackson ContextualSerializer，自动识别脱敏注解并调用 Handler |
| `@MobileDesensitize` | 手机号脱敏（138****1234） |
| `@IdCardDesensitize` | 身份证脱敏（110101********1234） |
| `@EmailDesensitize` | 邮箱脱敏（te****@gmail.com） |
| `@BankCardDesensitize` | 银行卡脱敏（6222****1234） |
| 对应 Handler 实现 | `MobileDesensitizeHandler` / `IdCardDesensitizeHandler` / `EmailDesensitizeHandler` / `BankCardDesensitizeHandler` |

**使用方式**：在 VO 字段上标注 `@MobileDesensitize`，Jackson 序列化时自动脱敏。

### 租户初始化

| 类 | 说明 |
|----|------|
| `TenantInitializer` | 租户初始化器接口，新建租户时自动调用 |
| `TenantInitializerRegistry` | 初始化器注册中心，收集所有实现并按顺序执行 |
| `TenantInitContext` | 初始化上下文（携带租户信息） |

### Sentinel 限流熔断

| 类 | 说明 |
|----|------|
| `SentinelAutoConfiguration` | Sentinel 自动配置 |
| `SentinelBlockExceptionHandler` | Sentinel 限流异常统一处理，返回 429 状态码 |

### XxlJob 定时任务

| 类 | 说明 |
|----|------|
| `XxlJobAutoConfiguration` | XxlJob 自动配置 |
| `XxlJobProperties` | XxlJob 配置属性 |

### 工具类

| 类 | 说明 |
|----|------|
| `DfaFilter` | DFA 敏感词过滤器（Trie 算法） |
| `IpRegionUtils` | IP 地区解析工具（基于 ip2region.xdb） |

### 配置

| 类 | 说明 |
|----|------|
| `JacksonConfig` | Jackson 全局配置（日期格式、Long 序列化等） |

---

## common-data

数据层公共模块，包含 MyBatis-Plus 配置、多租户、数据权限、安全注解。

### 实体基类

| 类 | 说明 |
|----|------|
| `BaseEntity` | 实体基类：id(AUTO), createUserId, createUserName, createTime, updateUserId, updateUserName, updateTime, deleteFlag(逻辑删除), dataVersion(乐观锁), remark |
| `TenantBaseEntity` | 租户实体基类：extends BaseEntity + tenantId |

### MyBatis-Plus 配置

| 类 | 说明 |
|----|------|
| `MybatisPlusConfig` | 核心配置：分页插件 + 租户拦截器 + 数据权限拦截器 + 乐观锁插件 |
| `AuditFieldHandler` | MetaObjectHandler 实现，自动填充审计字段（createUserId/Time, updateUserId/Time, tenantId） |

### 多租户

| 类 | 说明 |
|----|------|
| `TenantProperties` | 配置属性（`saas.tenant` 前缀）：enable, column, ignoreTables |
| `TenantLineHandlerImpl` | MyBatis-Plus TenantLineHandler 实现，提供 tenantId 值和忽略表判断 |
| `@TenantIgnore` | 注解，标注方法跳过租户过滤 |
| `TenantIgnoreAspect` | AOP 切面，处理 @TenantIgnore |
| `@TenantJob` | 注解，标注定时任务方法，自动遍历所有租户执行 |
| `TenantJobAspect` | AOP 切面，处理 @TenantJob |
| `TenantFrameworkService` | 接口，提供获取所有租户 ID 列表的能力 |

### 数据权限

| 类 | 说明 |
|----|------|
| `@DataScope` | 注解，声明需要数据权限过滤（指定部门表别名和用户表别名） |
| `DataScopeAspect` | AOP 切面，将数据范围信息写入 DataScopeContextHolder |
| `DataScopeSqlInterceptor` | MyBatis-Plus 拦截器，根据数据范围追加 SQL WHERE 条件 |
| `DataScopeContextHolder` | ThreadLocal 持有数据范围上下文 |

### 安全注解

| 类 | 说明 |
|----|------|
| `@RequirePermission` | 权限校验注解，值为权限标识（如 `sys:user:add`） |
| `PermissionAspect` | AOP 切面，校验当前用户是否拥有指定权限 |
| `@InnerApi` | 内部 API 注解，标注 Feign 调用入口 |
| `InnerApiAspect` | AOP 切面，校验内部调用签名（X-Internal-Source + X-Internal-Signature） |

### 安全上下文

| 类 | 说明 |
|----|------|
| `TenantContext` | 租户上下文（TransmittableThreadLocal），存储当前 tenantId |
| `UserContext` | 用户上下文，存储当前用户 ID / 用户名 / 权限 / 部门 / 数据范围 |
| `TenantContextFilter` | Servlet Filter，从 Header 提取 tenantId 写入 TenantContext |

### Sa-Token 集成

| 类 | 说明 |
|----|------|
| `SaTokenAutoConfiguration` | Sa-Token 自动配置 |
| `StpInterfaceImpl` | Sa-Token StpInterface 实现，提供用户权限和角色列表 |

### Feign 拦截器

| 类 | 说明 |
|----|------|
| `TenantFeignInterceptor` | Feign RequestInterceptor，自动传播 X-Tenant-Id + 内部调用签名 |

---

## common-redis

Redis 操作和基于 Redis 的分布式能力。

| 类 | 说明 |
|----|------|
| `RedisConfig` | RedisTemplate + CacheManager 配置 |
| `@Idempotent` | 幂等注解：防重复提交，支持 SpEL 自定义 key |
| `IdempotentAspect` | 幂等 AOP 切面，基于 Redis SETNX 实现 |
| `@DistributedLock` | 分布式锁注解：基于 Redisson，支持 SpEL key + 等待/释放时间 |
| `DistributedLockAspect` | 分布式锁 AOP 切面 |
| `TenantRedisCacheManager` | 租户隔离的 CacheManager（@Primary），自动拼接 `:tenantId` 后缀到 Cache Name |
| `TwoLevelCacheManager` | 二级缓存管理器（Caffeine 本地 + Redis 远程），命名 Bean `twoLevelCacheManager` |
| `TwoLevelCache` | 二级缓存实现：读取 Caffeine→Redis→回填，写入/删除通过 Pub/Sub 通知多实例 |
| `CacheRefreshListener` | Redis Pub/Sub 监听器，收到失效通知后清除本地 Caffeine 缓存 |
| `TwoLevelCacheAutoConfiguration` | 二级缓存自动配置（@ConditionalOnClass(Caffeine)） |

---

## common-log

日志和 Kafka 消息相关。

### 操作日志

| 类 | 说明 |
|----|------|
| `@OperationLog` | 操作日志注解：module, operation, type(OperateType), recordDiff |
| `OperateType` | 操作类型枚举：OTHER / QUERY / CREATE / UPDATE / DELETE / EXPORT / IMPORT / LOGIN / LOGOUT |
| `OperationLogAspect` | AOP 切面，收集方法执行信息，支持字段 Diff，通过 Kafka 异步发送日志 |
| `OperationLogEvent` | 操作日志事件 DTO（含 changeDiff 字段变更内容） |
| `@DiffField` | 标注实体字段的中文显示名，用于 Diff 对比 |
| `DiffUtil` | 对比两个同类型对象中 @DiffField 字段的差异，生成可读文本 |
| `OperationLogContext` | ThreadLocal 上下文，Service 层设置修改前后对象供切面计算 Diff |

### API 访问/错误日志

| 类 | 说明 |
|----|------|
| `@ApiAccessLog` | 接口粒度控制注解：enable（是否记录）、logArgs（是否记录参数） |
| `ApiAccessLogFilter` | OncePerRequestFilter，记录每次 API 请求，支持 @ApiAccessLog 注解控制，通过 Kafka 异步发送 |
| `ApiErrorLogFilter` | OncePerRequestFilter，捕获异常记录错误日志 |
| `ApiAccessLogEvent` | 访问日志 DTO |
| `ApiErrorLogEvent` | 错误日志 DTO |
| `ApiLogAutoConfiguration` | 自动配置（@ConditionalOnBean(KafkaProducerService)） |

### 链路追踪

| 类 | 说明 |
|----|------|
| `TraceAutoConfiguration` | Trace 自动配置 |
| `TraceResponseFilter` | 将 traceId 写入响应 Header |

### Kafka

| 类 | 说明 |
|----|------|
| `KafkaConfig` | Kafka 配置 + Topic 定义（saas-operation-log, saas-api-access-log, saas-api-error-log） |
| `KafkaProducerService` | Kafka 生产者封装 |
| `TenantKafkaProducerInterceptor` | 生产时自动将 tenantId 写入 Kafka Header |
| `TenantKafkaConsumerInterceptor` | 消费时从 Kafka Header 还原 TenantContext |
| `TenantKafkaListenerInterceptor` | @KafkaListener 方法拦截器，还原租户上下文 |

---

## common-excel

Excel 导入导出封装。

| 类 | 说明 |
|----|------|
| `ExcelUtils` | FastExcel 读写工具类 |
| `@DictFormat` | 字典格式化注解：指定字典类型编码 |
| `DictDataProvider` | 字典数据提供者接口（SPI），业务侧实现 |
| `DictFormatConverter` | FastExcel Converter 实现，导出时字典值→标签，导入时标签→字典值 |
| `ExcelAutoConfiguration` | 自动配置（@ConditionalOnBean(DictDataProvider)） |

---

## common-storage

对象存储封装。

| 类 | 说明 |
|----|------|
| `MinioConfig` | MinIO 客户端配置 |
| `MinioService` | MinIO 操作服务（上传/下载/删除/生成预签名 URL） |

---

## common-websocket

WebSocket 框架模块，支持集群广播。

### 会话管理

| 类 | 说明 |
|----|------|
| `WebSocketSessionManager` | ConcurrentHashMap<Long, Set<WebSocketSession>> 管理，支持多设备 |

### 消息推送

| 类 | 说明 |
|----|------|
| `WebSocketMessageSender` | 接口：sendToUser / broadcast |
| `LocalWebSocketMessageSender` | 本地推送实现（直接通过 SessionManager） |
| `KafkaWebSocketMessageSender` | Kafka 集群广播实现（每个实例消费并本地投递） |

### 认证

| 类 | 说明 |
|----|------|
| `WebSocketTokenResolver` | SPI 接口，解析 Token 返回 TokenInfo(userId, tenantId) |
| `LoginUserHandshakeInterceptor` | 握手阶段校验 Token，存储用户信息到 Session Attributes |

### 消息处理

| 类 | 说明 |
|----|------|
| `JsonWebSocketHandler` | TextWebSocketHandler 实现，管理连接生命周期，委托消息给 Listener |
| `WebSocketMessageListener` | SPI 接口，业务侧实现消息处理 |

### 配置

| 类 | 说明 |
|----|------|
| `WebSocketAutoConfiguration` | 自动配置（@ConditionalOnBean(WebSocketTokenResolver)），注册 `/ws` 端点，自动选择 Local 或 Kafka 推送 |
