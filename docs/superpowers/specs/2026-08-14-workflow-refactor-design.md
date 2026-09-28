# Workflow 模块改造设计方案

> 参考项目: tuk-approval-center (DDD 5 层 + Flowable 事件驱动 + 策略模式)
> 目标: 保持 2 模块结构 (workflow-api + workflow-service)，内部实现逻辑全部对齐 tuk-approval-center

## 1. 改造范围

### 1.1 改造范围

| 项 | 决策 |
|----|------|
| 模块结构 | 保持 2 模块 (workflow-api + workflow-service)，不拆 5 模块 |
| 包内分层 | workflow-service 内部按 DDD 分包 (domain/tunnel/app) |
| 持久层 | MyBatis-Plus 改为 MyBatis XML + CQRS (CommandMapper/QueryMapper) |
| 设计模式 | 审批动作策略 + 审批人分配策略 + 任务拦截器责任链 |
| 事件驱动 | Flowable 引擎事件 → Spring Event → Kafka |
| 数据库表 | 9 张业务表对齐 tuk（去掉钉钉映射表） |
| 增量能力 | 审批操作日志、任务关联、催办记录、草稿、审批统计 |

### 1.2 不去做的

| 项 | 理由 |
|----|------|
| 钉钉集成 | saas-cloud 使用自有通知体系，钉钉替换为通用 notfiy-service |
| 拆 5 个 Maven 模块 | 保持 2 模块，部署单元不变，CI/CD 改动最小 |
| 前端 | 前端已有独立项目，不在本次范围 |

## 2. 模块结构

```
saas-cloud/services/
├── workflow-api/                          # 不变：对外接口
│   └── src/main/java/com/saas/cloud/workflow/api/
│       ├── constants/                     # [新增] 常量
│       │   ├── FlowVariableConstants     # 流程变量键名
│       │   ├── TaskRelationTypeConstants # 任务关联类型
│       │   └── MqConstants              # Kafka Topic/Tag
│       ├── dto/                           # [增强] 对齐 tuk 的 request DTO
│       │   ├── request/                  # 按功能域分目录
│       │   │   ├── ApproveRequest
│       │   │   ├── RejectRequest
│       │   │   ├── TransferRequest
│       │   │   ├── DelegateRequest
│       │   │   ├── AddSignRequest
│       │   │   ├── RecallRequest
│       │   │   ├── ResubmitRequest
│       │   │   ├── CancelRequest
│       │   │   └── UrgeRequest
│       │   ├── process/                  # 流程实例相关
│       │   │   ├── StartProcessInstanceDTO
│       │   │   └── QueryProcessInstanceDTO
│       │   ├── ProcessDefinitionCreateDTO
│       │   ├── ProcessDefinitionQueryDTO
│       │   ├── ProcessDeployDTO
│       │   ├── ProcessStartDTO
│       │   ├── NodeConfigDTO
│       │   └── mq/ApprovalMessageDTO     # Kafka 消息体
│       ├── vo/                            # [增强] 对齐 tuk 的 VO
│       │   ├── ProcessDefinitionVO
│       │   ├── ProcessDefinitionDetailVO
│       │   ├── NodeConfigVO
│       │   ├── TaskVO
│       │   ├── ApprovalRecordVO
│       │   ├── ProcessInstanceVO
│       │   ├── CcRecordVO
│       │   ├── DraftVO
│       │   └── statistics/
│       │       ├── ApprovalOverviewVO
│       │       ├── ApprovalTrendVO
│       │       ├── DepartmentStatisticsVO
│       │       └── ProcessTypeStatisticsVO
│       ├── enums/                         # [新增] 枚举
│       │   ├── ApprovalActionEnum        # 审批动作类型
│       │   ├── ApprovalStatusEnum        # 审批状态
│       │   ├── ApproveModeEnum           # 审批模式 (或签/会签/依次)
│       │   ├── AssignTypeEnum            # 审批人类型
│       │   ├── RejectModeEnum            # 驳回模式
│       │   ├── TaskRelationTypeEnum      # 任务关联类型
│       │   ├── FlowTypeEnum              # 流程类型
│       │   └── TimeoutStrategyEnum       # 超时策略
│       ├── event/                         # [新增] 领域事件 DTO
│       │   ├── ApprovalActionEvent       # 审批动作事件
│       │   ├── TaskCreatedEvent          # 任务创建事件
│       │   ├── TaskCompletedEvent        # 任务完成事件
│       │   └── ProcessCompletedEvent     # 流程完成事件
│       └── feign/                         # [增强] 按功能域拆分
│           ├── ProcessInstanceFeignClient
│           ├── TaskActionFeignClient
│           ├── TaskFeignClient
│           ├── DefinitionFeignClient
│           ├── NodeConfigFeignClient
│           ├── CcRecordFeignClient
│           ├── DraftFeignClient
│           └── ApprovalStatisticsFeignClient
│
├── workflow-service/
│   └── src/main/java/com/saas/cloud/workflow/
│       ├── WorkflowApplication.java
│       │
│       ├── config/                        # [增强] 引擎配置
│       │   └── FlowableEngineConfig      # 注册事件监听器 + 引擎参数配置
│       │
│       ├── controller/                    # [增强] 薄层，委派 DomainService
│       │   ├── ProcessInstanceController  # 流程实例 (启动/列表/详情/挂起/激活/终止)
│       │   ├── TaskActionController       # 任务动作 (审批/驳回/转办/委派/加签/拿回/批量)
│       │   ├── TaskController             # 任务查询 (待办/已办)
│       │   ├── ProcessLifecycleController # 流程生命周期 (重新提交/撤销/撤回/抄送/催办)
│       │   ├── DefinitionController       # 流程定义 (CRUD/部署/模板)
│       │   ├── NodeConfigController       # 节点配置
│       │   ├── ApprovalDetailController   # 审批详情
│       │   ├── CcRecordController         # 抄送记录
│       │   ├── DraftController            # 草稿管理
│       │   ├── ModelController            # Flowable 模型管理
│       │   ├── ApprovalStatisticsController     # 审批统计
│       │   └── ApprovalStatisticsExportController # 统计导出
│       │
│       ├── domain/                        # [新增] 核心领域层
│       │   ├── gateway/                   # 防腐层
│       │   │   ├── ProcessEngineGateway   # 接口: 封装 Flowable 全部操作
│       │   │   └── impl/
│       │   │       └── FlowableProcessEngineGateway  # 实现类
│       │   │
│       │   ├── service/                   # 领域服务
│       │   │   ├── ProcessInstanceDomainService       # 流程实例领域服务
│       │   │   ├── ProcessInstanceQueryService        # 流程实例查询
│       │   │   ├── TaskActionDomainService            # 任务动作路由
│       │   │   ├── TaskQueryService                   # 任务查询
│       │   │   ├── TaskLifecycleDomainService         # 任务生命周期
│       │   │   ├── TaskAssignmentDomainService        # 任务分配
│       │   │   ├── TaskRelationDomainService          # 任务关联
│       │   │   ├── DefinitionDomainService            # 流程定义
│       │   │   ├── ModelDomainService                 # Flowable 模型
│       │   │   ├── NodeConfigDomainService            # 节点配置
│       │   │   ├── ProcessDefExtDomainService         # 流程定义扩展
│       │   │   ├── ProcessCompletionService           # 流程完成处理
│       │   │   ├── ProcessLifecycleDomainService      # 流程生命周期
│       │   │   ├── ApprovalDetailDomainService        # 审批详情
│       │   │   ├── ApprovalStatisticsDomainService    # 审批统计
│       │   │   ├── ApprovalTimeoutDomainService       # 审批超时
│       │   │   ├── CcRecordDomainService              # 抄送
│       │   │   ├── DraftDomainService                 # 草稿
│       │   │   ├── UrgeDomainService                  # 催办
│       │   │   └── impl/                              # 实现类
│       │   │       ├── action/                        # 审批动作策略
│       │   │       │   ├── ApprovalAction             # 接口
│       │   │       │   ├── ApproveActionService       # 审批通过
│       │   │       │   ├── RejectActionService        # 驳回
│       │   │       │   ├── TransferActionService      # 转办
│       │   │       │   ├── DelegateActionService      # 委派
│       │   │       │   ├── AddSignActionService       # 加签
│       │   │       │   ├── RecallActionService        # 拿回
│       │   │       │   ├── CancelActionService        # 撤销/作废
│       │   │       │   ├── ResubmitActionService      # 重新提交
│       │   │       │   ├── CcActionService            # 抄送
│       │   │       │   ├── UrgeActionService          # 催办
│       │   │       │   └── support/
│       │   │       │       ├── ApprovalActionSupport  # 公共基类
│       │   │       │       ├── OperatorContext        # 操作人上下文
│       │   │       │       └── RecordContext          # 审批记录上下文
│       │   │       └── ... (其余领域服务实现)
│       │   │
│       │   ├── strategy/                  # 审批人分配策略
│       │   │   ├── AssignmentStrategy     # 接口
│       │   │   ├── NodeConfigAssignmentResolver  # 策略调度器
│       │   │   ├── RejectActionResolver   # 驳回策略解析器接口
│       │   │   └── impl/
│       │   │       ├── UserAssignmentStrategy
│       │   │       ├── RoleAssignmentStrategy
│       │   │       ├── DeptAssignmentStrategy
│       │   │       ├── ApprovalGroupAssignmentStrategy
│       │   │       ├── InitiatorAssignmentStrategy
│       │   │       ├── ExpressionAssignmentStrategy
│       │   │       └── ApiAssignmentStrategy
│       │   │
│       │   ├── interceptor/               # 任务拦截器责任链
│       │   │   ├── TaskInterceptor        # 接口
│       │   │   ├── TaskContext             # 上下文
│       │   │   ├── TaskInterceptorChain    # 责任链
│       │   │   ├── ProcessTaskRoutingInterceptor  # 流程路由 (order=MIN)
│       │   │   └── SameApproverAutoSkipInterceptor # 同审批人跳过 (order=100)
│       │   │
│       │   ├── event/                     # 领域事件 Spring Event
│       │   │   ├── TaskCreatedEvent
│       │   │   ├── TaskCompletedEvent
│       │   │   ├── ProcessCompletedEvent
│       │   │   └── ApprovalActionEvent
│       │   │
│       │   ├── converter/                 # DO <-> VO 转换器
│       │   │   ├── ApprovalRecordConverter
│       │   │   ├── ProcessInstanceConverter
│       │   │   ├── ProcessDefExtConverter
│       │   │   ├── NodeConfigConverter
│       │   │   ├── CcRecordConverter
│       │   │   ├── DraftConverter
│       │   │   └── TaskRelationConverter
│       │   │
│       │   └── handler/                   # 流程任务处理器 (扩展点)
│       │       ├── ProcessTaskHandler     # 接口
│       │       └── ProcessTaskHandlerRegistry  # 注册中心
│       │
│       ├── tunnel/                        # [新增] 数据访问层
│       │   ├── dataobject/                # DO 实体
│       │   │   ├── FlowApprovalRecordDO
│       │   │   ├── FlowApprovalStatisticsDO
│       │   │   ├── FlowCcRecordDO
│       │   │   ├── FlowDraftDO
│       │   │   ├── FlowNodeConfigDO
│       │   │   ├── FlowNodeCandidateDO
│       │   │   ├── FlowProcessDefExtDO
│       │   │   ├── FlowProcessInstanceExtDO
│       │   │   ├── FlowTaskRelationDO
│       │   │   └── FlowUrgeLogDO
│       │   ├── mapper/                    # MyBatis Mapper (CQRS)
│       │   │   ├── FlowApprovalRecordCommandMapper
│       │   │   ├── FlowApprovalRecordQueryMapper
│       │   │   ├── FlowApprovalStatisticsCommandMapper
│       │   │   ├── FlowApprovalStatisticsQueryMapper
│       │   │   ├── FlowCcRecordCommandMapper
│       │   │   ├── FlowCcRecordQueryMapper
│       │   │   ├── FlowDraftCommandMapper
│       │   │   ├── FlowDraftQueryMapper
│       │   │   ├── FlowNodeConfigCommandMapper
│       │   │   ├── FlowNodeConfigQueryMapper
│       │   │   ├── FlowNodeCandidateCommandMapper
│       │   │   ├── FlowNodeCandidateQueryMapper
│       │   │   ├── FlowProcessDefExtCommandMapper
│       │   │   ├── FlowProcessDefExtQueryMapper
│       │   │   ├── FlowProcessInstanceExtQueryMapper
│       │   │   ├── FlowTaskRelationCommandMapper
│       │   │   ├── FlowTaskRelationQueryMapper
│       │   │   ├── FlowUrgeLogCommandMapper
│       │   │   ├── FlowUrgeLogQueryMapper
│       │   │   └── ProcessInstanceQueryMapper
│       │   └── dao/                       # DAO 封装 (组合 CommandMapper + QueryMapper)
│       │       ├── ApprovalRecordDAO
│       │       ├── ApprovalStatisticsDAO
│       │       ├── CcRecordDAO
│       │       ├── FlowDraftDAO
│       │       ├── FlowNodeConfigDAO
│       │       ├── FlowNodeCandidateDAO
│       │       ├── FlowProcessDefExtDAO
│       │       ├── TaskRelationDAO
│       │       └── UrgeLogDAO
│       │
│       ├── app/                           # [新增] 应用层
│       │   ├── listener/                  # Flowable 事件监听器
│       │   │   ├── FlowableEventBridge          # Flowable 事件 → Spring Event
│       │   │   ├── TaskCreatedEventListener     # 任务创建 → 分配审批人
│       │   │   ├── TaskCompletedEventListener   # 任务完成 → 记录日志
│       │   │   └── ProcessCompletedEventListener # 流程完成 → 更新状态
│       │   ├── job/                       # 定时任务
│       │   │   ├── ApprovalStatisticsAggregationJob  # 审批统计聚合
│       │   │   └── ApprovalTimeoutJob              # 审批超时处理
│       │   └── service/                   # 应用服务
│       │       └── ApprovalStatisticsExportService  # 统计导出
│       │
│       └── resources/
│           └── mapper/                    # [增强] MyBatis XML (18个)
│               ├── FlowApprovalRecordCommandMapper.xml
│               ├── FlowApprovalRecordQueryMapper.xml
│               ├── FlowApprovalStatisticsCommandMapper.xml
│               ├── FlowApprovalStatisticsQueryMapper.xml
│               ├── FlowCcRecordCommandMapper.xml
│               ├── FlowCcRecordQueryMapper.xml
│               ├── FlowDraftCommandMapper.xml
│               ├── FlowDraftQueryMapper.xml
│               ├── FlowNodeConfigCommandMapper.xml
│               ├── FlowNodeConfigQueryMapper.xml
│               ├── FlowNodeCandidateCommandMapper.xml
│               ├── FlowNodeCandidateQueryMapper.xml
│               ├── FlowProcessDefExtQueryMapper.xml
│               ├── FlowProcessInstanceExtQueryMapper.xml
│               ├── FlowTaskRelationCommandMapper.xml
│               ├── FlowTaskRelationQueryMapper.xml
│               ├── FlowUrgeLogCommandMapper.xml
│               └── FlowUrgeLogQueryMapper.xml
```

## 3. 数据库表改造

### 3.1 当前 5 张表 → 改造后 9 张表

| 当前表 | 改造后表 | 变更 |
|--------|---------|------|
| `wf_process_definition_ext` | `wf_process_definition_ext` (增强) | 增加 `is_visible`、`form_url` 等字段 |
| `wf_process_instance_ext` | `wf_process_instance_ext` (增强) | 增加 `flow_type` 字段 |
| `wf_task_ext` | **废弃**，改为 `wf_approval_record` | 职责拆分：任务快照 vs 操作日志 |
| `wf_copy` | `wf_copy` | 字段微调 |
| `wf_node_config` | `wf_node_config` (增强) | 增加 `reject_mode`、`timeout_*`、`same_approver_skip` 字段 |
| — | `wf_node_candidate` (新增) | 候选人来源从 node_config 拆出 |
| — | `wf_approval_record` (新增) | 审批操作日志 (替代 wf_task_ext) |
| — | `wf_task_relation` (新增) | 转办/委派/加签链路追踪 |
| — | `wf_urge_log` (新增) | 催办记录 |
| — | `wf_draft` (新增) | 流程草稿 |
| — | `wf_approval_statistics` (新增) | 审批统计聚合 |

### 3.2 核心表 DDL 设计

#### wf_approval_record (审批操作记录 — 核心审计表)

```sql
CREATE TABLE wf_approval_record (
    id              BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '主键',
    -- 租户
    tenant_id       BIGINT(20)   NOT NULL COMMENT '租户ID',
    -- 流程实例
    process_instance_id VARCHAR(64) NOT NULL COMMENT 'Flowable流程实例ID',
    process_def_key VARCHAR(128) NOT NULL COMMENT '流程定义Key',
    -- 任务
    task_id         VARCHAR(64)  COMMENT 'Flowable任务ID',
    task_name       VARCHAR(255) COMMENT '任务名称',
    node_def_key    VARCHAR(128) COMMENT '节点定义Key',
    -- 操作人
    operator_id     BIGINT(20)   NOT NULL COMMENT '操作人ID',
    operator_name   VARCHAR(64)  NOT NULL COMMENT '操作人姓名',
    operator_dept_id BIGINT(20)  COMMENT '操作人部门ID',
    -- 操作
    action          VARCHAR(32)  NOT NULL COMMENT '动作: APPROVED/REJECTED/TRANSFER/DELEGATE/ADD_SIGN/RECALL/CANCEL/RESUBMIT/URGE',
    action_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    -- 审批意见
    comment         TEXT         COMMENT '审批意见',
    system_reason   VARCHAR(500) COMMENT '系统原因 (如自动跳过)',
    -- 流转
    source_node_key VARCHAR(128) COMMENT '来源节点Key (驳回/拿回追溯)',
    target_node_key VARCHAR(128) COMMENT '目标节点Key (驳回/跳转目标)',
    -- 任务关联
    parent_task_id  VARCHAR(64)  COMMENT '父任务ID (转办/委派/加签)',
    -- 审批策略
    approve_strategy VARCHAR(32) COMMENT '审批策略: ANY/ALL/SEQUENTIAL',
    -- 终态标记
    is_final_decision TINYINT(1) DEFAULT 0 COMMENT '是否终态决定',
    -- 审计字段
    create_user_id  BIGINT(20),
    create_user_name VARCHAR(64),
    create_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_user_id  BIGINT(20),
    update_user_name VARCHAR(64),
    update_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    delete_flag     TINYINT(1)   NOT NULL DEFAULT 0,
    valid_status    TINYINT(1)   NOT NULL DEFAULT 1,
    trace_id        VARCHAR(255),
    remark          VARCHAR(500),
    data_version    INT(11)      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_tenant_operator (tenant_id, operator_id),
    KEY idx_tenant_operator_action (tenant_id, operator_id, action),
    KEY idx_process_instance (process_instance_id),
    KEY idx_task_id (task_id),
    KEY idx_action_time (tenant_id, action_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批操作记录';
```

#### wf_node_config (节点配置 — 核心配置表)

```sql
CREATE TABLE wf_node_config (
    id              BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id       BIGINT(20)   NOT NULL COMMENT '租户ID',
    process_def_key VARCHAR(128) NOT NULL COMMENT '流程定义Key',
    node_def_key    VARCHAR(128) NOT NULL COMMENT '节点定义Key',
    node_name       VARCHAR(255) NOT NULL COMMENT '节点名称',
    -- 审批模式
    approve_mode    VARCHAR(32)  NOT NULL DEFAULT 'ANY' COMMENT 'ANY/ALL/SEQUENTIAL',
    pass_threshold  INT(11)      DEFAULT 1 COMMENT '通过阈值 (会签时有效)',
    sequential_order VARCHAR(500) COMMENT '顺签顺序 (JSON数组)',
    -- 超时配置
    timeout_enabled TINYINT(1)   DEFAULT 0 COMMENT '是否启用超时',
    timeout_hours   INT(11)      COMMENT '超时小时数',
    timeout_strategy VARCHAR(32) COMMENT '超时策略: AUTO_APPROVE/AUTO_REJECT/TRANSFER/REMIND',
    timeout_transfer_user_id BIGINT(20) COMMENT '超时转办人ID',
    -- 驳回配置
    reject_mode     VARCHAR(32)  DEFAULT 'BPMN' COMMENT 'BPMN/INITIATOR/PREVIOUS/CUSTOM',
    custom_target_node VARCHAR(128) COMMENT '自定义驳回目标节点',
    -- 特殊配置
    same_approver_skip TINYINT(1) DEFAULT 1 COMMENT '同审批人自动跳过',
    -- 审计字段
    create_user_id  BIGINT(20),
    create_user_name VARCHAR(64),
    create_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_user_id  BIGINT(20),
    update_user_name VARCHAR(64),
    update_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    delete_flag     TINYINT(1)   NOT NULL DEFAULT 0,
    valid_status    TINYINT(1)   NOT NULL DEFAULT 1,
    trace_id        VARCHAR(255),
    remark          VARCHAR(500),
    data_version    INT(11)      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_process_node (process_def_key, node_def_key, delete_flag),
    KEY idx_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程节点配置';
```

#### wf_node_candidate (候选人来源 — 与 node_config 多对一)

```sql
CREATE TABLE wf_node_candidate (
    id              BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '主键',
    tenant_id       BIGINT(20)   NOT NULL COMMENT '租户ID',
    node_config_id  BIGINT(20)   NOT NULL COMMENT '节点配置ID (关联 wf_node_config.id)',
    process_def_key VARCHAR(128) NOT NULL COMMENT '流程定义Key (冗余)',
    node_def_key    VARCHAR(128) NOT NULL COMMENT '节点定义Key (冗余)',
    assign_type     VARCHAR(32)  NOT NULL COMMENT 'USER/ROLE/DEPT/GROUP/EXPRESSION/API/INITIATOR',
    assign_value    VARCHAR(500) NOT NULL COMMENT '分配值 (用户ID/角色ID/表达式/API URL)',
    sort_order      INT(11)      DEFAULT 0 COMMENT '排序',
    -- 审计字段
    create_user_id  BIGINT(20),
    create_user_name VARCHAR(64),
    create_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_user_id  BIGINT(20),
    update_user_name VARCHAR(64),
    update_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    delete_flag     TINYINT(1)   NOT NULL DEFAULT 0,
    valid_status    TINYINT(1)   NOT NULL DEFAULT 1,
    trace_id        VARCHAR(255),
    remark          VARCHAR(500),
    data_version    INT(11)      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_node_config (node_config_id),
    KEY idx_process_node (process_def_key, node_def_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='节点候选人来源';
```

#### wf_task_relation (任务关联关系)

```sql
CREATE TABLE wf_task_relation (
    id              BIGINT(20)   NOT NULL AUTO_INCREMENT,
    tenant_id       BIGINT(20)   NOT NULL,
    task_id         VARCHAR(64)  NOT NULL COMMENT '当前任务ID',
    parent_task_id  VARCHAR(64)  COMMENT '父任务ID',
    process_instance_id VARCHAR(64) NOT NULL,
    relation_type   VARCHAR(32)  NOT NULL COMMENT 'TRANSFER/DELEGATE/COUNTER_SIGN',
    -- 审计字段
    create_user_id  BIGINT(20),
    create_user_name VARCHAR(64),
    create_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_user_id  BIGINT(20),
    update_user_name VARCHAR(64),
    update_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    delete_flag     TINYINT(1)   NOT NULL DEFAULT 0,
    valid_status    TINYINT(1)   NOT NULL DEFAULT 1,
    trace_id        VARCHAR(255),
    remark          VARCHAR(500),
    data_version    INT(11)      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_task_id (task_id),
    KEY idx_parent (parent_task_id),
    KEY idx_process_instance (process_instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务关联关系';
```

#### wf_urge_log (催办记录)

```sql
CREATE TABLE wf_urge_log (
    id              BIGINT(20)   NOT NULL AUTO_INCREMENT,
    tenant_id       BIGINT(20)   NOT NULL,
    task_id         VARCHAR(64)  NOT NULL,
    process_instance_id VARCHAR(64) NOT NULL,
    urge_user_id    BIGINT(20)   NOT NULL COMMENT '催办人ID',
    urge_user_name  VARCHAR(64)  COMMENT '催办人姓名',
    target_user_id  BIGINT(20)   NOT NULL COMMENT '被催办人ID',
    target_user_name VARCHAR(64) COMMENT '被催办人姓名',
    urge_time       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    channel         VARCHAR(32)  DEFAULT 'IN_APP' COMMENT '通知渠道',
    -- 审计字段
    create_user_id  BIGINT(20),
    create_user_name VARCHAR(64),
    create_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_user_id  BIGINT(20),
    update_user_name VARCHAR(64),
    update_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    delete_flag     TINYINT(1)   NOT NULL DEFAULT 0,
    valid_status    TINYINT(1)   NOT NULL DEFAULT 1,
    trace_id        VARCHAR(255),
    remark          VARCHAR(500),
    data_version    INT(11)      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_task (task_id),
    KEY idx_urge_user (urge_user_id),
    KEY idx_target_user (target_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='催办记录';
```

#### wf_draft (流程草稿)

```sql
CREATE TABLE wf_draft (
    id              BIGINT(20)   NOT NULL AUTO_INCREMENT,
    tenant_id       BIGINT(20)   NOT NULL,
    process_def_key VARCHAR(128) NOT NULL COMMENT '流程定义Key',
    business_key    VARCHAR(128) COMMENT '业务关联键',
    draft_content   JSON         COMMENT '草稿内容',
    -- 审计字段
    create_user_id  BIGINT(20),
    create_user_name VARCHAR(64),
    create_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_user_id  BIGINT(20),
    update_user_name VARCHAR(64),
    update_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    delete_flag     TINYINT(1)   NOT NULL DEFAULT 0,
    valid_status    TINYINT(1)   NOT NULL DEFAULT 1,
    trace_id        VARCHAR(255),
    remark          VARCHAR(500),
    data_version    INT(11)      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_business_key (tenant_id, process_def_key, business_key, delete_flag),
    KEY idx_tenant_user (tenant_id, create_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程草稿';
```

#### wf_approval_statistics (审批统计)

```sql
CREATE TABLE wf_approval_statistics (
    id              BIGINT(20)   NOT NULL AUTO_INCREMENT,
    tenant_id       BIGINT(20)   NOT NULL,
    user_id         BIGINT(20)   NOT NULL COMMENT '用户ID',
    user_name       VARCHAR(64)  COMMENT '用户姓名',
    dept_id         BIGINT(20)   COMMENT '部门ID',
    process_def_key VARCHAR(128) COMMENT '流程定义Key',
    process_def_name VARCHAR(255) COMMENT '流程定义名称',
    stat_period     VARCHAR(16)  NOT NULL COMMENT '统计周期: MONTH/QUARTER/YEAR',
    stat_date       DATE         NOT NULL COMMENT '统计日期',
    total_count     INT(11)      DEFAULT 0 COMMENT '审批总数',
    approved_count  INT(11)      DEFAULT 0 COMMENT '通过数',
    rejected_count  INT(11)      DEFAULT 0 COMMENT '驳回数',
    avg_duration_ms BIGINT(20)   DEFAULT 0 COMMENT '平均耗时(ms)',
    -- 审计字段
    create_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    delete_flag     TINYINT(1)   NOT NULL DEFAULT 0,
    valid_status    TINYINT(1)   NOT NULL DEFAULT 1,
    data_version    INT(11)      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_user_period_date_process (tenant_id, user_id, stat_period, stat_date, process_def_key),
    KEY idx_tenant_period (tenant_id, stat_period, stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批统计';
```

## 4. 核心设计模式

### 4.1 审批动作策略 (替代当前 if-else)

**当前问题**: `WfTaskExtServiceImpl` 中 approve/reject/transfer/delegate/addSign 五个方法写在一个类里，扩展新动作需要修改这个类。

**改造方案**: 策略模式，每个审批动作独立实现，通过 `ApprovalActionEnum` 自动路由。

```java
// 接口 (domain/service/impl/action/ApprovalAction.java)
public interface ApprovalAction<T> {
    ApprovalActionEnum getActionType();  // 返回动作类型枚举
    void execute(T request);             // 执行动作
}

// 9 个实现类，每个独立文件
ApproveActionService implements ApprovalAction<ApproveRequest>
RejectActionService implements ApprovalAction<RejectRequest>
TransferActionService implements ApprovalAction<TransferRequest>
DelegateActionService implements ApprovalAction<DelegateRequest>
AddSignActionService implements ApprovalAction<AddSignRequest>
RecallActionService implements ApprovalAction<RecallRequest>
CancelActionService implements ApprovalAction<CancelRequest>
ResubmitActionService implements ApprovalAction<ResubmitRequest>
UrgeActionService implements ApprovalAction<UrgeRequest>
```

**关键设计要点**:
1. `ApproveActionService` 强制覆盖路由变量 `RESULT=PASS`、`ACTION=APPROVED`，防止客户端伪造
2. `RejectActionService` 支持 4 种驳回模式: BPMN(默认流)/INITIATOR(发起人)/PREVIOUS(上一节点)/CUSTOM(自定义)
3. `RejectActionService` 的 RETURNED 模式使用 `moveActivityIdTo` 实现节点跳转
4. `RecallActionService` 前置校验下游任务未签收 + 多实例检查
5. 所有 Action 继承 `ApprovalActionSupport`，共用: 任务校验、权限校验、Flowable 操作、审计日志记录

**路由方式**: `TaskActionController` 根据请求中的 `actionType` 字段，从 Spring 容器中获取对应的 `ApprovalAction` Bean 执行。

### 4.2 审批人分配策略

**当前问题**: `WfNodeConfigServiceImpl` 仅支持 4 种固定 assigneeType，分配逻辑硬编码。

**改造方案**: 策略模式，7 种分配策略，通过 `NodeConfigAssignmentResolver` 统一调度。

```java
public interface AssignmentStrategy {
    AssignTypeEnum getType();
    List<AssigneeDTO> resolve(String assignValue, Map<String, Object> variables);
}
```

**安全设计**: `ExpressionAssignmentStrategy` 使用 `SimpleEvaluationContext`（禁止 `T()`、`java.`、`Runtime` 等），防止 SpEL 注入。

**调度逻辑**: `NodeConfigAssignmentResolver` 遍历节点的所有 `FlowNodeCandidateDO`，按 `assignType` 匹配对应策略，收集所有候选人后去重。

### 4.3 任务拦截器责任链

**用途**: 在任务创建/完成时执行横切逻辑，如自动跳过、路由分发。

```java
public interface TaskInterceptor {
    int getOrder();                        // 执行顺序，越小越先
    void onTaskCreate(TaskContext context);   // 任务创建时
    void onTaskComplete(TaskContext context); // 任务完成时
}
```

**内置实现**:
- `ProcessTaskRoutingInterceptor` (order=MIN): 根据 `processDefinitionKey` 路由到定制处理器
- `SameApproverAutoSkipInterceptor` (order=100): 上一审批人 = 当前节点候选人 → 自动跳过

**TaskContext 控制标志**:
- `skip()`: 跳过后续所有拦截器
- `skipTask()`: 跳过当前任务（自动完成）

**责任链**: `TaskInterceptorChain` 在 `@PostConstruct` 时按 order 排序并缓存，执行时遍历直至 `context.isSkip()`。

### 4.4 防腐层 (ProcessEngineGateway)

**设计意图**: 领域层零 Flowable 类型依赖。所有 Flowable API 调用通过 Gateway 接口，实现类 `FlowableProcessEngineGateway` 封装全部操作。

**覆盖方法** (30+):
- 任务: `completeTask`, `claimTask`, `delegateTask`, `setAssignee`, `addCandidateUser`, `deleteCandidateUser`
- 流程变量: `getVariables`, `setVariable`, `setVariables`, `getVariable`, `hasVariable`
- 流程实例: `startProcessInstance`, `deleteProcessInstance`, `suspendProcessInstance`, `activateProcessInstance`, `getActiveActivityIds`
- 历史查询: `queryHistoricProcessInstance`, `queryHistoricActivityInstances`, `queryHistoricTaskInstances`, `queryHistoricVariableInstances`
- 流程定义: `getProcessDefinition`, `getProcessModel`, `suspendProcessDefinition`, `activateProcessDefinition`
- 节点跳转: `moveActivityIdTo`, `validateActivityJumpAllowed`
- 评论: `addComment`, `getComments`
- 身份链接: `getIdentityLinksForTask`

## 5. 事件驱动架构

### 5.1 事件流

```
Flowable 引擎事件 (TASK_CREATED / TASK_COMPLETED / PROCESS_COMPLETED)
    │
    ▼
FlowableEventBridge (FlowableEventListener, isFailOnException=true)
    │ 转换 FlowableEvent → Spring ApplicationEvent
    ▼
Spring Event Listener (@TransactionalEventListener 或 @EventListener)
    │
    ├─ TaskCreatedEventListener
    │   ├─ 执行拦截器链 onTaskCreate
    │   ├─ 若 skipTask → 自动完成 + 记录 AUTO_SKIP
    │   └─ 否则 → TaskAssignmentDomainService.assign() 分配审批人
    │
    ├─ TaskCompletedEventListener
    │   ├─ 执行拦截器链 onTaskComplete
    │   ├─ 记录审批操作日志 (wf_approval_record)
    │   └─ 发布 ApprovalActionEvent → Kafka 异步通知
    │
    └─ ProcessCompletedEventListener
        ├─ 更新 wf_process_instance_ext 状态
        └─ 发布 ProcessCompletedEvent → Kafka 异步通知
```

### 5.2 关键设计

1. **`FlowableEventBridge.isFailOnException()=true`**: 异常阻断 Flowable 引擎，保证事务一致性
2. **事件监听器与 Flowable 共享事务**: 审批日志写入与 Flowable 任务完成在同一事务中
3. **Kafka 异步通知**: 在事务提交后通过 Spring Event 发布，不阻塞主流程
4. **安全设计**: 流程变量注入时过滤保留字 (`RESULT`, `ACTION`)，防止客户端注入绕过审批

## 6. 审批动作核心流程

### 6.1 审批通过 (ApproveActionService)

```
1. 校验任务存在 + 当前用户是处理人
2. 按需签收 (claimTaskIfNeeded)
3. 服务端强制覆盖路由变量: RESULT=PASS, ACTION=APPROVED
4. 审批意见双写: Flowable comment + wf_approval_record
5. 调用 processEngine.completeTask()
6. 发布 ApprovalActionEvent → 触发通知
```

### 6.2 驳回 (RejectActionService)

```
1. 校验任务
2. 驳回模式解析: 策略优先 > 配置 > 默认(BPMN)
3. 若 BPMN: completeTask() 走默认流
   若 RETURNED: moveActivityIdTo(from, to) 跳回指定节点
4. 记录审计日志
5. 通知发起人
```

### 6.3 转办/委派 (TransferActionService / DelegateActionService)

```
转办:
1. 校验权限
2. setAssignee(newUserId) 改签
3. 记录审计日志 + wf_task_relation (TRANSFER)

委派:
1. 校验权限
2. claimTaskIfNeeded() 签收
3. delegateTask(newUserId) 委派
4. 记录审计日志 + wf_task_relation (DELEGATE)
```

### 6.4 加签 (AddSignActionService)

```
1. 校验任务
2. 逐个 userId: addCandidateUser() + 记录 wf_task_relation (COUNTER_SIGN)
3. 去重: 跳过已是 assignee 和已有候选人
4. 记录审计日志
5. 通知被加签人
```

### 6.5 拿回 (RecallActionService)

```
1. 前置校验: 下游任务未签收 (assignee 为空)
2. 风控拦截: validateRejectJumpAllowed + validateNotMultiInstance
3. moveActivityIdTo(下游节点, 当前节点)
4. 标记下游任务为终结
```

### 6.6 重新提交 (ResubmitActionService)

```
1. 从审批记录反查驳回锚点 (selectLatestUnresolvedReturn)
2. 找到驳回来源节点 sourceNodeKey
3. moveActivityIdTo(当前节点, sourceNodeKey)
4. 发布 RESUBMITTED 事件
```

### 6.7 撤销/作废 (CancelActionService)

```
1. 校验仅发起人可执行
2. 遍历所有活跃任务发布终结事件
3. 发布流程终态事件
4. deleteProcessInstance() 删除流程
```

## 7. 审批人分配流程

```
TaskCreatedEvent 触发
  │
  ▼
TaskAssignmentDomainService.assign(taskId, processInstanceId)
  │
  ├─ 1. 查询 wf_node_config WHERE process_def_key = ? AND node_def_key = ?
  ├─ 2. 查询 wf_node_candidate WHERE node_config_id = ?
  ├─ 3. 遍历 candidates，按 assignType 匹配 AssignmentStrategy
  │     ├─ USER: 直接返回 assignValue
  │     ├─ ROLE: 调用 rbac-service 查询角色成员
  │     ├─ DEPT: 调用 rbac-service 查询部门负责人
  │     ├─ GROUP: 查审批组表
  │     ├─ EXPRESSION: SpEL 解析 (SimpleEvaluationContext)
  │     ├─ API: HTTP POST 回调外部接口
  │     └─ INITIATOR: 从流程变量取 starterId
  ├─ 4. 去重 → 得到最终审批人列表
  └─ 5. setAssignee(tasks, assignees) 分配任务
```

## 8. 持久层 CQRS

### 8.1 设计

- 写操作: `CommandMapper` (insert/update/delete)
- 读操作: `QueryMapper` (select)
- DAO 层组合两者: `ApprovalRecordDAO` 持有 `commandMapper` + `queryMapper`

### 8.2 关键查询

- `FlowApprovalRecordQueryMapper.selectByProcessInstanceId`: 按流程实例 ID 查审批记录
- `FlowApprovalRecordQueryMapper.selectLatestUnresolvedReturn`: 查最近未消费的驳回记录 (重新提交反查锚点)
- `FlowApprovalRecordQueryMapper.streamByActionTimeRange`: 流式查询，用于统计任务
- `FlowApprovalStatisticsQueryMapper`: 按用户/周期/日期/流程定义聚合统计

### 8.3 改造策略

从 MyBatis-Plus `BaseMapper<T>` + LambdaQueryWrapper 改为 MyBatis XML:
- 保留 `common-data` 中的 `TenantLineInnerInterceptor` (租户隔离)
- 保留 `MetaObjectHandler` (审计字段自动填充)
- 保留 `LogicDelete` (逻辑删除)
- 去掉 `IService<T>` 继承，改为手动 DAO 封装

## 9. Controller 拆分

### 当前 6 个 Controller → 改造后 12 个 Controller

| 当前 Controller | 改造后 Controller | 变更 |
|----------------|------------------|------|
| `WfTaskExtController` | `TaskActionController` + `TaskController` | 查询与操作分离 |
| `WfProcessInstanceExtController` | `ProcessInstanceController` + `ProcessLifecycleController` | 实例管理与生命周期分离 |
| `WfProcessDefinitionExtController` | `DefinitionController` | 重命名，职责不变 |
| `WfNodeConfigController` | `NodeConfigController` | 增强，支持候选人管理 |
| `WfCopyController` | `CcRecordController` | 重命名 |
| `WfMonitorController` | 合并到 `ProcessInstanceController` | 监控功能归入实例管理 |
| — | `ApprovalDetailController` | 新增，审批详情 |
| — | `DraftController` | 新增，草稿管理 |
| — | `ModelController` | 新增，Flowable 模型管理 |
| — | `ApprovalStatisticsController` | 新增，审批统计 |
| — | `ApprovalStatisticsExportController` | 新增，统计导出 |

**Controller 设计原则**:
- 纯薄层: 仅 `@Valid` 参数校验 + 委托 DomainService
- 无 `@Transactional`: 事务由 DomainService 控制
- 统一返回 `ApiResult<T>`

## 10. 定时任务

### 10.1 审批统计聚合 (ApprovalStatisticsAggregationJob)

- 周期: 每日凌晨执行
- 逻辑: 流式读取 `wf_approval_record` 昨日数据，按 (tenant_id, user_id, process_def_key) 聚合
- 写入 `wf_approval_statistics`

### 10.2 审批超时处理 (ApprovalTimeoutJob)

- 周期: 每小时执行
- 逻辑: 查询 `wf_node_config` 中 `timeout_enabled=1` 的节点，检查对应任务是否超过 `timeout_hours`
- 超时动作: AUTO_APPROVE(自动通过) / AUTO_REJECT(自动驳回) / TRANSFER(转办) / REMIND(提醒)

## 11. 实施步骤

### 阶段一: 数据库 (1-2天)
1. 创建 4 张新表: `wf_approval_record`, `wf_node_candidate`, `wf_task_relation`, `wf_urge_log`, `wf_draft`, `wf_approval_statistics`
2. 扩展 3 张存量表: `wf_node_config`, `wf_process_definition_ext`, `wf_process_instance_ext`
3. 数据迁移: `wf_task_ext` 历史数据迁移到 `wf_approval_record`

### 阶段二: 基础设施 (2-3天)
1. 创建 `tunnel` 包: DO 实体 + Mapper 接口 + MyBatis XML + DAO
2. 创建 `ProcessEngineGateway` 防腐层
3. 创建 `FlowableEngineConfig` 事件监听注册
4. 创建 `FlowableEventBridge` + 3 个 EventListener

### 阶段三: 领域层 (4-5天)
1. 创建 7 种 `AssignmentStrategy` + `NodeConfigAssignmentResolver`
2. 创建 `TaskInterceptor` 责任链 + 2 个内置拦截器
3. 创建 9 种 `ApprovalAction` + `ApprovalActionSupport`
4. 创建各 DomainService

### 阶段四: 应用层 (2-3天)
1. 拆分 12 个 Controller
2. 创建定时任务
3. 创建统计导出服务

### 阶段五: 收尾 (1-2天)
1. API 文档更新 (Knife4j)
2. 单元测试
3. 前端适配

## 12. 风险与注意事项

1. **Flowable 版本兼容**: tuk 用 Flowable 6.6.0, saas-cloud 用 Flowable 7.x (Spring Boot 3.x 捆绑)，需确认 API 差异
2. **MyBatis-Plus 迁移**: 去掉 `IService<T>` 继承，改为手动 DAO，需确认租户拦截器、审计填充、逻辑删除依然生效
3. **Kafka 消息格式**: 当前 `NotifyEvent` 格式需对齐 `ApprovalMessageDTO`
4. **审批组**: tuk 有 `SysPost` 审批组体系，saas-cloud 可能用 RBAC 角色替代，需确认
5. **数据迁移**: `wf_task_ext` 历史数据需要平滑迁移，建议新旧表并行运行一段时间