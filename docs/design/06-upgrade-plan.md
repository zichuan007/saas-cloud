# SaaS Cloud 技术栈升级计划

> 文档版本：V1.1 | 编写日期：2026-05-21 | 作者：saas-cloud
>
> **阶段一（框架核心升级）**：已完成 - 25 模块全量编译通过
> **阶段二（安全加固 & 基础设施增强）**：已完成 - XSS 防护、Redisson、FastExcel、数据字典、AJ-Captcha、Sentinel、Sa-Token 全部集成
> **阶段三（可观测性 & 运维能力）**：已完成 - Micrometer Tracing、Actuator+Prometheus、XXL-Job、SMS4J 全部集成
> **阶段四（文件管理 & 代码生成器升级）**：已完成 - sys_file 文件管理+MinIO 租户隔离、代码生成器 FieldInfo 补全 nullable/length

## 1. 升级总览

### 1.1 升级目标

将 SaaS Cloud 从 Java 11 / Spring Boot 2.7 技术栈全面升级至 Java 17 / Spring Boot 3.4 技术栈，同时引入安全加固、基础设施增强、可观测性等缺失能力，对齐主流开源 SaaS 平台（ruoyi-vue-pro、lamp-cloud）的能力水位。

### 1.2 版本对照表

| 组件 | 当前版本 | 目标版本 | 变更说明 |
|------|---------|---------|---------|
| **Java** | 11 | 17 | LTS 升级，启用 text blocks、sealed classes 等特性 |
| **Spring Boot** | 2.7.18 | 3.4.5 | 最新稳定版，javax→jakarta 命名空间迁移 |
| **Spring Cloud** | 2021.0.8 | 2024.0.3 | 对齐 Spring Boot 3.4 |
| **Spring Cloud Alibaba** | 2021.0.5.0 | 2023.0.3.4 | 支持 Spring Boot 3.x |
| **MyBatis-Plus** | 3.5.5 | 3.5.16 | 改用 mybatis-plus-spring-boot3-starter |
| **Flowable** | 6.8.1 | 7.1.0 | 原生支持 Jakarta EE，ACT_* 表自动升级 |
| **JJWT** | 0.11.5 | 0.12.6 | API 小幅调整（parserBuilder→parser） |
| **Hutool** | 5.8.25 | 5.8.36 | 小版本升级，无破坏性变更 |
| **Lombok** | 1.18.30 | 1.18.36 | 支持 Java 17+ |
| **MapStruct** | 1.5.5.Final | 1.6.3 | 支持 Java 17+ |
| **MinIO** | 8.5.7 | 8.5.17 | 小版本升级 |
| **Knife4j** | 4.3.0 (openapi2) | 4.5.0 (openapi3-jakarta) | 切换到 Jakarta 版本 |
| **TTL** | 2.14.3 | 2.14.5 | 兼容性升级 |
| **MySQL Connector** | 8.0.33 (mysql-connector-java) | 8.4.0 (mysql-connector-j) | Maven artifact 重命名 |
| **Druid** | 1.2.20 | 1.2.24 | 支持 Jakarta |
| **JSqlParser** | 4.6 | 5.0 | MyBatis-Plus 3.5.16 依赖 |
| **Maven Compiler Plugin** | 3.11.0 | 3.13.0 | 支持 Java 17 |

### 1.3 新增组件

| 组件 | 版本 | 用途 | 所属模块 |
|------|------|------|---------|
| **Sa-Token** | 1.44.0 | 替代自定义 JWT 认证体系 | common-security |
| **Sentinel** | 1.8.8 | 限流熔断降级 | 新增 common-sentinel |
| **AJ-Captcha** | 1.4.0 | 行为验证码（滑块/点选） | rbac-service |
| **FastExcel** | 1.3.0 | Excel 导入导出（EasyExcel 继任） | 新增 common-excel |
| **XXL-Job** | 3.1.1 | 分布式定时任务 | 新增 common-job |
| **SMS4J** | 3.3.3 | 多通道短信发送 | notify-service |
| **Redisson** | 3.40.2 | 分布式锁、限流器、布隆过滤器 | common-redis |
| **Jsoup** | 1.18.3 | HTML/XSS 防护 | common-core |
| **Micrometer Tracing** | 1.4.x (Boot 管理) | 分布式链路追踪 | common-log |
| **Zipkin Reporter** | Boot 管理 | 链路追踪上报 | common-log |

### 1.4 数据库变更摘要

| 变更类型 | 具体内容 |
|---------|---------|
| 新增数据库 | `xxl_job`（XXL-Job 调度中心） |
| 新增表 | `sys_dict_type` + `sys_dict_data`（rbac 库，数据字典） |
| 新增表 | `sys_sms_log`（notify 库，短信发送记录） |
| 新增表 | `sys_captcha_config`（rbac 库，验证码配置） |
| 新增表 | `sys_file`（platform 库，文件管理记录） |
| 字段变更 | `sys_user` 增加 `phone` varchar(20)、`avatar` varchar(500) |
| Flowable | ACT_* 表由 Flowable 7 自动迁移，无需手动干预 |
| 现有表 | 26 张业务表结构**不变** |

---

## 2. 分阶段实施计划

### 阶段一：框架核心升级（Foundation）

**目标**：完成 Spring Boot 3.4 迁移，项目可编译通过并正常启动。

#### Step 1.1 — 根 POM 版本升级

**改动文件**：`pom.xml`（根）

- `java.version` 11 → 17
- `maven.compiler.source/target` 11 → 17
- `spring-boot.version` 2.7.18 → 3.4.5
- `spring-cloud.version` 2021.0.8 → 2024.0.3
- `spring-cloud-alibaba.version` 2021.0.5.0 → 2023.0.3.4
- `mybatis-plus.version` 3.5.5 → 3.5.16
- `flowable.version` 6.8.1 → 7.1.0
- `jjwt.version` 0.11.5 → 0.12.6
- `hutool.version` 5.8.25 → 5.8.36
- `lombok.version` 1.18.30 → 1.18.36
- `mapstruct.version` 1.5.5.Final → 1.6.3
- `minio.version` 8.5.7 → 8.5.17
- `knife4j.version` 4.3.0 → 4.5.0
- `transmittable-thread-local.version` 2.14.3 → 2.14.5
- `mysql.version` 8.0.33 → 8.4.0
- `druid.version` 1.2.20 → 1.2.24
- `jsqlparser.version` 4.6 → 5.0
- 新增 `sa-token.version` = 1.44.0
- 新增 `redisson.version` = 3.40.2
- 新增 `sentinel.version` = 1.8.8
- 新增 `fastexcel.version` = 1.3.0
- 新增 `xxl-job.version` = 3.1.1
- 新增 `sms4j.version` = 3.3.3
- 新增 `aj-captcha.version` = 1.4.0
- 新增 `jsoup.version` = 1.18.3
- Maven Compiler Plugin 3.11.0 → 3.13.0
- `mybatis-plus-boot-starter` → `mybatis-plus-spring-boot3-starter`
- `mysql-connector-java` → `mysql-connector-j`（groupId 变为 `com.mysql`）
- `knife4j-openapi2-spring-boot-starter` → `knife4j-openapi3-jakarta-spring-boot-starter`
- 新增 `dependencyManagement` 条目：Sa-Token、Redisson、Sentinel、FastExcel、XXL-Job、SMS4J、AJ-Captcha、Jsoup

#### Step 1.2 — javax → jakarta 命名空间迁移

**影响范围统计**：48 个 Java 文件，72 处 import

| javax 包 | 替换为 | 文件数 | import 数 |
|----------|--------|--------|----------|
| `javax.validation.*` | `jakarta.validation.*` | 40 | 55 |
| `javax.servlet.*` | `jakarta.servlet.*` | 7 | 12 |
| `javax.annotation.PostConstruct` | `jakarta.annotation.PostConstruct` | 3 | 3 |
| `javax.crypto.*` | **不变**（属于 JDK，不在 Jakarta 迁移范围） | 2 | 2 |

**关键说明**：`javax.crypto.SecretKey` 属于 JDK 标准库，不需要迁移。只有 Java EE（现 Jakarta EE）的包才需要。

**额外注意**：
- `code-generator/src/main/resources/templates/*.vm` 代码模板也需同步更新
- `rbac-service/pom.xml` 中 `javax.validation:validation-api` 显式依赖需移除（Spring Boot 3 自带 `jakarta.validation`）

#### Step 1.3 — MyBatis-Plus 3.5.16 适配

**改动文件**：
- `common/common-data/pom.xml`：`mybatis-plus-boot-starter` → `mybatis-plus-spring-boot3-starter`
- 显式移除 `jsqlparser` 依赖（MyBatis-Plus 3.5.16 内部管理）
- 检查 `TenantLineHandlerImpl` API 兼容性（3.5.16 `getTenantId()` 返回类型变更）

#### Step 1.4 — Knife4j OpenAPI3 Jakarta 迁移

**改动文件**：
- 根 `pom.xml` dependencyManagement：`knife4j-openapi2-spring-boot-starter` → `knife4j-openapi3-jakarta-spring-boot-starter`
- 各服务 Swagger 配置类：Swagger 2 注解（`@Api`、`@ApiOperation`）→ OpenAPI 3 注解（`@Tag`、`@Operation`）
- 所有 Controller 和 DTO 上的 Swagger 注解需批量替换

#### Step 1.5 — MySQL Connector 迁移

**改动文件**：
- 根 `pom.xml`：`mysql:mysql-connector-java` → `com.mysql:mysql-connector-j`
- `common/common-data/pom.xml`：同步修改 artifactId
- 各服务 `pom.xml` 中引用的同步修改
- 驱动类名：`com.mysql.cj.jdbc.Driver`（8.x 版本驱动类名不变）

#### Step 1.6 — JJWT 0.12 API 适配

**影响文件**：
- `gateway/AuthGlobalFilter.java`
- `common-security/JwtUtils.java`

**API 变更**：
```java
// 旧 (0.11.x)
Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
Jwts.builder().setSubject(username).setClaims(claims)...

// 新 (0.12.x)
Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
Jwts.builder().subject(username).claims(claims)...
```

#### Step 1.7 — Flowable 7.1.0 升级

**改动文件**：
- 根 `pom.xml`：`flowable-spring-boot-starter-process` 版本升级
- Flowable 7 原生支持 Jakarta，无需额外桥接
- `application.yml`：确认 `flowable.database-schema-update=true`，ACT_* 表自动迁移
- 检查 `workflow-service` 中自定义 Flowable API 调用的兼容性

#### Step 1.8 — Spring Cloud Gateway 适配

**要点**：
- Spring Cloud Gateway 在 Spring Boot 3.x 中无重大 API 变更
- `AuthGlobalFilter` 中 `ServerWebExchange` API 不变
- `TenantSecurityFilter` 同步检查
- `bootstrap.yml` / `application.yml` 中 Nacos 配置文件引入方式可能调整（`spring.config.import`）

#### Step 1.9 — Nacos 配置适配

**要点**：
- Spring Cloud Alibaba 2023.0.x 要求 `spring.config.import=nacos:` 方式引入配置
- 移除 `bootstrap.yml`/`bootstrap.properties` 依赖，改用 `application.yml` + `spring.config.import`
- 或者显式引入 `spring-cloud-starter-bootstrap` 保持 bootstrap 模式

#### Step 1.10 — 编译验证 & 烟雾测试

- `mvn clean compile -DskipTests` 全量编译通过
- 各服务可正常启动并注册到 Nacos
- Gateway 路由转发正常
- 基础 CRUD 接口联调通过

---

### 阶段二：安全加固 & 基础设施增强（Hardening）

**目标**：引入 Sa-Token 替代自定义 JWT、Sentinel 限流、验证码、数据字典等核心能力。

#### Step 2.1 — Sa-Token 集成（替代自定义 JWT 认证）

**新增/修改模块**：common-security、gateway、rbac-service

**方案**：
- Gateway 集成 `sa-token-reactor-spring-boot3-starter`（响应式网关鉴权）
- 各业务服务集成 `sa-token-spring-boot3-starter`
- 利用 Sa-Token 的 `StpUtil` 替代当前手动解析 JWT Claims
- Redis 集成：`sa-token-redis-jackson`，token 信息存储 Redis
- 多租户适配：自定义 `SaTokenDao` 实现带 `tenantId` 前缀的 key
- 保留当前 `SecurityConstants` 中的 header 常量，确保前端兼容
- `AuthGlobalFilter` 逐步重构为 Sa-Token 路由拦截模式

**迁移策略（双轨过渡）**：
1. 先引入 Sa-Token 依赖，新写 Sa-Token 登录接口
2. 旧 JWT 接口保留但标记 `@Deprecated`
3. 前端切换后移除旧代码

#### Step 2.2 — Sentinel 限流熔断

**新增模块**：`common/common-sentinel`

**内容**：
- 引入 `spring-cloud-starter-alibaba-sentinel`
- Gateway 层集成 Sentinel 网关限流（`spring-cloud-alibaba-sentinel-gateway`）
- 按 `tenantId` 维度限流（自定义 `RequestOriginParser`）
- 支持 Nacos 动态规则推送
- Feign 集成 Sentinel 降级（替代当前 fallbackFactory 手动实现）

#### Step 2.3 — AJ-Captcha 验证码

**改动模块**：rbac-service

**内容**：
- 引入 `aj-captcha-sdk` + `aj-captcha-spring-boot-starter`
- 登录接口增加滑块/点选验证码校验
- 验证码存储使用 Redis
- 新增 `sys_captcha_config` 配置表

#### Step 2.4 — 数据字典

**改动模块**：rbac-service（或 platform-service）

**内容**：
- 新增 `sys_dict_type`、`sys_dict_data` 表
- CRUD + Redis 缓存
- 提供 `@DictFormat` 注解，VO 返回时自动翻译字典值
- 前端对接字典 API

#### Step 2.5 — FastExcel 导入导出

**新增模块**：`common/common-excel`

**内容**：
- 引入 `cn.idev.excel:fastexcel` 1.3.0
- 封装通用导出工具（ExcelUtils），支持流式下载
- 用户列表、操作日志等场景接入
- 提供 `@ExcelProperty` 注解示例

#### Step 2.6 — Redisson 分布式锁升级

**改动模块**：common-redis

**内容**：
- 引入 `redisson-spring-boot-starter` 3.40.2
- 封装 `@DistributedLock` 注解 + AOP
- 替代当前 RedisTemplate 手写 SETNX 锁
- 提供 Redisson 限流器（RRateLimiter）工具
- 幂等性注解 `@Idempotent`（基于 Redisson）

#### Step 2.7 — XSS 防护

**改动模块**：common-core 或 common-security

**内容**：
- 引入 Jsoup 1.18.3
- 实现 `XssFilter`（全局 HTTP 请求体 HTML 清洗）
- `@Xss` 注解用于标注需要 XSS 校验的 String 字段
- JSON 反序列化自定义（Jackson XssStringDeserializer）

---

### 阶段三：可观测性 & 运维能力（Observability）

**目标**：建立完整的链路追踪、健康监控、分布式任务调度能力。

#### Step 3.1 — Micrometer Tracing + Zipkin 链路追踪

**改动模块**：common-log、各服务

**内容**：
- 引入 `micrometer-tracing-bridge-brave` + `zipkin-reporter-brave`
- 替代当前 MDC 手动 traceId 注入
- Feign/Kafka/Redis 自动传播 traceId
- Gateway 层添加 traceId 到响应头
- docker-compose.yml 添加 Zipkin 服务

#### Step 3.2 — Spring Boot Actuator + Prometheus

**改动模块**：各服务 `application.yml`

**内容**：
- 引入 `spring-boot-starter-actuator` + `micrometer-registry-prometheus`
- 暴露 `/actuator/health`、`/actuator/prometheus` 端点
- 自定义业务指标（登录次数、租户活跃数等）
- Gateway 白名单放行 actuator 端点
- docker-compose.yml 添加 Prometheus + Grafana

#### Step 3.3 — XXL-Job 分布式定时任务

**新增模块**：`common/common-job`

**内容**：
- 引入 `xxl-job-core` 3.1.1
- 封装自动注册配置（XxlJobConfig）
- docker-compose.yml 添加 XXL-Job Admin
- 新增 `xxl_job` 数据库（XXL-Job 官方 SQL）
- 迁移现有 `@Scheduled` 任务到 XXL-Job

#### Step 3.4 — SMS4J 多通道短信

**改动模块**：notify-service

**内容**：
- 引入 `org.dromara.sms4j:sms4j-spring-boot-starter` 3.3.3
- 替代当前 `notify_channel_config` 中的自定义短信发送逻辑
- 支持阿里云、腾讯云、华为云等多通道
- 新增 `sys_sms_log` 表记录发送日志

---

### 阶段四：文件管理 & 代码生成器升级（Enhancement）

#### Step 4.1 — 文件管理增强

**改动模块**：platform-service、common-storage

**内容**：
- 新增 `sys_file` 表记录文件元信息
- MinIO 上传增加租户隔离（bucket 按 tenantId 分区或路径前缀隔离）
- 文件预览 URL 签名
- 文件清理定时任务

#### Step 4.2 — 代码生成器适配 Spring Boot 3

**改动模块**：code-generator

**内容**：
- 模板文件（`.vm`）中 `javax.*` → `jakarta.*`
- 生成的代码默认使用 OpenAPI 3 注解
- 生成的 Service/Controller 遵循新的 Sa-Token 认证模式

---

## 3. javax → jakarta 迁移详细清单

### 3.1 需迁移（Java EE → Jakarta EE）

| 原包名 | 新包名 | 影响文件数 |
|--------|--------|-----------|
| `javax.validation.Valid` | `jakarta.validation.Valid` | 8 |
| `javax.validation.constraints.*` | `jakarta.validation.constraints.*` | 32 |
| `javax.servlet.FilterChain` | `jakarta.servlet.FilterChain` | 1 |
| `javax.servlet.ServletException` | `jakarta.servlet.ServletException` | 1 |
| `javax.servlet.ServletRequest` | `jakarta.servlet.ServletRequest` | 1 |
| `javax.servlet.ServletResponse` | `jakarta.servlet.ServletResponse` | 1 |
| `javax.servlet.http.HttpServletRequest` | `jakarta.servlet.http.HttpServletRequest` | 4 |
| `javax.servlet.http.HttpServletResponse` | `jakarta.servlet.http.HttpServletResponse` | 1 |
| `javax.annotation.PostConstruct` | `jakarta.annotation.PostConstruct` | 3 |

### 3.2 不迁移（JDK 标准库）

| 包名 | 原因 |
|------|------|
| `javax.crypto.SecretKey` | JDK 内置密码学 API，不属于 Jakarta EE |

---

## 4. 风险评估 & 缓解措施

| 风险 | 等级 | 缓解措施 |
|------|------|---------|
| javax→jakarta 遗漏导致运行时 ClassNotFoundException | 高 | 全量 Grep 扫描 + 编译验证 + 启动烟雾测试 |
| MyBatis-Plus 3.5.16 TenantLineHandler API 变更 | 中 | 查看 changelog 确认兼容性，编写单元测试 |
| Spring Cloud Alibaba 2023.0.x Nacos 配置加载方式变更 | 中 | 优先使用 `spring.config.import` 模式 |
| Flowable 7 ACT_* 表自动迁移失败 | 低 | 先在测试环境验证，保留数据库备份 |
| JJWT 0.12 API 变更导致 token 不兼容 | 中 | JWT 密钥和 Claims 结构不变，仅 API 调用变更 |
| Knife4j OpenAPI3 注解批量替换遗漏 | 低 | 启动后访问 doc.html 验证文档完整性 |
| Sa-Token 与现有前端 token 格式不兼容 | 中 | 双轨过渡期，前端分批迁移 |
| 第三方库间版本冲突 | 中 | 逐步引入，每步编译验证 |

---

## 5. 实施顺序 & 依赖关系

```
Step 1.1 (POM版本)
  ├── Step 1.2 (javax→jakarta)
  ├── Step 1.3 (MyBatis-Plus)
  ├── Step 1.4 (Knife4j)
  ├── Step 1.5 (MySQL Connector)
  ├── Step 1.6 (JJWT)
  ├── Step 1.7 (Flowable)
  ├── Step 1.8 (Gateway)
  └── Step 1.9 (Nacos)
        └── Step 1.10 (编译验证)
              ├── Step 2.1 (Sa-Token)
              ├── Step 2.2 (Sentinel)
              ├── Step 2.3 (Captcha)
              ├── Step 2.4 (数据字典)
              ├── Step 2.5 (FastExcel)
              ├── Step 2.6 (Redisson)
              └── Step 2.7 (XSS防护)
                    ├── Step 3.1 (链路追踪)
                    ├── Step 3.2 (Actuator)
                    ├── Step 3.3 (XXL-Job)
                    └── Step 3.4 (SMS4J)
                          ├── Step 4.1 (文件管理)
                          └── Step 4.2 (代码生成器)
```

**关键路径**：Step 1.1 → 1.2 → 1.3 → 1.10（编译通过后才能进入阶段二）

---

## 6. 文件影响矩阵

| 模块 | 阶段一 | 阶段二 | 阶段三 | 阶段四 |
|------|--------|--------|--------|--------|
| pom.xml（根） | 重大 | 中 | 小 | — |
| common-core | 中（javax→jakarta） | 中（XSS） | — | — |
| common-security | 重大（javax→jakarta + JJWT） | 重大（Sa-Token） | — | — |
| common-data | 中（MyBatis-Plus） | — | — | — |
| common-redis | 小 | 中（Redisson） | — | — |
| common-feign | 小 | 小（Sentinel） | 小（tracing） | — |
| common-kafka | 小 | — | 小（tracing） | — |
| common-log | 小（javax→jakarta） | — | 中（tracing） | — |
| common-storage | 小 | — | — | 中 |
| 新增 common-sentinel | — | 新建 | — | — |
| 新增 common-excel | — | 新建 | — | — |
| 新增 common-job | — | — | 新建 | — |
| gateway | 中（JJWT + Gateway） | 中（Sa-Token + Sentinel） | 小 | — |
| rbac-service | 中（javax→jakarta） | 重大（Sa-Token + Captcha + Dict） | 小 | — |
| platform-service | 小 | 小 | 小 | 中 |
| workflow-service | 中（Flowable 7） | — | 小 | — |
| notify-service | 小 | — | 中（SMS4J） | — |
| wechat-oa-service | 小 | — | 小 | — |
| code-generator | 中（模板 javax→jakarta） | — | — | 中 |

---

## 7. 数据库 DDL 脚本清单

阶段一无数据库变更。以下脚本在对应阶段执行：

### 阶段二

```sql
-- docs/sql/upgrade/02-dict.sql
-- 数据字典（rbac 库）
CREATE TABLE sys_dict_type (...);
CREATE TABLE sys_dict_data (...);

-- docs/sql/upgrade/02-captcha.sql
-- 验证码配置（rbac 库）
CREATE TABLE sys_captcha_config (...);

-- docs/sql/upgrade/02-user-fields.sql
-- 用户表扩展字段（rbac 库）
ALTER TABLE sys_user ADD COLUMN phone varchar(20) DEFAULT NULL COMMENT '手机号';
ALTER TABLE sys_user ADD COLUMN avatar varchar(500) DEFAULT NULL COMMENT '头像URL';
```

### 阶段三

```sql
-- docs/sql/upgrade/03-xxl-job.sql
-- XXL-Job 调度中心库（独立数据库 xxl_job）
-- 使用 XXL-Job 官方 tables_xxl_job.sql

-- docs/sql/upgrade/03-sms-log.sql
-- 短信日志表（notify 库）
CREATE TABLE sys_sms_log (...);
```

### 阶段四

```sql
-- docs/sql/upgrade/04-file.sql
-- 文件管理表（platform 库）
CREATE TABLE sys_file (...);
```
