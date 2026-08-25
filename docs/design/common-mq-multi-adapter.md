# common-mq 多 MQ 适配层设计文档

> 版本: v1.0 · 日期: 2026-08-24 · 作者: zichuan
> 状态: 设计定稿，分阶段实现中

## 1. 背景与目标

当前 saas-cloud 的消息能力直接耦合 Kafka（见 §3 现状），无统一抽象、无生产补偿、无消费幂等。本设计参考 `cn.focusmedia.central:tuk-mq:2.0-SNAPSHOT` 的可靠性模式，并在此基础上构建**真正的多 MQ 适配层**，使业务代码与底层 MQ 解耦，支持通过配置在 Kafka / RabbitMQ / RocketMQ 间切换。

**核心目标**

1. 模块命名为 `common-mq`，对外提供统一 SPI（`MessageSender` / `MessageListener`），业务代码零感知底层 MQ。
2. 一行配置 `saas.mq.type=kafka|rabbit|rocket` 切换实现。
3. 引入 tuk-mq 的两个可靠性模式：生产侧 **Outbox 本地消息表 + 补偿重试**、消费侧 **注解式幂等**。
4. 保留本项目独有的多租户 Header 传播能力（`X-Tenant-Id`）。
5. 分阶段迁移现有 6 处 Kafka 耦合点，过程行为兼容、零风险回滚。

## 2. tuk-mq 2.0 实现剖析

`tuk-mq` 名为 mq，实则 Kafka 强耦合（`KafkaService` 直接持有 `KafkaTemplate`），**并不做多 MQ 适配**。其可借鉴价值在于：

| 能力 | 实现 | 借鉴价值 |
|------|------|----------|
| 统一信封 | `MsgDetailDTO<T>` / `MsgBody<T>`：`msgId/topic/msgKey/data`，`build(...)` 静态工厂 | 高 — 作为 `MessageEnvelope` 蓝本 |
| 生产 Outbox | `MsgService.sendMessage` 先写 DB `MsgProducer`(INIT) → `KafkaService` 实投 → 置 `SEND_SUCCESS/FAIL` | 高 — 落地为 `mq_outbox` 表 |
| 补偿重试 | `RetrySendMsgService` + `RetrySendMsgJob` 扫 `queryNeedSendMsg()`(INIT/FAIL) 重投 | 高 — 落地为 `OutboxRetryJob` |
| 消费幂等 | `@MsgConsumerIdempotent` + `MsgConsumerIdempotentAspect` 按 `msgId` 落 `MsgConsumer` 去重 | 高 — 落地为 `@MqIdempotent` + `mq_consume_log` |
| DDD 分层 | `client/domain/domainobject,service,convert/tunnel/dao,mapper` | 中 — 过重，本项目按接口/适配器/reliability 三段简化 |
| 审计字段 | `MsgProducer/MsgConsumer` 自带 `createUser*/traceId/dataVersion/deleteFlag` | 高 — 与本项目 DDL 规范一致，照搬 |
| 配置 | `PropertiesConfig`(@ConfigurationProperties) + 手写 Factory | 中 — 本项目用 `MqProperties` 统一 |

**关键结论**：tuk-mq 不是多 MQ 方案，是"Kafka + 可靠性模式"。本设计**只取其两模式**，多 MQ 抽象为自建。

## 3. saas-cloud 现状（gap 分析）

### 3.1 生产侧

| 位置 | 耦合方式 |
|------|----------|
| `common-log: KafkaProducerService` | 具体类持 `KafkaTemplate<String,Object>`，无接口 |
| `common-log: OperationLogAspect` L155 | 注入 `KafkaProducerService`，发 `saas-operation-log`（Kafka 不可用降级 Spring 事件） |
| `common-log: ApiAccessLogFilter` L97 | 注入 `KafkaProducerService`，发 `saas-api-access-log` |
| `common-log: ApiErrorLogFilter` L87 | 注入 `KafkaProducerService`，发 `saas-api-error-log` |

### 3.2 消费侧

| 位置 | 耦合方式 |
|------|----------|
| `rbac-service: OperationLogConsumer` | `@KafkaListener(topic=saas-operation-log, group=rbac-service)` + `@TenantIgnore` |
| `notify-service: NotifyEventConsumer` | `@KafkaListener(topic=saas-notify-event, group=notify-service)` |
| `common-websocket: KafkaWebSocketMessageSender` | 生产 `saas-websocket-broadcast` + `@KafkaListener` 随机 group 广播消费 |

### 3.3 配置与基础设施

- `common-log: KafkaConfig`：4 个 `NewTopic` Bean、`TenantKafkaProducerInterceptor`(acks=all/幂等/retries=3)、`TenantKafkaListenerInterceptor`、`DefaultErrorHandler`(DLT, 重试 3×1s)。
- `TenantKafkaProducerInterceptor.HEADER_TENANT_ID="X-Tenant-Id"` 硬编码。
- 无 `@ConfigurationProperties`，走原生 `spring.kafka.*`；各服务 yaml 仅 `bootstrap-servers` + 序列化器。
- `common-websocket` 已有 `WebSocketMessageSender` 接口 + Kafka/Local 两实现 + `@ConditionalOnBean` 选择 —— **本设计的 SPI + 自动装配范式直接参照此先例**。
- 依赖：`spring-kafka`（BOM 管理），无 rabbit/rocket 任何坐标。

### 3.4 缺口

- 无统一 MQ 接口；无生产 outbox / 补偿；无消费幂等；`@KafkaListener` 注解硬编码无法跨 MQ。

## 4. 架构设计

### 4.1 模块结构

```
common/common-mq/
├── common-mq-api/              # 纯接口 + 信封 + 枚举 + 注解，零 MQ 坐标，所有服务可依赖
│   └── com.saas.cloud.common.mq
│       ├── MessageEnvelope<T>      # 对应 MsgDetailDTO
│       ├── SendResult             # 投递结果
│       ├── MessageSender           # 统一生产 SPI
│       ├── MessageListener<T>      # 统一消费 SPI（业务实现）
│       ├── MessageConsumer         # 消费上下文(payload/headers/ack)
│       ├── MQType enum             # KAFKA / RABBIT / ROCKET
│       ├── MqConst                 # 常量(topic 命名、header key)
│       └── annotation
│           ├── @MqConsumer          # 标记 + topic + group
│           └── @MqIdempotent        # 消费幂等标记
├── common-mq-kafka/             # Kafka 适配（复用 common-log KafkaConfig 逻辑）
│   └── com.saas.cloud.common.mq.kafka
│       ├── KafkaMessageSender
│       ├── KafkaListenerRegistrar  # @MqConsumer → MessageListenerContainer
│       └── KafkaMqAutoConfiguration
├── common-mq-rabbit/           # RabbitMQ 适配(spring-amqp)
├── common-mq-rocket/            # RocketMQ 适配(rocketmq-spring-boot-starter)
├── common-mq-reliability/       # tuk-mq 两模式落地
│   └── com.saas.cloud.common.mq.reliability
│       ├── OutboxMessageSender      # 装饰 MessageSender：先落 mq_outbox 再实投
│       ├── OutboxRetryJob           # @Scheduled 扫 INIT/FAIL 重投
│       ├── MqIdempotentAspect       # @MqIdempotent AOP：按 msgId 落 mq_consume_log 去重
│       ├── entity MqOutbox / MqConsumeLog
│       └── mapper MqOutboxMapper / MqConsumeLogMapper
└── common-mq-spring-boot-starter/  # 自动装配：按 saas.mq.type 选适配器 + 注册 listener
    └── com.saas.cloud.common.mq.autoconfigure
        ├── MqProperties               # @ConfigurationProperties("saas.mq")
        ├── MqAutoConfiguration        # 按 type 装配对应 Sender + Registrar
        └── MqConsumerRegistry         # 收集所有 @MqConsumer 的 MessageListener
```

### 4.2 核心 SPI（common-mq-api，零 MQ 依赖）

```java
/** 消息信封（对标 tuk-mq MsgDetailDTO） */
@Data
@Builder
public class MessageEnvelope<T> {
    private String msgId;     // 幂等键，缺省由发送侧 UUID 生成
    private String bizId;     // 业务标识（如订单号），便于追踪
    private String topic;
    private String msgKey;    // 分区/路由键，可空
    private T data;
    private Map<String, String> headers; // 业务自定义头（租户头由适配器自动注入，无需业务填）

    public static <T> MessageEnvelope<T> of(String topic, T data) { ... }
    public static <T> MessageEnvelope<T> of(String topic, String msgKey, T data) { ... }
}

/** 统一生产 SPI */
public interface MessageSender {
    /** 同步直投（不入 outbox，低延迟，调用方感知失败） */
    <T> SendResult send(MessageEnvelope<T> msg);
    /** 异步可靠投递（经 outbox 落库 + 后台实投，至少一次送达） */
    <T> void sendReliable(MessageEnvelope<T> msg);
}

/** 统一消费 SPI（业务实现并注册为 Bean，加 @MqConsumer） */
public interface MessageListener<T> {
    default String topic() { return null; }     // 优先取注解值
    default String group() { return null; }
    default Class<T> payloadType() { return null; }
    void onMessage(MessageEnvelope<T> msg, MessageConsumer ctx);
}

/** 消费上下文 */
public interface MessageConsumer {
    String getPayload();                    // 原始报文
    Map<String, String> getHeaders();       // 底层头（含 X-Tenant-Id）
    void ack();                             // 确认（按 MQ 模式映射）
    void nack(Throwable cause);            // 否决/重试
}
```

### 4.3 注解

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MqConsumer {
    String topic();
    String group();
    /** 并发度，0 表示用适配器默认 */
    int concurrency() default 0;
}

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MqIdempotent {
    /** 幂等键 SpEL，缺省取 envelope.msgId */
    String key() default "#msg.msgId";
    /** 幂等失效时间(秒)，0 表示永久 */
    long expireSeconds() default 0;
}
```

### 4.4 适配器约定

每个适配器只做"翻译"，不掺业务：

| 适配器 | 生产实现 | 消费实现 | 租户头传播 |
|--------|----------|----------|------------|
| Kafka | `KafkaMessageSender` 持 `KafkaTemplate`，复用 `TenantKafkaProducerInterceptor` | `KafkaListenerRegistrar` 用 `ConcurrentMessageListenerContainer`，注册 `RecordInterceptor` 还原租户 | Header `X-Tenant-Id` |
| Rabbit | `RabbitMessageSender` 持 `RabbitTemplate` | `SimpleMessageListenerContainer` | `MessageProperties.header` |
| Rocket | `RocketMessageSender` 持 `RocketMQTemplate` | `DefaultMQPushConsumer` 编程式 | `MessageProperties` Key |
| Outbox（装饰器） | `OutboxMessageSender` 包装任一 Sender：先落 `mq_outbox`(INIT) → 异步实投 → 成功置 `SEND_SUCCESS`/失败 `SEND_FAIL` | — | 透传 |

**租户还原**：每个适配器的消费解包逻辑统一调用 `MqTenantHelper.resolve(headers)`，写入 `TenantContext`，消费结束 `clear()`。Header key 常量收敛到 `MqConst.HEADER_TENANT_ID`，废弃 `TenantKafkaProducerInterceptor.HEADER_TENANT_ID` 硬编码（保留兼容引用）。

### 4.5 可靠性模式（落地到本项目规范）

#### 4.5.1 生产 Outbox

`sendReliable` 流程：生成 `msgId` → 写 `mq_outbox`(msg_status=INIT) → 线程池异步调底层 `MessageSender.send` → 成功置 `SEND_SUCCESS`/失败 `SEND_FAIL` + `retry_count++`。表结构沿用全局 DDL 审计字段（见 §6）。

#### 4.5.2 补偿重试 Job

`OutboxRetryJob` 用 `@Scheduled(cron=saas.mq.outbox.retry-cron)` 扫描 `msg_status IN (INIT, SEND_FAIL) AND next_retry_time <= now()`，限流批量重投。重试上限 `saas.mq.outbox.max-retry`，超限置 `SEND_GIVE_UP` 并告警。

#### 4.5.3 消费幂等

`@MqIdempotent` AOP 切 `MessageListener.onMessage`：按 SpEL 取幂等键 → 查/插 `mq_consume_log`(唯一键 `msg_id`)；命中即跳过；执行成功置 `CONSUME_SUCCESS`、异常置 `CONSUME_FAIL` 走重试/DLT。

## 5. 配置设计

```yaml
saas:
  mq:
    type: kafka                    # kafka | rabbit | rocket
    outbox:
      enabled: false               # 默认关，生产侧走同步直投；开启后 send→sendReliable
      retry-cron: "0 */1 * * * ?" # 补偿扫描
      max-retry: 10
      batch-size: 200
    idempotent:
      enabled: true
  # 各适配器原生配置，仅 type 匹配时生效
  kafka:
    bootstrap-servers: ${KAFKA_ADDR:localhost:9092}
    producer: { key-serializer: StringSerializer, value-serializer: StringSerializer }
  rabbit:
    addresses: ${RABBIT_ADDR:localhost:5672}
    virtual-host: /
  rocket:
    name-server: ${ROCKET_ADDR:localhost:9876}
```

Topic 常量收敛到 `MqConst`（搬运 `KafkaConfig` 的 4 个常量 + websocket 的 `saas-websocket-broadcast`），保持原 topic 名不变，确保迁移期新旧消费者兼容。

## 6. 数据库 DDL（追加到 docs/sql/init.sql）

独立 `mq` 库（或并入 `notify` 库，待定；建议独立库与 tuk-mq 的 MsgProducer/MsgConsumer 对应）。

```sql
-- 生产 outbox（对标 tuk-mq MsgProducer）
CREATE TABLE mq_outbox (
  id            bigint AUTO_INCREMENT PRIMARY KEY,
  msg_id        varchar(64)  NOT NULL COMMENT '消息唯一ID(幂等键)',
  biz_id        varchar(64)  COMMENT '业务标识',
  topic         varchar(128) NOT NULL,
  msg_key       varchar(128) COMMENT '分区/路由键',
  payload       text         NOT NULL COMMENT '序列化报文',
  msg_status    int          NOT NULL DEFAULT 0 COMMENT '0 INIT 1 SEND_SUCCESS 2 SEND_FAIL 3 SEND_GIVE_UP',
  retry_count   int          NOT NULL DEFAULT 0,
  next_retry_time timestamp  COMMENT '下次重试时间',
  -- 审计字段（全局 DDL 规范）
  create_user_id varchar(64), create_user_no varchar(64), create_user_name varchar(64),
  create_time    timestamp DEFAULT CURRENT_TIMESTAMP,
  update_user_id varchar(64), update_user_no varchar(64), update_user_name varchar(64),
  update_time    timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  delete_flag    int NOT NULL DEFAULT 0, valid_status int NOT NULL DEFAULT 1,
  trace_id varchar(255), remark varchar(255), data_version int NOT NULL DEFAULT 0,
  UNIQUE KEY uk_msg_id (msg_id),
  KEY idx_status_retry (msg_status, next_retry_time)
) COMMENT 'MQ 生产 outbox';

-- 消费幂等日志（对标 tuk-mq MsgConsumer）
CREATE TABLE mq_consume_log (
  id            bigint AUTO_INCREMENT PRIMARY KEY,
  msg_id        varchar(64)  NOT NULL COMMENT '消息唯一ID(幂等键)',
  topic         varchar(128) NOT NULL,
  group_id      varchar(128) NOT NULL,
  consume_status int NOT NULL DEFAULT 0 COMMENT '0 INIT 1 CONSUME_SUCCESS 2 CONSUME_FAIL',
  error_msg     text,
  -- 审计字段同上
  ...
  UNIQUE KEY uk_msg_group (msg_id, group_id)
) COMMENT 'MQ 消费幂等日志';
```

## 7. 迁移步骤（分阶段，每阶段可独立回滚）

| 阶段 | 动作 | 风险 | 验收 |
|------|------|------|------|
| P1 | 建 `common-mq` 模块骨架 + api（SPI/信封/注解/枚举） | 无（新增模块） | `mvn -pl common/common-mq -am compile` 通过 |
| P2 | 实现 Kafka 适配器（复用 `KafkaConfig` 拦截器/ErrorHandler）+ starter 自动装配 | 无（新增） | KafkaSender 发送、`@MqConsumer` 注册消费，行为等价原 `@KafkaListener` |
| P3 | 生产侧迁移：`KafkaProducerService` 实现 `MessageSender`；3 处 Filter/Aspect 改注入 `MessageSender` | 低（接口化，行为不变） | 操作日志/API 日志正常入 Kafka |
| P4 | 消费侧迁移：rbac/notify/websocket 三处 `@KafkaListener` → `@MqConsumer` | 低（starter 内仍转 Kafka 等价） | 三消费链路回归通过 |
| P5 | 可靠性增强：建 `mq_outbox`/`mq_consume_log` 表；`@MqIdempotent` 接入三 listener；outbox 默认关可选开 | 中（涉及 DB） | 重复消息被去重；outbox 开启后断 MQ 期间消息不丢 |
| P6 | Rabbit/Rocket 适配器实现 + 切换验证 | 中 | `saas.mq.type=rabbit` 全链路通过 |

> P3 起删除 `common-log: KafkaConfig` 的重复 spring.factories 注册（与 AutoConfiguration.imports 重复）。

## 8. 关键取舍

1. **不照搬 tuk-mq 五层 DDD**：通用 starter 过重，按本项目 common 惯例三段（api/适配器/reliability）即可。
2. **多租户传播为本项目独有**：必须进每个适配器解包层，tuk-mq 无此概念，不能漏。
3. **`@KafkaListener` 无法跨 MQ**：本项目 `@MqConsumer` + 注册表方案比注解硬编码干净；RocketMQ 需用 `DefaultMQPushConsumer` 编程式注册以统一模型。
4. **outbox 默认关闭**：避免无谓 DB 写入，仅对"不能丢"的业务（如通知触发）按需开启。
5. **topic 名不变**：迁移期新旧消费者可并存，保证灰度安全。

## 9. 依赖坐标（待加根 pom.xml dependencyManagement）

```xml
<!-- Spring Boot 3 BOM 已含 spring-kafka；rabbit/rocket 需显式 -->
<dependency>
  <groupId>org.springframework.amqp</groupId>
  <artifactId>spring-rabbit</artifactId>
</dependency>
<dependency>
  <groupId>org.apache.rocketmq</groupId>
  <artifactId>rocketmq-spring-boot-starter</artifactId>
  <version>2.3.3</version>
</dependency>
```

## 10. 实现进度

- **P1~P8（多底座全链路）已实现并编译通过**（本次落地，JDK 17）：
  - `common-mq` 模块：`common-mq-api`、`common-mq-kafka`、`common-mq-rabbit`、`common-mq-rocket`、`common-mq-reliability`、`common-mq-spring-boot-starter`。
  - **生产侧**：发送器 Bean 统一 `delegateMessageSender`（Outbox 装饰器与底座无关）；`KafkaProducerService` 已删，3 处 Filter/Aspect + ApiLogAutoConfiguration 注入 `MessageSender`。
  - **消费侧**：各适配器 `*ListenerRegistrar` 扫 `@MqConsumer` 的 `MessageListener` Bean 建容器替代各 MQ 原生注解（`@KafkaListener`/`@RabbitListener`/`@RocketMQMessageListener`）；rbac `OperationLogConsumer`、notify `NotifyEventConsumer` 已迁移。
  - **可靠性**：`OutboxMessageSender`(@Primary,opt-in) + `OutboxRetryJob` + `MqIdempotentAspect`；`mq_outbox`/`mq_consume_log` DDL 入 init.sql 全 5 库；DB 操作 `TenantContext.executeWithoutTenant` 绕租户。
  - **多底座切换**（`@ConditionalOnProperty(saas.mq.type)` 互斥）：
    - Kafka：`matchIfMissing=true`（默认）。`KafkaMessageSender` + `KafkaListenerRegistrar`（`ConcurrentMessageListenerContainer`，复用 `CommonErrorHandler` 死信/重试）。
    - Rabbit：`type=rabbit` + `@ConditionalOnClass(RabbitTemplate)`。`RabbitMessageSender`（默认交换机→同名队列）+ `RabbitListenerRegistrar`（`SimpleMessageListenerContainer`，多实例共享队列=竞争消费）+ `RabbitMessageListenerAdapter`。starter optional，切 Rabbit 服务显式引入 + 配 `spring.rabbitmq.*`。
    - Rocket：`type=rocket` + `@ConditionalOnClass(RocketMQTemplate)`。`RocketMessageSender`（原生 `DefaultMQProducer.send`，msgId/bizId/tenantId 显式 userProperty）+ `RocketListenerRegistrar`（`DefaultMQPushConsumer` 编程式，CLUSTERING 竞争 / BROADCASTING 广播，nameserver 取自 producer 或 `RocketMQProperties`）+ `RocketMessageListenerAdapter`（`MessageListenerConcurrently`，失败返回 RECONSUME_LATER）。starter optional，切 Rocket 服务显式引入 + 配 `rocketmq.name-server`。`rocketmq-spring-boot-starter` 2.3.3 已入根 BOM。
  - **广播模式**：`@MqConsumer(broadcast=true)` —— Kafka 每实例随机 group、Rabbit 每实例独立队列、Rocket `MessageModel.BROADCASTING`。websocket `MqWebSocketMessageSender`（替代旧 `KafkaWebSocketMessageSender`）生产侧切 `MessageSender`、消费侧 `@MqConsumer(broadcast=true)`，全底座通用。
  - 验证：`common-mq` 全 6 子模块 + `common-websocket` + `rbac/notify/platform/wechat-oa/gateway` `mvn compile` 通过。Rabbit/Rocket 消费侧仅编译验证，运行时需真实 broker 冒烟（Rocket 的 SB3 兼容性已确认 2.3.3 可解析/编译，启动冒烟建议在接入 broker 时做）。
- **后续可选**：① Rabbit/Rocket 接真实 broker 做启动冒烟；② `KafkaConfig.TOPIC_*` 常量现已被 `MqConst` 取代，可择机清理（`KafkaConfig` 的 NewTopic/customizer/errorHandler 仍被 Kafka 注册表复用，保留）。
- **切换示例**：Kafka→Rabbit：①加 `common-mq-rabbit` 依赖；②`saas.mq.type=rabbit`；③`spring.rabbitmq.*`。业务代码（`@MqConsumer`/`MessageSender`）零改动。
- **构建命令**：`export JAVA_HOME="C:/Program Files/Java/jdk-17"` 后 `mvn -pl common/common-mq -am install -DskipTests`。
