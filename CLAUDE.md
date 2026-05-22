# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

SaaS Cloud 是一个多租户企业管理平台，采用 Spring Cloud 微服务架构，基于 `tenant_id` 行级隔离实现多租户数据隔离。

**技术栈**: Java 17 / Spring Boot 3.4.5 / Spring Cloud 2024.0.3 / Spring Cloud Alibaba 2023.0.3.4 / MyBatis-Plus 3.5.16

## Build & Run Commands

```bash
# 构建全部模块
mvn clean package -DskipTests

# 构建单个服务（例如 rbac-service）
mvn clean package -DskipTests -pl services/rbac-service -am

# 启动基础设施（MySQL/Redis/Nacos/Kafka/MinIO）
docker-compose up -d

# 初始化数据库（创建 5 个库 + 26 张表 + 种子数据）
mysql -u root -p < docs/sql/init.sql

# 运行单个服务
mvn spring-boot:run -pl services/rbac-service
```

前端（`frontend/` 目录，基于 Vue Vben Admin v5 monorepo）：
```bash
cd frontend
pnpm install
pnpm dev:admin      # 租户管理端
pnpm dev:platform   # 平台运营端
pnpm build:admin
pnpm build:platform
pnpm test:unit      # vitest
pnpm lint
```

## Architecture

### Module Structure

```
saas-cloud/
├── common/                          # 公共模块（所有服务共享）
│   ├── common-core/                 # 基础: ApiResult<T>, ResultCode, 异常体系, 常量
│   ├── common-security/             # 安全: TenantContext, @RequirePermission, @InnerApi, JWT 工具
│   ├── common-data/                 # 数据: MyBatis-Plus 配置, 租户拦截器, DataScope 拦截器, 审计字段填充
│   ├── common-redis/                # Redis: RedisTemplate 配置
│   ├── common-kafka/                # Kafka: 生产者/消费者封装
│   ├── common-feign/                # Feign: 租户上下文传播 + 内部调用签名
│   ├── common-log/                  # 日志: @OperationLog 注解 + AOP
│   └── common-storage/              # 存储: MinIO 客户端封装
├── services/
│   ├── gateway/             (8080)  # Spring Cloud Gateway, JWT 解析, 路由转发
│   ├── platform-api/                # platform-service 的 Feign 接口 + DTO/VO
│   ├── platform-service/    (8084)  # 租户管理, 套餐配额, 全局配置, 数据库: platform
│   ├── rbac-api/                    # rbac-service 的 Feign 接口 + DTO/VO
│   ├── rbac-service/        (8081)  # 用户/角色/部门/菜单/认证, 数据库: rbac
│   ├── workflow-api/
│   ├── workflow-service/    (8082)  # Flowable 流程引擎, 数据库: workflow
│   ├── wechat-oa-api/
│   ├── wechat-oa-service/   (8083)  # 微信公众号管理, 数据库: wechat_oa
│   ├── notify-api/
│   └── notify-service/      (8085)  # 站内消息/邮件/Webhook, 数据库: notify
├── code-generator/                  # MyBatis-Plus 代码生成器
└── frontend/                        # Vue 3 + Vben Admin v5 (pnpm monorepo)
    ├── apps/web-admin/              # 租户管理端
    ├── apps/web-platform/           # 平台运营端
    └── apps/backend-mock/           # Mock 数据服务
```

### API 分层约定

每个业务领域拆分为 `*-api` 和 `*-service` 两个模块：
- `*-api`: FeignClient 接口定义 + DTO/VO，供其他服务依赖
- `*-service`: 具体实现，包含 Controller/Service/Mapper/Entity

服务间 Feign 调用走 `/internal/**` 路径，接口用 `@InnerApi` 标注，由 `InnerApiAspect` 校验 `X-Internal-Source` 请求头。

### Multi-Tenancy (多租户隔离)

**核心链路**: Gateway 解析 JWT → 提取 `tenantId` → 注入 HTTP Header `X-Tenant-Id` → 下游 `TenantContextFilter` 写入 `TenantContext`(TransmittableThreadLocal) → MyBatis-Plus `TenantLineInnerInterceptor` 自动追加 `WHERE tenant_id = ?`

关键配置（`saas.tenant` 前缀）：
- `TenantProperties.ignoreTables`: 不需要租户隔离的平台级表（sys_menu, sys_package, sys_tenant 等）
- `TenantContext.setIgnoreTenant(true)`: 编程式跳过租户过滤
- `TenantContext.executeWithoutTenant(...)`: 在回调中临时忽略租户过滤

**重要**: 新增业务表时，如果该表是租户维度的，必须包含 `tenant_id bigint(20)` 字段；如果是平台级表，需要在 `TenantProperties.ignoreTables` 中配置。

### Auth & Permission

- Gateway（`AuthGlobalFilter`）解析 JWT 并将用户信息透传到下游 Header
- 白名单路径在 `AuthGlobalFilter.WHITE_LIST` 中配置
- 权限校验: `@RequirePermission("sys:user:add")` 注解 + `PermissionAspect` AOP
- 数据范围: `@DataScope` 注解 + `DataScopeSqlInterceptor`，基于部门 `ancestors` 字段实现层级查询
- JWT Claims 键名定义在 `SecurityConstants` 中

### Gateway Routing

路由规则: `/api/{service-name}/**` → StripPrefix=2 → 对应服务。例如 `/api/rbac/user/list` 路由到 rbac-service 的 `/user/list`。

### Database

5 个独立数据库: `platform` / `rbac` / `wechat_oa` / `notify` / `workflow`

DDL 脚本: `docs/sql/init.sql`（全量初始化）和各库独立的 `docs/sql/{db}.sql`

MyBatis-Plus 配置: 逻辑删除字段 `deleteFlag`（0=未删除, 1=已删除），下划线转驼峰自动映射。

### Inter-Service Communication

- **同步 Feign**: rbac → platform（配额校验），workflow → rbac（查询审批人）
- **异步 Kafka Topics**: `notification-events`（通知触发），`tenant-lifecycle`（租户状态变更），`quota-change`（配额变更）
- Feign 调用通过 `TenantFeignInterceptor` 自动传播 `X-Tenant-Id` 和内部签名

### Infrastructure (docker-compose.yml)

| 组件 | 端口 |
|------|------|
| MySQL 8.0 | 3306 |
| Redis 7 | 6379 |
| Nacos 2.2.3 | 8848 |
| Kafka (Confluent 7.5) | 9092 |
| MinIO | 9000(API) / 9001(Console) |

Nacos namespace: 通过环境变量 `NACOS_NAMESPACE` 配置，各服务额外从 Nacos 拉取配置文件（如 `gateway.yml`、`rbac-service.yml`）。
