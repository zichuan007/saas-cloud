# SaaS Cloud 架构总览

> 最后更新：2026-05-24

## 技术栈

| 层面 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 17 |
| 框架 | Spring Boot | 3.4.5 |
| 微服务 | Spring Cloud | 2024.0.3 |
| 微服务（阿里巴巴） | Spring Cloud Alibaba | 2023.0.3.4 |
| ORM | MyBatis-Plus | 3.5.16 |
| 认证 | Sa-Token | — |
| 流控 | Sentinel | — |
| 注册/配置中心 | Nacos | 2.2.3 |
| 数据库 | MySQL | 8.0 |
| 缓存 | Redis (Redisson) | 7 |
| 消息队列 | Kafka (Confluent) | 7.5 |
| 对象存储 | MinIO | — |
| 工作流引擎 | Flowable | — |
| Excel | FastExcel (cn.idev.excel) | — |
| 前端 | Vue 3 + Vben Admin v5 | pnpm monorepo |

## 模块结构

```
saas-cloud/
├── common/                        # 公共模块层
│   ├── common-core/               # 基础：ApiResult, ResultCode, 异常, 常量, XSS, 脱敏, Sentinel, XxlJob
│   ├── common-data/               # 数据：MyBatis-Plus, 多租户, 数据权限, 审计字段, Sa-Token, 安全注解
│   ├── common-redis/              # Redis：RedisTemplate, @Idempotent, @DistributedLock, TenantRedisCacheManager
│   ├── common-log/                # 日志：@OperationLog, Kafka生产者, 链路追踪, API访问/错误日志, Kafka租户传播
│   ├── common-excel/              # Excel：FastExcel封装, @DictFormat字典转换
│   ├── common-storage/            # 存储：MinIO客户端
│   └── common-websocket/          # WebSocket：Session管理, 消息推送(本地/Kafka), Token认证握手
├── services/
│   ├── gateway/           (8080)  # API网关：Sa-Token认证, 路由转发, 租户校验
│   ├── platform-api/              # 平台服务 Feign 接口 + DTO/VO
│   ├── platform-service/  (8084)  # 平台服务：租户管理, 套餐, 公告, 全局配置, 文件, API客户端
│   ├── rbac-api/                  # RBAC 服务 Feign 接口 + DTO/VO
│   ├── rbac-service/      (8081)  # RBAC 服务：用户/角色/部门/菜单/字典/认证/日志/导出/三方登录/敏感词/地区/公告/岗位
│   ├── workflow-api/              # 工作流 Feign 接口 + DTO/VO
│   ├── workflow-service/  (8082)  # 工作流服务：Flowable流程定义/实例/任务/抄送/节点配置
│   ├── wechat-oa-api/             # 微信公众号 Feign 接口 + DTO/VO
│   ├── wechat-oa-service/ (8083)  # 微信公众号：账号/素材/图文/粉丝/标签/菜单/自动回复
│   ├── notify-api/                # 通知服务 Feign 接口 + DTO/VO
│   └── notify-service/    (8085)  # 通知服务：站内消息/邮件/Webhook(钉钉/飞书/企微)/短信/模板
├── code-generator/                # MyBatis-Plus 代码生成器
└── frontend/                      # Vue 3 前端 (pnpm monorepo)
    ├── apps/web-admin/            # 租户管理端
    ├── apps/web-platform/         # 平台运营端
    └── apps/backend-mock/         # Mock 数据
```

## API 分层约定

每个业务域拆分为 `*-api` 和 `*-service`：
- **`*-api`**：FeignClient 接口 + DTO/VO，供其他服务依赖
- **`*-service`**：Controller / Service / Mapper / Entity 实现

### 包内分层

```
com.saas.cloud.{module}/
├── controller/        # REST API 端点
├── service/           # 接口定义 (I{Entity}Service)
│   └── impl/          # 实现类
├── mapper/            # MyBatis-Plus Mapper 接口
├── entity/            # 数据库实体 (extends BaseEntity / TenantBaseEntity)
├── config/            # 配置类
├── job/               # 定时任务
├── tenant/            # 租户初始化器 (rbac 特有)
├── captcha/           # 验证码 (rbac 特有)
├── sender/            # 消息发送器 (notify 特有)
└── consumer/          # Kafka 消费者 (notify 特有)
```

## 多租户架构

### 核心链路

```
Gateway (SaReactorFilter 认证)
  → AuthGlobalFilter (剥离伪造Header, 从Sa-Token Session提取用户信息透传)
    → TenantContextFilter (从 X-Tenant-Id 写入 TenantContext - TransmittableThreadLocal)
      → MyBatis-Plus TenantLineInnerInterceptor (自动追加 WHERE tenant_id = ?)
```

### 租户传播

| 场景 | 机制 |
|------|------|
| HTTP 请求 | Gateway → `X-Tenant-Id` Header → `TenantContextFilter` |
| Feign 调用 | `TenantFeignInterceptor` 自动携带 `X-Tenant-Id` |
| Kafka 消息 | `TenantKafkaProducerInterceptor` 写入 Header / `TenantKafkaConsumerInterceptor` 还原 |
| 定时任务 | `@TenantJob` + `TenantJobAspect` 遍历所有租户执行 |
| Redis 缓存 | `TenantRedisCacheManager` 自动拼接 `:tenantId` 到 Cache Name |

### 跳过租户过滤

- **声明式**：`@TenantIgnore` 注解 + AOP
- **编程式**：`TenantContext.setIgnoreTenant(true)` 或 `TenantContext.executeWithoutTenant(Runnable)`
- **配置式**：`TenantProperties.ignoreTables` 配置平台级表

### 租户初始化

新租户创建时，`TenantInitializerRegistry` 自动调用所有 `TenantInitializer` 实现：
- `AdminUserInitializer` — 创建租户管理员
- `DeptInitializer` — 创建默认部门
- `RoleInitializer` — 创建默认角色
- `RoleMenuInitializer` — 绑定角色菜单
- `UserRoleInitializer` — 绑定用户角色

## 认证与权限

### 认证流程

1. Gateway 的 `SaReactorFilter`（由 `SaTokenGatewayConfig` 配置）校验 Token
2. `AuthGlobalFilter` 从 Sa-Token Session 读取用户信息，透传 Header（userId, username, tenantId, permissions, deptId, dataScope, roleLevel）
3. 下游服务 `TenantContextFilter` / `UserContext` 从 Header 恢复上下文

### 白名单路径

```
/api/rbac/auth/login, /api/rbac/auth/refresh, /api/rbac/auth/register
/api/rbac/captcha/**, /api/rbac/auth/social/**
/api/platform/auth/login, /api/generator/**
/doc.html, /webjars/**, /swagger-resources/**, /v3/api-docs/**
/actuator/**, /api/rbac/ws/**
```

### 权限控制

- **功能权限**：`@RequirePermission("sys:user:add")` + `PermissionAspect`
- **数据权限**：`@DataScope` + `DataScopeAspect` + `DataScopeSqlInterceptor`
  - 数据范围类型：`DataScopeEnum`（ALL / DEPT_AND_BELOW / DEPT_ONLY / SELF）
  - 基于部门 `ancestors` 字段实现层级查询
- **内部调用**：`@InnerApi` + `InnerApiAspect` 校验 `X-Internal-Source` Header + 签名验证

## 网关路由

规则：`/api/{service-name}/**` → StripPrefix=2 → 对应服务

| 路径前缀 | 目标服务 | 端口 |
|---------|---------|------|
| /api/rbac/** | rbac-service | 8081 |
| /api/workflow/** | workflow-service | 8082 |
| /api/wechat-oa/** | wechat-oa-service | 8083 |
| /api/platform/** | platform-service | 8084 |
| /api/notify/** | notify-service | 8085 |

## 服务间通信

### 同步 Feign 调用

| 调用方 | 被调方 | FeignClient | 用途 |
|--------|--------|------------|------|
| rbac-service | platform-service | PlatformFeignClient | 配额校验、租户信息 |
| workflow-service | rbac-service | RbacFeignClient | 查询审批人信息 |
| 所有服务 | rbac-service | RbacFeignClient | 内部 API 调用 |

Feign 内部调用走 `/internal/**` 路径，`TenantFeignInterceptor` 自动传播租户上下文和内部签名。

### 异步 Kafka Topics

| Topic | 生产者 | 消费者 | 用途 |
|-------|--------|--------|------|
| notification-events | 各服务 | notify-service | 通知触发 |
| tenant-lifecycle | platform-service | rbac-service 等 | 租户状态变更 |
| quota-change | platform-service | 各服务 | 配额变更广播 |
| saas-operation-log | 各服务 | rbac-service | 操作日志异步写入 |
| saas-api-access-log | 各服务 | rbac-service | API访问日志 |
| saas-api-error-log | 各服务 | rbac-service | API错误日志 |
| saas-websocket-broadcast | 各服务 | websocket实例 | WebSocket集群广播 |

## 数据库

5 个独立数据库，按服务隔离：

| 数据库 | 服务 | 表前缀 |
|--------|------|--------|
| platform | platform-service | sys_ |
| rbac | rbac-service | sys_ |
| wechat_oa | wechat-oa-service | wechat_oa_ |
| notify | notify-service | notify_ / sys_sms_ |
| workflow | workflow-service | wf_ |

### 实体基类

- **BaseEntity**：id(AUTO), createUserId, createUserName, createTime, updateUserId, updateUserName, updateTime, deleteFlag(逻辑删除), dataVersion(乐观锁), remark
- **TenantBaseEntity** extends BaseEntity：+ tenantId

## 基础设施（docker-compose）

| 组件 | 端口 | 说明 |
|------|------|------|
| MySQL 8.0 | 3306 | 5 个独立数据库 |
| Redis 7 | 6379 | 缓存 + 分布式锁 + 幂等 + 限流 |
| Nacos 2.2.3 | 8848 | 注册中心 + 配置中心 |
| Kafka (Confluent 7.5) | 9092 | 异步消息 |
| MinIO | 9000/9001 | 对象存储 |
