# 02 — 系统架构设计

## 1. 整体架构总览

```
                          ┌─────────────┐
                          │   CDN/DNS   │
                          └──────┬──────┘
                                 │
               ┌─────────────────┴─────────────────┐
               │        Nginx（SSL 卸载 + 静态）       │
               │   /admin/* → 租户前端                 │
               │   /platform/* → 平台运营前端           │
               │   /api/** → Gateway                  │
               └─────────────────┬─────────────────┘
                                 │
               ┌─────────────────┴─────────────────┐
               │         Spring Cloud Gateway       │
               │  ┌─ JWT 解析 ─ 租户识别 ─ 限流 ─┐   │
               │  │  签名注入  ─ 路由转发        │   │
               │  └─────────────────────────────┘   │
               └──┬──────┬──────┬──────┬───────────┘
                  │      │      │      │
        ┌─────────┤      │      │      │
        │         │      │      │      │
   ┌────▼───┐ ┌──▼────┐ │  ┌───▼────┐ │
   │Platform │ │ RBAC  │ │  │Workflow│ │
   │Service  │ │Service│ │  │Service │ │
   │(平台管理)│ │(权限)  │ │  │(流程)   │ │
   └────────┘ └───────┘ │  └────────┘ │
                     ┌───▼────┐   ┌───▼──────┐
                     │WeChat  │   │Notify    │
                     │OA Svc  │   │Service   │
                     │(公众号) │   │(通知中心) │
                     └────────┘   └──────────┘
        │         │      │      │      │
        └─────────┴──────┴──────┴──────┘
                         │
              ┌──────────┴──────────┐
              │    基础设施层          │
              │ Nacos  Redis  Kafka  │
              │ MySQL  MinIO  Zipkin │
              └─────────────────────┘
```

## 2. 服务拆分

| 服务 | 端口 | 职责 | 数据库 |
|------|------|------|--------|
| **gateway** | 8080 | JWT 解析、租户识别、限流、签名注入、路由转发 | 无（纯转发） |
| **platform-service** | 8084 | 租户 CRUD、套餐管理、全局配置、平台统计、系统公告 | `platform` |
| **rbac-service** | 8081 | 用户/角色/部门/菜单管理（租户维度） | `rbac` |
| **workflow-service** | 8082 | 流程设计/部署/审批、Flowable 引擎 | `workflow` |
| **wechat-oa-service** | 8083 | 公众号绑定/素材/粉丝/自动回复/菜单 | `wechat_oa` |
| **notify-service** | 8085 | 站内消息、邮件、IM Webhook（异步消费 Kafka） | `notify` |

### 2.1 为什么新增 platform-service？

```
传统单租户：rbac-service 管所有用户和权限
SaaS 多租户：必须分离「平台管理」和「租户管理」

平台管理（platform-service）：
  - 租户生命周期（注册/试用/冻结/注销）
  - 套餐与配额（增删改查 + 配额校验接口）
  - 平台级菜单模板管理
  - 全局统计看板
  - 系统公告

租户管理（rbac-service）：
  - 租户内部的用户/角色/部门/权限
  - 受配额约束（通过 Feign 调 platform-service 校验）
```

### 2.2 为什么新增 notify-service？

通知触发源分散在多个服务（审批到达、催办、套餐到期、系统公告），如果每个服务自己发通知会导致：
- 通知渠道配置分散
- 消息模板重复
- 难以做统一的已读/未读管理

抽成独立服务，其他服务通过 **Kafka 事件** 触发通知：

```
workflow-service  ──publish──→  [topic: notification-events]  ──consume──→  notify-service
platform-service ──publish──→                                              │
                                                                     ┌────┴────┐
                                                                     │ 站内信   │
                                                                     │ 邮件    │
                                                                     │ Webhook │
                                                                     └─────────┘
```

---

## 3. 多租户隔离方案

### 3.1 方案选型

| 方案 | 隔离级别 | 成本 | 运维复杂度 | 适用场景 |
|------|---------|------|-----------|---------|
| 独立数据库 | 高 | 高 | 高 | 大客户/金融/合规要求 |
| **共享数据库 + 行级隔离** | 中 | **低** | **低** | **SaaS 中小企业** |
| Schema 隔离 | 中 | 中 | 中 | 中等规模 |

**选择：共享数据库 + tenant_id 行级隔离**

理由：
- 中小企业 SaaS 场景，单租户数据量可控
- 运维成本最低，一套 DDL 管所有租户
- 通过 MyBatis 拦截器自动注入 `tenant_id`，业务代码无感知

### 3.2 租户上下文传播链路

```
                    ┌─────────────────────────────────────────────────────────┐
                    │                   完整请求链路                            │
                    │                                                         │
前端请求 ──────────►│ Gateway                                                 │
 Header:           │  ├─ 解析 JWT → 提取 tenant_id + user_id                  │
 Authorization:    │  ├─ 校验租户状态（Redis 缓存租户信息）                       │
 Bearer <JWT>      │  │   - 冻结/过期 → 直接 403                               │
                   │  ├─ 注入 Headers:                                        │
                   │  │   X-Tenant-Id / X-User-Id / X-Username / X-Roles      │
                   │  │   X-Data-Scope / X-Signature / X-Timestamp            │
                   │  └─ 路由到下游服务                                        │
                   │                                                         │
                   │ 下游服务 (rbac/workflow/wechat-oa)                        │
                   │  ├─ TenantContextFilter:                                │
                   │  │   验证签名 → 构建 TenantContext → 放入 ThreadLocal      │
                   │  ├─ MyBatis TenantInterceptor:                          │
                   │  │   SELECT/UPDATE/DELETE 自动追加 WHERE tenant_id = ?     │
                   │  │   INSERT 自动填充 tenant_id 字段                        │
                   │  └─ Feign 拦截器:                                        │
                   │      服务间调用自动传播 X-Tenant-Id                         │
                   └─────────────────────────────────────────────────────────┘
```

### 3.3 TenantContext 设计

```java
public class TenantContext {
    private static final TransmittableThreadLocal<TenantInfo> CONTEXT
        = new TransmittableThreadLocal<>();

    // TenantInfo 包含:
    // - tenantId       (Long)
    // - tenantName     (String)
    // - packageType    (枚举: FREE/BASIC/PRO/ENTERPRISE)
    // - tenantStatus   (枚举: TRIAL/ACTIVE/FROZEN/DEACTIVATED)
}
```

**关键规则：**
- 平台侧接口（`/platform/**`）不走租户拦截器，直接操作所有租户数据
- 租户侧接口（`/api/**`）必须带 `tenant_id`，拦截器自动注入
- 使用 `TransmittableThreadLocal` 保证 `@Async` 和线程池场景下租户上下文不丢失

### 3.4 MyBatis 租户拦截器

```java
// 伪代码，说明核心逻辑
@Intercepts({
    @Signature(type = Executor.class, method = "update", ...),
    @Signature(type = Executor.class, method = "query", ...)
})
public class TenantSqlInterceptor implements Interceptor {

    // 不需要租户隔离的表（白名单）
    private static final Set<String> IGNORE_TABLES = Set.of(
        "sys_menu",           // 平台级菜单
        "sys_package",        // 平台级套餐
        "sys_tenant",         // 租户表本身
        "sys_platform_user"   // 平台用户
    );

    @Override
    public Object intercept(Invocation invocation) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) return invocation.proceed(); // 平台侧，不过滤

        // 用 JSqlParser 解析 SQL
        // SELECT → 追加 WHERE tenant_id = #{tenantId}
        // INSERT → 自动填充 tenant_id 列
        // UPDATE/DELETE → 追加 WHERE tenant_id = #{tenantId}
        // 遇到 IGNORE_TABLES 中的表 → 跳过
    }
}
```

---

## 4. 认证与权限架构

### 4.1 JWT Token 设计

```json
{
  "sub": "user_id",
  "tid": "tenant_id",
  "username": "zhangsan",
  "roleLevel": 1,
  "dataScope": 2,
  "permissions": ["sys:user:list", "sys:user:add", "wf:process:start"],
  "iat": 1700000000,
  "exp": 1700007200
}
```

**为什么把 permissions 放进 JWT？**

| 方案 | 优点 | 缺点 |
|------|------|------|
| 每次请求查 Redis | 权限实时更新 | 每个请求都有 Redis 网络开销 |
| **JWT 内嵌 permissions** | **零网络开销**，网关直接校验 | 权限变更需重新登录或刷新 Token |

选择 JWT 内嵌方案。理由：
- 权限变更是低频操作，中间等待可以接受
- 提供 Token 刷新接口，管理员修改权限后可主动踢人重新登录
- 大幅降低 Redis 压力和请求延迟

### 4.2 双 Token 机制

```
Access Token:  有效期 2 小时，包含完整权限信息
Refresh Token: 有效期 7 天，仅用于换取新 Access Token

                    ┌─ Access Token 过期 ─┐
                    │                     │
前端 ──────────────►│  自动用 Refresh Token │
                    │  换取新 Access Token  │
                    │  （新 Token 包含最新权限）│
                    └─────────────────────┘
```

### 4.3 权限校验流程

```
前端请求
  │
  ▼
Gateway
  ├─ 解析 JWT
  ├─ 提取 tenant_id → 查 Redis 租户状态缓存
  │   └─ 冻结/过期 → 403 "租户已冻结"
  ├─ 注入 Headers (tenant_id, user_id, permissions, dataScope...)
  ├─ HMAC 签名防篡改
  └─ 转发到下游
        │
        ▼
  下游服务
  ├─ TenantContextFilter: 验签 → 构建上下文
  ├─ @RequirePermission("sys:user:add")  ← 注解式权限校验
  │   └─ AOP 拦截：从 Header 的 permissions 中检查
  ├─ @DataScope(type = DEPT)  ← 数据范围注解
  │   └─ MyBatis 拦截器：自动追加部门过滤条件
  └─ TenantInterceptor: 自动追加 tenant_id
```

### 4.4 @RequirePermission 注解（替代硬编码注册表）

```java
// 使用方式
@RestController
@RequestMapping("/user")
public class UserController {

    @RequirePermission("sys:user:list")
    @GetMapping("/list")
    public R<PageResult<UserVO>> list(UserQuery query) { ... }

    @RequirePermission("sys:user:add")
    @PostMapping
    public R<Void> add(@RequestBody UserDTO dto) { ... }
}

// 实现原理：AOP 拦截器
@Aspect
public class PermissionAspect {
    @Around("@annotation(perm)")
    public Object check(ProceedingJoinPoint pjp, RequirePermission perm) {
        UserInfo user = UserContextHolder.get();
        if (user.getRoleLevel() == 0) return pjp.proceed(); // 超管跳过
        if (!user.getPermissions().contains(perm.value())) {
            throw new ForbiddenException("无权限: " + perm.value());
        }
        return pjp.proceed();
    }
}
```

**优势：**
- 权限定义跟着接口走，不会遗漏
- IDE 可直接搜索 `@RequirePermission` 查看所有权限点
- 不需要维护一个集中式的 PermissionRegistry

---

## 5. 技术选型

### 5.1 后端技术栈

| 层次 | 技术 | 版本 | 说明 |
|------|------|------|------|
| JDK | OpenJDK | 11 | LTS，模块系统 + var |
| 框架 | Spring Boot | 2.7.x | 稳定版本，与 Spring Cloud 兼容 |
| 微服务 | Spring Cloud | 2021.x | 配套 Boot 2.7 |
| 服务发现/配置 | Nacos | 2.2+ | 注册中心 + 配置中心 |
| 网关 | Spring Cloud Gateway | 3.1.x | 响应式网关 |
| ORM | MyBatis-Plus | 3.5.x | 简化 CRUD + 拦截器机制 |
| 流程引擎 | Flowable | 6.8.x | BPMN 2.0，内嵌 + REST API |
| 缓存 | Redis | 7.x | 租户状态缓存、Token 黑名单、分布式锁 |
| 消息队列 | Kafka | 3.x | 异步通知、事件驱动 |
| 对象存储 | MinIO | 最新 | 素材/附件/头像存储 |
| 链路追踪 | Micrometer + Zipkin | — | 替代 Spring Cloud Sleuth（已停更） |
| 监控 | Prometheus + Grafana | — | 指标采集 + 可视化 |
| 容器 | Docker + Docker Compose | — | 本地开发一键启动 |

### 5.2 前端技术栈

| 技术 | 说明 |
|------|------|
| Vue Vben Admin v5 | 企业级管理后台脚手架 |
| Vue 3 + TypeScript | 前端框架 |
| Vite 5 | 构建工具 |
| Ant Design Vue 4 | UI 组件库 |
| Pinia | 状态管理 |
| bpmn-js | BPMN 流程设计器 |

### 5.3 基础设施

| 组件 | 用途 |
|------|------|
| MySQL 8.0 | 主数据库 |
| Redis 7.x | 缓存 + Token 黑名单 + 分布式锁 |
| Kafka | 异步事件 + 通知解耦 |
| Nacos | 服务发现 + 配置中心 |
| MinIO | 对象存储 |
| Zipkin | 链路追踪 |
| Nginx | 反向代理 + 静态资源 |

---

## 6. 服务间通信

### 6.1 同步调用（Feign）

```
rbac-service ──Feign──→ platform-service   (校验配额：用户数是否超限)
workflow-service ──Feign──→ rbac-service   (查询审批人信息)
wechat-oa-service ──Feign──→ platform-service (校验公众号绑定配额)
```

**内部调用鉴权：**
- 不走 Gateway，服务间直连
- 使用 HMAC 签名 + 时间戳（5 秒有效期）防止伪造
- 自动传播 `X-Tenant-Id` Header

### 6.2 异步事件（Kafka）

| Topic | 生产者 | 消费者 | 场景 |
|-------|-------|-------|------|
| `notification-events` | workflow / platform | notify-service | 审批通知、系统公告 |
| `tenant-lifecycle` | platform-service | rbac / workflow / wechat-oa | 租户冻结→各服务清理缓存 |
| `quota-change` | platform-service | rbac / wechat-oa | 配额变更→各服务更新本地缓存 |
| `user-permission-change` | rbac-service | gateway | 权限变更→踢出在线 Token |

### 6.3 租户上下文在 Feign 中的传播

```java
@Component
public class TenantFeignInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        TenantInfo tenant = TenantContext.get();
        if (tenant != null) {
            template.header("X-Tenant-Id", String.valueOf(tenant.getTenantId()));
        }
        // 内部调用签名
        String timestamp = String.valueOf(System.currentTimeMillis());
        String signature = HmacUtil.sign(internalSecret, timestamp);
        template.header("X-Internal-Timestamp", timestamp);
        template.header("X-Internal-Signature", signature);
    }
}
```

---

## 7. 网关设计

### 7.1 路由规则

```yaml
# 平台侧路由 —— 平台管理员专用
- id: platform-service
  uri: lb://platform-service
  predicates:
    - Path=/api/platform/**
  filters:
    - StripPrefix=2          # /api/platform/tenant → /tenant
    - PlatformAuth           # 校验平台管理员身份

# 租户侧路由
- id: rbac-service
  uri: lb://rbac-service
  predicates:
    - Path=/api/rbac/**
  filters:
    - StripPrefix=2
    - TenantAuth             # 校验租户身份 + 注入租户上下文

- id: workflow-service
  uri: lb://workflow-service
  predicates:
    - Path=/api/workflow/**
  filters:
    - StripPrefix=2
    - TenantAuth

- id: wechat-oa-service
  uri: lb://wechat-oa-service
  predicates:
    - Path=/api/wechat-oa/**
  filters:
    - StripPrefix=2
    - TenantAuth
```

### 7.2 网关过滤器链

```
请求 → RateLimitFilter → JwtParseFilter → TenantStatusFilter → SignatureInjectFilter → 路由
         │                   │                   │                      │
         │                   │                   │                      └─ HMAC 签名注入
         │                   │                   └─ Redis 查租户状态，冻结则 403
         │                   └─ 解析 JWT，提取用户和租户信息
         └─ 基于 tenant_id 的限流（令牌桶）
```

### 7.3 白名单（免认证路径）

```yaml
security:
  permit-all-patterns:
    - /api/rbac/auth/login          # 登录
    - /api/rbac/auth/register       # 租户注册
    - /api/rbac/auth/refresh-token  # Token 刷新
    - /api/rbac/auth/captcha        # 验证码
    - /api/wechat-oa/callback/**    # 微信回调
    - /actuator/**                  # 健康检查
```

---

## 8. 数据范围（DataScope）控制

### 8.1 数据范围级别

| 级别 | 枚举值 | 说明 |
|------|-------|------|
| 全部数据 | 1 | 看本租户所有数据 |
| 本部门及下级 | 2 | 基于 ancestors 字段查询 |
| 本部门 | 3 | 仅本部门 |
| 仅本人 | 4 | 只看自己创建的 |
| 自定义 | 5 | 指定部门列表 |

### 8.2 ancestors 祖先链设计（替代递归查询）

```
部门表:
  id=1, name=总公司,   parent_id=0, ancestors="0"
  id=2, name=技术部,   parent_id=1, ancestors="0,1"
  id=3, name=前端组,   parent_id=2, ancestors="0,1,2"
  id=4, name=后端组,   parent_id=2, ancestors="0,1,2"

查 "技术部及下级" 的所有部门:
  SELECT * FROM sys_dept WHERE ancestors LIKE '0,1,2%' AND tenant_id = ?

一条 SQL，零递归，零缓存依赖。
```

### 8.3 MyBatis 数据范围拦截器

```java
// 伪代码
@DataScope(deptAlias = "d", userAlias = "u")
public List<UserVO> selectUserList(UserQuery query) { ... }

// 拦截器自动追加:
// data_scope=1 → 不追加（看租户全部）
// data_scope=2 → AND d.ancestors LIKE CONCAT(#{userAncestors}, '%')
// data_scope=3 → AND d.id = #{userDeptId}
// data_scope=4 → AND u.create_user_id = #{userId}
// data_scope=5 → AND d.id IN (自定义部门列表)
```

---

## 9. 公共模块设计

```
common/
├── common-core/           # 基础：R<T> 响应体、异常体系、常量、工具类
├── common-security/       # 安全：JWT 工具、TenantContext、权限注解、拦截器
├── common-data/           # 数据：MyBatis 配置、租户拦截器、数据范围拦截器、审计字段自动填充
├── common-redis/          # Redis：RedisTemplate 配置、分布式锁、缓存注解
├── common-kafka/          # Kafka：生产者/消费者配置、事件基类
├── common-feign/          # Feign：租户上下文传播、内部鉴权拦截器、降级工厂
├── common-log/            # 日志：操作日志注解 + AOP、链路追踪 MDC
└── common-storage/        # 存储：MinIO 客户端封装、文件上传/下载工具
```

### 9.1 各模块引用关系

```
common-core        ← 所有模块都依赖
common-security    ← 所有业务服务依赖（Gateway 除外，Gateway 有独立的 JWT 解析）
common-data        ← 所有有数据库的服务依赖
common-redis       ← Gateway + 需要缓存的服务
common-kafka       ← 需要事件发布/消费的服务
common-feign       ← 需要服务间调用的服务
common-log         ← 所有业务服务
common-storage     ← wechat-oa-service（素材上传）
```

---

## 10. 容错与高可用

### 10.1 服务降级

```
rbac-service 不可用时:
  ├─ workflow-service 的审批人查询 → 降级返回缓存中的用户信息
  └─ 降级策略：@FeignClient(fallbackFactory = RbacFallbackFactory.class)

platform-service 不可用时:
  ├─ 配额校验 → 降级为放行（不阻塞业务）
  └─ 记录降级日志，后续补偿校验
```

### 10.2 租户级限流

```
Gateway 限流维度:
  ├─ 全局：总 QPS 上限
  ├─ 租户级：每个 tenant_id 独立令牌桶
  │   ├─ 免费版: 100 QPS
  │   ├─ 基础版: 500 QPS
  │   ├─ 专业版: 2000 QPS
  │   └─ 旗舰版: 按需
  └─ IP 级：防止未认证请求暴力攻击
```

### 10.3 Token 黑名单

```
场景：管理员修改某用户权限后需要踢下线

流程：
  1. rbac-service 发布 Kafka 事件: user-permission-change
  2. Gateway 消费事件，将 old access_token 加入 Redis 黑名单
  3. 黑名单 Key: token:blacklist:{jti}，TTL = token 剩余有效期
  4. 用户下次请求 → Gateway 发现 token 在黑名单 → 401
  5. 前端自动用 refresh_token 获取新 token（包含最新权限）
```

---

## 11. 部署架构

### 11.1 本地开发

```yaml
# docker-compose.yml 一键启动所有中间件
services:
  mysql:
    image: mysql:8.0
    ports: ["3306:3306"]
    volumes:
      - ./sql/init:/docker-entrypoint-initdb.d  # 自动建库建表

  redis:
    image: redis:7-alpine
    ports: ["6379:6379"]

  kafka:
    image: bitnami/kafka:3.6
    ports: ["9092:9092"]

  nacos:
    image: nacos/nacos-server:v2.2.3
    ports: ["8848:8848"]

  minio:
    image: minio/minio
    ports: ["9000:9000", "9001:9001"]

  zipkin:
    image: openzipkin/zipkin
    ports: ["9411:9411"]
```

### 11.2 生产部署（未来）

```
                 ┌─ K8s Cluster ──────────────────┐
                 │                                 │
                 │  Ingress (Nginx)                │
                 │    ↓                            │
                 │  Gateway (2 Pod)                │
                 │    ↓                            │
                 │  Services (各 2 Pod)             │
                 │    platform / rbac / workflow    │
                 │    wechat-oa / notify            │
                 │                                 │
                 │  中间件（独立或云托管）              │
                 │    RDS / ElastiCache / MSK       │
                 └─────────────────────────────────┘
```

---

## 12. 平台侧 vs 租户侧的前端分离

```
两套独立前端，共享 UI 组件库：

┌─ 租户端 (admin.example.com) ─────────┐
│  Vue Vben Admin v5                    │
│  功能：权限管理 / 流程审批 / 公众号运营    │
│  登录后 JWT 含 tenant_id              │
└───────────────────────────────────────┘

┌─ 平台运营端 (platform.example.com) ──┐
│  Vue Vben Admin v5（独立部署）          │
│  功能：租户管理 / 套餐管理 / 全局统计     │
│  登录后 JWT 不含 tenant_id            │
└───────────────────────────────────────┘
```

**为什么分开？**
- 安全隔离：平台管理员的操作不应该和租户端混在一起
- 部署独立：平台端可以独立发版，不影响租户端
- 权限简单：不需要在一个前端里处理"平台用户"和"租户用户"的菜单差异
