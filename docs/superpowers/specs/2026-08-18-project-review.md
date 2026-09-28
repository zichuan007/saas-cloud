# SaaS Cloud 项目框架审查报告

> 审查日期：2026-08-18

---

## 一、当前架构全景

```
                    ┌─ gateway (8080) ─┐
                    │  Sa-Token + JWT   │
                    │  路由: /api/*     │
                    └───────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
   ┌────▼─────┐    ┌─────────▼──────┐    ┌─────────▼──────┐
   │ platform  │    │    rbac        │    │   workflow     │
   │ (8084)   │◄───│   (8081)       │    │   (8082)       │
   │ 传统3层  │    │   传统3层      │    │  DDD 分层      │
   └──────────┘    └────────────────┘    └────────────────┘
        ▲                     │
        │          ┌──────────▼──────┐
        └──────────│  wechat-oa     │
                   │  (8083)        │
                   │  传统3层       │
                   └────────────────┘

   ┌──────────┐    ┌──────────────┐
   │  notify   │    │  code-gen    │
   │  (8085)  │    │  (独立)      │
   │  传统3层  │    └──────────────┘
   └──────────┘

   Kafka: saas-notify-event, saas-operation-log, saas-websocket-broadcast
   Feign: platform-api (已接线), rbac-api (未接线)
```

---

## 二、按严重程度排列的问题

### 🔴 P0 — 架构一致性问题

#### 1. workflow-service 与其余服务架构风格不一致

| 服务 | 架构风格 | 持久层 |
|------|---------|--------|
| platform-service | Controller → Service → Entity/MyBatis-Plus | MyBatis-Plus BaseMapper |
| rbac-service | Controller → Service → Entity/MyBatis-Plus | MyBatis-Plus BaseMapper |
| wechat-oa-service | Controller → Service → Entity/MyBatis-Plus | MyBatis-Plus BaseMapper |
| notify-service | Controller → Service → Entity/MyBatis-Plus | MyBatis-Plus BaseMapper |
| **workflow-service** | **Controller → Domain → Tunnel/DAO → XML** | **MyBatis XML** |

**问题**：同一个项目两种完全不同的架构风格，维护成本翻倍。新同事学完 rbac 后看 workflow 会一脸懵。

**建议选项**：
- A：把 workflow 改回传统三层（推翻重来，不推荐）
- B：把其他服务也改成 DDD 分层（工程量大，但方向正确）
- C：保持现状，但把 workflow 的 DAO/Mapper 改回 MyBatis-Plus BaseMapper，XML 只保留复杂查询（折中，推荐）

#### 2. MyBatis-Plus 逻辑删除/乐观锁在 workflow 失效

`BaseEntity` 的 `@TableLogic` 和 `@Version` 只对 MyBatis-Plus `BaseMapper` 生效。workflow 全部用 XML Mapper，这两个能力丢失。

**影响**：`deleteById` 是物理删除，`dataVersion` 不会自动 +1，并发更新无保护。

**建议**：把 workflow 的基础 CRUD 改回 MyBatis-Plus `BaseMapper`，XML 只保留 `selectByXxx` 复杂查询。

---

### 🟡 P1 — 功能缺失

#### 3. RbacFeignClient 未接线

定义了 `getUserById`、`getUserCount`、`getDeptLeader`，但**没有任何服务注入使用**。

**影响**：workflow 的审批人分配策略中 `RoleAssignmentStrategy`、`DeptAssignmentStrategy` 无法查询 RBAC 的角色成员和部门负责人，目前是空实现。

**建议**：workflow 的 `RoleAssignmentStrategy` 和 `DeptAssignmentStrategy` 注入 `RbacFeignClient`，实现真正的审批人查询。

#### 4. 签名校验默认关闭

`saas.security.signature-enforced=false`，X-Signature 校验失败仅告警不拦截。

**影响**：内部调用签名形同虚设，任何服务可以伪造 X-Internal-Source 头绕过 `@InnerApi` 校验。

**建议**：生产环境强制开启 `saas.security.signature-enforced=true`。

#### 5. workflow Feign 接口缺失

`workflow-api/feign/` 目录为空，其他服务无法通过 Feign 调用 workflow。

**影响**：如果 rbac 或 notify 需要查询流程状态、发起审批，只能走 HTTP 直连，没有类型安全的 Feign 接口。

**建议**：至少建 `TaskFeignClient` 和 `ProcessInstanceFeignClient`。

---

### 🟢 P2 — 代码质量

#### 6. CLAUDE.md 多处过时

| 内容 | 文档说 | 实际 |
|------|--------|------|
| 模块列表 | common-security, common-kafka, common-feign 独立模块 | 代码在 common-data/common-log 内 |
| 表数量 | 26 张 | ~46 张 |
| Kafka Topic | notification-events, tenant-lifecycle, quota-change | saas-notify-event 等 |
| workflow 架构 | "Controller-Service-Mapper 三层" | 实际是 DDD 分层 |

**建议**：更新 CLAUDE.md 到最新状态。

#### 7. wechat-oa-api 空壳

目录存在但无 pom.xml、无 src，未在 services/pom.xml 注册。

**建议**：要么删除，要么补全并注册为模块。

#### 8. workflow Dao 层冗余

10 个 DAO 类只是纯转发 Mapper 方法，没有任何额外逻辑。

**建议**：去掉 DAO 层，Controller/DomainService 直接注入 Mapper 接口。

---

### 🔵 P3 — 运维与安全

#### 9. 缺少全局异常处理一致性

`common-core` 有 `GlobalExceptionHandler`，但各服务可能有自己的异常处理，行为不一致。

**建议**：统一用 `common-core` 的 `GlobalExceptionHandler`，各服务不要自定义。

#### 10. 缺少 API 限流策略

只有 `SentinelRuleInitializer` 做了基础 QPS 限流，没有熔断降级策略。

**建议**：为关键接口（登录、审批、支付）加上 Sentinel 熔断规则。

---

## 三、优先级建议

| 优先级 | 问题 | 建议 | 工作量 |
|--------|------|------|--------|
| **P0-1** | 架构不一致 | workflow CRUD 改回 MyBatis-Plus BaseMapper | 2天 |
| **P0-2** | 逻辑删除/乐观锁失效 | 同上，一个改动修两个问题 | — |
| **P1-3** | RbacFeignClient 未接线 | workflow 注入 RbacFeignClient | 1天 |
| **P1-4** | 签名校验关闭 | 配置文件改一行 | 1分钟 |
| **P1-5** | workflow Feign 缺失 | 建 2 个 FeignClient | 半天 |
| **P2-6** | CLAUDE.md 过时 | 重写文档 | 1小时 |
| **P2-7** | wechat-oa-api 空壳 | 删除或补全 | 5分钟 |
| **P2-8** | DAO 层冗余 | 去掉 DAO，直接用 Mapper | 1天 |
| **P3-9** | 异常处理一致性 | 审查各服务 | 半天 |
| **P3-10** | 限流策略 | 补充 Sentinel 规则 | 半天 |