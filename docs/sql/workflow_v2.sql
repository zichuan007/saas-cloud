-- ============================================================
-- SaaS Cloud Workflow 模块 - 数据库改造脚本
-- 数据库: workflow
-- 参考: tuk-approval-center
-- 说明: 在现有 5 张表基础上，新增 6 张表，改造 1 张核心表
-- ============================================================

USE workflow;

-- ============================================================
-- 1. 审批操作记录表 (核心审计表，替代 wf_task_ext 的审计职责)
-- ============================================================
DROP TABLE IF EXISTS `wf_approval_record`;
CREATE TABLE `wf_approval_record` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
    `process_instance_id` varchar(64) NOT NULL COMMENT 'Flowable流程实例ID',
    `process_def_key` varchar(128) DEFAULT NULL COMMENT '流程定义Key (冗余，方便按流程类型统计)',
    `process_name` varchar(256) DEFAULT NULL COMMENT '流程名称',
    `task_id` varchar(64) DEFAULT NULL COMMENT 'Flowable任务ID (流程级操作如撤销/撤回时为NULL)',
    `task_def_key` varchar(128) DEFAULT NULL COMMENT '节点定义Key (流程级操作时为NULL)',
    `task_name` varchar(256) DEFAULT NULL COMMENT '节点名称',
    `operator_id` bigint(20) NOT NULL COMMENT '操作人ID',
    `operator_name` varchar(64) DEFAULT NULL COMMENT '操作人姓名',
    `operator_dept_id` bigint(20) DEFAULT NULL COMMENT '操作人部门ID',
    `action` varchar(32) NOT NULL COMMENT '操作动作: APPROVED/REJECTED/RETURNED/TRANSFER/DELEGATE/ADD_SIGN/RECALL/CANCEL/RESUBMIT/URGE/AUTO_SUBMIT/AUTO_SKIP',
    `operator_type` varchar(16) NOT NULL DEFAULT 'USER' COMMENT '操作来源: USER/SYSTEM',
    `comment` varchar(1024) DEFAULT NULL COMMENT '审批意见',
    `system_reason` varchar(255) DEFAULT NULL COMMENT '系统判定原因 (如NODE_AUTO_SKIPPED)，与人工comment分离',
    `action_time` datetime NOT NULL COMMENT '操作时间',
    `flow_type` varchar(32) NOT NULL DEFAULT 'NORMAL' COMMENT '流转分类: NORMAL/REJECT/AUTO_SUBMIT',
    `source_node_key` varchar(128) DEFAULT NULL COMMENT '来源节点Key (驳回/跳转场景)',
    `target_node_key` varchar(128) DEFAULT NULL COMMENT '目标节点Key (驳回/跳转场景)',
    `parent_task_id` varchar(64) DEFAULT NULL COMMENT '父任务ID (转办/委派/加签场景)',
    `approve_strategy` varchar(32) DEFAULT NULL COMMENT '节点审批策略: ANY/ALL/SEQUENTIAL',
    `is_final_decision` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否终态决定: 0-否 1-是',
    `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
    `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_user_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
    `update_user_name` varchar(64) DEFAULT NULL COMMENT '更新人姓名',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag` int(11) NOT NULL DEFAULT 0 COMMENT '删除标记',
    `data_version` int(11) NOT NULL DEFAULT 0 COMMENT '数据版本号',
    `remark` varchar(512) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_operator` (`tenant_id`, `operator_id`),
    KEY `idx_tenant_operator_action` (`tenant_id`, `operator_id`, `action`),
    KEY `idx_process_instance` (`process_instance_id`),
    KEY `idx_task_id` (`task_id`),
    KEY `idx_action_time` (`tenant_id`, `action_time`),
    KEY `idx_proc_flow_time` (`process_instance_id`, `flow_type`, `action_time`),
    KEY `idx_operator_action_time` (`operator_id`, `action_time`, `delete_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批操作记录表';

-- ============================================================
-- 2. 流程节点配置表 (改造：从 process_definition_id 改为 process_def_key + node_def_key)
-- ============================================================
DROP TABLE IF EXISTS `wf_node_config_new`;
CREATE TABLE `wf_node_config_new` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
    `process_def_key` varchar(128) NOT NULL COMMENT '流程定义Key',
    `node_def_key` varchar(128) NOT NULL COMMENT '节点定义Key',
    `node_name` varchar(256) NOT NULL COMMENT '节点名称',
    -- 审批模式
    `approve_mode` varchar(16) NOT NULL DEFAULT 'ANY' COMMENT '审批模式: ANY-或签/ALL-会签/SEQUENTIAL-顺签',
    `pass_threshold` int(11) DEFAULT NULL COMMENT '通过阈值 (会签模式下达到此数即通过)',
    `sequential_order` int(11) NOT NULL DEFAULT 0 COMMENT '顺签顺序',
    -- 超时配置
    `timeout_enabled` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否启用超时: 0-禁用 1-启用',
    `timeout_hours` int(11) DEFAULT NULL COMMENT '超时小时数',
    `timeout_strategy` varchar(16) DEFAULT NULL COMMENT '超时策略: APPROVE/REJECT/TRANSFER/REMIND',
    `timeout_transfer_user_id` bigint(20) DEFAULT NULL COMMENT '超时转办人ID',
    -- 驳回配置
    `reject_mode` varchar(32) DEFAULT 'BPMN' COMMENT '驳回模式: BPMN-默认流/INITIATOR-发起人/PREVIOUS-上一节点/CUSTOM-自定义',
    `custom_target_node` varchar(128) DEFAULT NULL COMMENT '自定义驳回目标节点Key',
    -- 特殊配置
    `same_approver_skip` tinyint(1) NOT NULL DEFAULT 1 COMMENT '同审批人自动跳过: 0-不跳过 1-跳过',
    `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用: 0-禁用 1-启用',
    `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '排序',
    `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
    `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_user_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
    `update_user_name` varchar(64) DEFAULT NULL COMMENT '更新人姓名',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag` int(11) NOT NULL DEFAULT 0 COMMENT '删除标记',
    `data_version` int(11) NOT NULL DEFAULT 0 COMMENT '数据版本号',
    `remark` varchar(512) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_process_node` (`process_def_key`, `node_def_key`, `delete_flag`),
    KEY `idx_tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程节点配置表';

-- 迁移旧数据 (如果旧表存在)
-- INSERT INTO wf_node_config_new (...) SELECT ... FROM wf_node_config;
-- 注意: 旧表使用 process_definition_id，新表使用 process_def_key，需要关联 wf_process_definition_ext 转换

-- ============================================================
-- 3. 节点候选人来源表 (与 wf_node_config 多对一)
-- ============================================================
DROP TABLE IF EXISTS `wf_node_candidate`;
CREATE TABLE `wf_node_candidate` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
    `node_config_id` bigint(20) NOT NULL COMMENT '节点配置ID (关联 wf_node_config_new.id)',
    `process_def_key` varchar(128) NOT NULL COMMENT '流程定义Key (冗余)',
    `node_def_key` varchar(128) NOT NULL COMMENT '节点定义Key (冗余)',
    `assign_type` varchar(32) NOT NULL COMMENT '分配策略: USER/ROLE/DEPT/GROUP/EXPRESSION/API/INITIATOR',
    `assign_value` varchar(500) NOT NULL COMMENT '策略值 (用户ID/角色编码/审批组前缀/表达式/API URL)',
    `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '排序',
    `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
    `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_user_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
    `update_user_name` varchar(64) DEFAULT NULL COMMENT '更新人姓名',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag` int(11) NOT NULL DEFAULT 0 COMMENT '删除标记',
    `data_version` int(11) NOT NULL DEFAULT 0 COMMENT '数据版本号',
    `remark` varchar(512) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_node_config` (`node_config_id`),
    KEY `idx_process_node` (`process_def_key`, `node_def_key`, `delete_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='节点候选人来源表';

-- ============================================================
-- 4. 任务关联关系表 (转办/委派/加签链路追踪)
-- ============================================================
DROP TABLE IF EXISTS `wf_task_relation`;
CREATE TABLE `wf_task_relation` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
    `process_instance_id` varchar(64) NOT NULL COMMENT '流程实例ID',
    `task_id` varchar(64) NOT NULL COMMENT '当前任务ID',
    `parent_task_id` varchar(64) DEFAULT NULL COMMENT '父任务ID',
    `relation_type` varchar(32) NOT NULL COMMENT '关系类型: TRANSFER-转办/DELEGATE-委派/COUNTER_SIGN-加签',
    `operator_id` bigint(20) NOT NULL COMMENT '操作人ID',
    `operator_name` varchar(64) DEFAULT NULL COMMENT '操作人姓名',
    `target_user_id` bigint(20) NOT NULL COMMENT '目标用户ID',
    `target_user_name` varchar(64) DEFAULT NULL COMMENT '目标用户姓名',
    `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
    `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_user_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
    `update_user_name` varchar(64) DEFAULT NULL COMMENT '更新人姓名',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag` int(11) NOT NULL DEFAULT 0 COMMENT '删除标记',
    `data_version` int(11) NOT NULL DEFAULT 0 COMMENT '数据版本号',
    `remark` varchar(512) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_task_id` (`task_id`),
    KEY `idx_parent` (`parent_task_id`),
    KEY `idx_process_instance` (`process_instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务关联关系表';

-- ============================================================
-- 5. 催办记录表
-- ============================================================
DROP TABLE IF EXISTS `wf_urge_log`;
CREATE TABLE `wf_urge_log` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
    `process_instance_id` varchar(64) NOT NULL COMMENT '流程实例ID',
    `task_id` varchar(64) NOT NULL COMMENT '催办任务ID',
    `urge_user_id` bigint(20) NOT NULL COMMENT '催办人ID',
    `urge_user_name` varchar(64) DEFAULT NULL COMMENT '催办人姓名',
    `target_user_id` bigint(20) NOT NULL COMMENT '被催办人ID',
    `target_user_name` varchar(64) DEFAULT NULL COMMENT '被催办人姓名',
    `urge_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '催办时间',
    `channel` varchar(32) NOT NULL DEFAULT 'IN_APP' COMMENT '催办渠道: IN_APP/SMS/EMAIL',
    `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
    `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_user_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
    `update_user_name` varchar(64) DEFAULT NULL COMMENT '更新人姓名',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag` int(11) NOT NULL DEFAULT 0 COMMENT '删除标记',
    `data_version` int(11) NOT NULL DEFAULT 0 COMMENT '数据版本号',
    `remark` varchar(512) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_task` (`task_id`),
    KEY `idx_urge_user` (`urge_user_id`),
    KEY `idx_target_user` (`target_user_id`, `urge_time`),
    KEY `idx_proc_inst` (`process_instance_id`, `delete_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='催办记录表';

-- ============================================================
-- 6. 流程草稿表
-- ============================================================
DROP TABLE IF EXISTS `wf_draft`;
CREATE TABLE `wf_draft` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
    `process_def_key` varchar(128) NOT NULL COMMENT '流程定义Key',
    `process_name` varchar(256) DEFAULT NULL COMMENT '流程名称',
    `business_key` varchar(128) DEFAULT NULL COMMENT '业务关联键',
    `draft_content` text COMMENT '草稿内容 (JSON)',
    `status` varchar(32) NOT NULL DEFAULT 'DRAFT' COMMENT '状态: DRAFT-草稿/SUBMITTED-已提交',
    `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
    `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_user_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
    `update_user_name` varchar(64) DEFAULT NULL COMMENT '更新人姓名',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag` int(11) NOT NULL DEFAULT 0 COMMENT '删除标记',
    `data_version` int(11) NOT NULL DEFAULT 0 COMMENT '数据版本号',
    `remark` varchar(512) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_business_key` (`tenant_id`, `process_def_key`, `business_key`, `delete_flag`),
    KEY `idx_tenant_user` (`tenant_id`, `create_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程草稿表';

-- ============================================================
-- 7. 审批统计表
-- ============================================================
DROP TABLE IF EXISTS `wf_approval_statistics`;
CREATE TABLE `wf_approval_statistics` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
    `user_id` bigint(20) NOT NULL COMMENT '用户ID',
    `user_name` varchar(64) DEFAULT NULL COMMENT '用户姓名',
    `dept_id` bigint(20) DEFAULT NULL COMMENT '部门ID',
    `dept_name` varchar(128) DEFAULT NULL COMMENT '部门名称',
    `process_def_key` varchar(128) DEFAULT NULL COMMENT '流程定义Key (NULL表示汇总全部)',
    `process_name` varchar(256) DEFAULT NULL COMMENT '流程名称',
    `stat_period` varchar(16) NOT NULL COMMENT '统计周期: MONTH/QUARTER/YEAR',
    `stat_date` date NOT NULL COMMENT '统计日期/周期起始日',
    `total_count` int(11) NOT NULL DEFAULT 0 COMMENT '审批总数',
    `approved_count` int(11) NOT NULL DEFAULT 0 COMMENT '通过数',
    `rejected_count` int(11) NOT NULL DEFAULT 0 COMMENT '驳回数',
    `transferred_count` int(11) NOT NULL DEFAULT 0 COMMENT '转办数',
    `delegated_count` int(11) NOT NULL DEFAULT 0 COMMENT '委派数',
    `avg_duration_ms` bigint(20) DEFAULT NULL COMMENT '平均耗时(ms)',
    `max_duration_ms` bigint(20) DEFAULT NULL COMMENT '最大耗时(ms)',
    `min_duration_ms` bigint(20) DEFAULT NULL COMMENT '最小耗时(ms)',
    `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
    `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_user_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
    `update_user_name` varchar(64) DEFAULT NULL COMMENT '更新人姓名',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag` int(11) NOT NULL DEFAULT 0 COMMENT '删除标记',
    `data_version` int(11) NOT NULL DEFAULT 0 COMMENT '数据版本号',
    `remark` varchar(512) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_user_period_date_process` (`tenant_id`, `user_id`, `stat_period`, `stat_date`, `process_def_key`),
    KEY `idx_tenant_period` (`tenant_id`, `stat_period`, `stat_date`),
    KEY `idx_user_period_date` (`user_id`, `stat_period`, `stat_date`),
    KEY `idx_dept_period_date` (`dept_id`, `stat_period`, `stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批统计表';

-- ============================================================
-- 存量表改造: wf_process_instance_ext 增加 flow_type 字段
-- ============================================================
ALTER TABLE `wf_process_instance_ext`
    ADD COLUMN `flow_type` varchar(32) DEFAULT 'NORMAL' COMMENT '流程类型: NORMAL/REJECT/AUTO_SUBMIT' AFTER `business_key`;

-- ============================================================
-- 存量表保留: wf_process_definition_ext, wf_copy, wf_task_ext (暂时保留兼容)
-- wf_task_ext 将在代码层面废弃，数据迁移到 wf_approval_record
-- ============================================================