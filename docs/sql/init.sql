-- SaaS Cloud 数据库初始化脚本（schema + seed）
-- 行政区划数据见 init_area.sql；nacos schema 见 nacos 容器 mysql-schema.sql
-- 生成自当前已修正的本地库（含 valid_status/trace_id 审计字段）


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

/*!40000 DROP DATABASE IF EXISTS `platform`*/;

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `platform` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `platform`;
DROP TABLE IF EXISTS `sys_announcement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_announcement` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title` varchar(256) NOT NULL COMMENT '公告标题',
  `content` text NOT NULL COMMENT '公告内容',
  `type` tinyint NOT NULL DEFAULT '0' COMMENT '类型 0-通知 1-维护公告 2-功能更新',
  `target_type` tinyint NOT NULL DEFAULT '0' COMMENT '目标 0-全部租户 1-指定租户',
  `target_tenant_ids` text COMMENT '指定租户ID列表(JSON数组)',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态 0-草稿 1-已发布 2-已下线',
  `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(64) DEFAULT NULL COMMENT '更新人姓名',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` int NOT NULL DEFAULT '0' COMMENT '删除标记',
  `data_version` int NOT NULL DEFAULT '0' COMMENT '数据版本号',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  KEY `idx_status_publish` (`status`,`publish_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统公告表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_api_client`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_api_client` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL COMMENT 'ç§Ÿæˆ·ID',
  `client_name` varchar(128) NOT NULL COMMENT 'åº”ç”¨åç§°',
  `api_key` varchar(64) NOT NULL COMMENT 'API Key',
  `api_secret` varchar(128) NOT NULL COMMENT 'API Secret',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT 'çŠ¶æ€ 0-ç¦ç”¨ 1-å¯ç”¨',
  `rate_limit` int NOT NULL DEFAULT '100' COMMENT 'æ¯åˆ†é’Ÿè°ƒç”¨é™é¢',
  `expire_time` datetime DEFAULT NULL COMMENT 'è¿‡æœŸæ—¶é—´',
  `create_user_id` varchar(64) DEFAULT NULL,
  `create_user_name` varchar(64) DEFAULT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_user_id` varchar(64) DEFAULT NULL,
  `update_user_name` varchar(64) DEFAULT NULL,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` int NOT NULL DEFAULT '0',
  `data_version` int NOT NULL DEFAULT '0',
  `remark` varchar(512) DEFAULT NULL,
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_api_key` (`api_key`),
  KEY `idx_tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='APIå¼€æ”¾å¹³å°å®¢æˆ·ç«¯è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_file` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ä¸»é”®',
  `file_name` varchar(255) NOT NULL COMMENT 'åŽŸå§‹æ–‡ä»¶å',
  `file_path` varchar(500) NOT NULL COMMENT 'å­˜å‚¨è·¯å¾„ï¼ˆMinIO objectNameï¼‰',
  `file_size` bigint NOT NULL DEFAULT '0' COMMENT 'æ–‡ä»¶å¤§å°ï¼ˆå­—èŠ‚ï¼‰',
  `file_type` varchar(100) DEFAULT NULL COMMENT 'æ–‡ä»¶MIMEç±»åž‹',
  `file_suffix` varchar(32) DEFAULT NULL COMMENT 'æ–‡ä»¶åŽç¼€',
  `bucket_name` varchar(100) NOT NULL COMMENT 'å­˜å‚¨æ¡¶åç§°',
  `biz_type` varchar(64) DEFAULT NULL COMMENT 'ä¸šåŠ¡ç±»åž‹ï¼ˆavatar/document/attachmentç­‰ï¼‰',
  `biz_id` varchar(64) DEFAULT NULL COMMENT 'å…³è”ä¸šåŠ¡ID',
  `tenant_id` bigint NOT NULL COMMENT 'ç§Ÿæˆ·ID',
  `create_user_id` varchar(64) DEFAULT NULL COMMENT 'åˆ›å»ºäººID',
  `create_user_name` varchar(64) DEFAULT NULL COMMENT 'åˆ›å»ºäººå§“å',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_user_id` varchar(64) DEFAULT NULL COMMENT 'æ›´æ–°äººID',
  `update_user_name` varchar(64) DEFAULT NULL COMMENT 'æ›´æ–°äººå§“å',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `delete_flag` int NOT NULL DEFAULT '0' COMMENT 'åˆ é™¤æ ‡è®° 0-æ­£å¸¸ 1-åˆ é™¤',
  `data_version` int NOT NULL DEFAULT '0' COMMENT 'ä¹è§‚é”ç‰ˆæœ¬å·',
  `remark` varchar(255) DEFAULT NULL COMMENT 'å¤‡æ³¨',
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_biz` (`biz_type`,`biz_id`),
  KEY `idx_file_path` (`file_path`(191))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='æ–‡ä»¶ç®¡ç†è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_global_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_global_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` varchar(128) NOT NULL COMMENT '配置键',
  `config_value` varchar(2048) NOT NULL COMMENT '配置值',
  `config_type` varchar(32) NOT NULL DEFAULT 'STRING' COMMENT '值类型 STRING/NUMBER/BOOLEAN/JSON',
  `description` varchar(256) DEFAULT NULL COMMENT '配置说明',
  `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(64) DEFAULT NULL COMMENT '更新人姓名',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` int NOT NULL DEFAULT '0' COMMENT '删除标记',
  `data_version` int NOT NULL DEFAULT '0' COMMENT '数据版本号',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='全局配置表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_package`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_package` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '套餐ID',
  `package_name` varchar(64) NOT NULL COMMENT '套餐名称',
  `package_code` varchar(32) NOT NULL COMMENT '套餐编码 FREE/BASIC/PRO/ENTERPRISE',
  `price_monthly` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '月价格',
  `price_yearly` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '年价格',
  `max_users` int NOT NULL DEFAULT '0' COMMENT '最大用户数 0-不限',
  `max_roles` int NOT NULL DEFAULT '0' COMMENT '最大角色数 0-不限',
  `max_depts` int NOT NULL DEFAULT '0' COMMENT '最大部门数 0-不限',
  `max_process_definitions` int NOT NULL DEFAULT '0' COMMENT '最大流程定义数 0-不限',
  `max_wechat_accounts` int NOT NULL DEFAULT '0' COMMENT '最大公众号绑定数 0-不限',
  `max_storage_mb` bigint NOT NULL DEFAULT '0' COMMENT '最大存储空间(MB) 0-不限',
  `menu_ids` text COMMENT '该套餐可见的菜单ID列表(JSON数组)',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态 0-禁用 1-启用',
  `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(64) DEFAULT NULL COMMENT '更新人姓名',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` int NOT NULL DEFAULT '0' COMMENT '删除标记',
  `data_version` int NOT NULL DEFAULT '0' COMMENT '数据版本号',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_package_code` (`package_code`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='套餐表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_platform_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_platform_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(64) NOT NULL COMMENT '用户名',
  `password` varchar(128) NOT NULL COMMENT '密码(BCrypt)',
  `real_name` varchar(64) DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(128) DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(512) DEFAULT NULL COMMENT '头像URL',
  `role_type` tinyint NOT NULL DEFAULT '1' COMMENT '角色类型 0-超级管理员 1-运营人员',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态 0-禁用 1-启用',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(64) DEFAULT NULL COMMENT '最后登录IP',
  `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(64) DEFAULT NULL COMMENT '更新人姓名',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` int NOT NULL DEFAULT '0' COMMENT '删除标记',
  `data_version` int NOT NULL DEFAULT '0' COMMENT '数据版本号',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='平台用户表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_tenant`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_tenant` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '租户ID',
  `tenant_name` varchar(128) NOT NULL COMMENT '租户名称',
  `tenant_code` varchar(64) NOT NULL COMMENT '租户编码',
  `contact_person` varchar(64) DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `contact_email` varchar(128) DEFAULT NULL COMMENT '联系邮箱',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态 0-试用 1-正常 2-冻结 3-注销',
  `package_id` bigint NOT NULL COMMENT '套餐ID',
  `trial_expire_time` datetime DEFAULT NULL COMMENT '试用到期时间',
  `paid_expire_time` datetime DEFAULT NULL COMMENT '付费到期时间',
  `frozen_time` datetime DEFAULT NULL COMMENT '冻结时间',
  `frozen_reason` varchar(512) DEFAULT NULL COMMENT '冻结原因',
  `admin_user_id` bigint DEFAULT NULL COMMENT '租户管理员用户ID',
  `logo_url` varchar(512) DEFAULT NULL COMMENT '企业Logo',
  `address` varchar(512) DEFAULT NULL COMMENT '企业地址',
  `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(64) DEFAULT NULL COMMENT '更新人姓名',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` int NOT NULL DEFAULT '0' COMMENT '删除标记',
  `data_version` int NOT NULL DEFAULT '0' COMMENT '数据版本号',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_code` (`tenant_code`),
  KEY `idx_status` (`status`),
  KEY `idx_package_id` (`package_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='租户表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_tenant_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_tenant_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL COMMENT 'ç§Ÿæˆ·ID',
  `package_id` bigint NOT NULL COMMENT 'å¥—é¤ID',
  `order_no` varchar(64) NOT NULL COMMENT 'è®¢å•å·',
  `amount` decimal(10,2) NOT NULL COMMENT 'é‡‘é¢ï¼ˆå…ƒï¼‰',
  `pay_type` tinyint DEFAULT NULL COMMENT 'æ”¯ä»˜æ–¹å¼ 1-å¾®ä¿¡ 2-æ”¯ä»˜å®',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT 'çŠ¶æ€ 0-å¾…æ”¯ä»˜ 1-å·²æ”¯ä»˜ 2-å·²å–æ¶ˆ 3-å·²é€€æ¬¾',
  `pay_time` datetime DEFAULT NULL COMMENT 'æ”¯ä»˜æ—¶é—´',
  `expire_time` datetime DEFAULT NULL COMMENT 'è®¢é˜…åˆ°æœŸæ—¶é—´',
  `create_user_id` varchar(64) DEFAULT NULL,
  `create_user_name` varchar(64) DEFAULT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_user_id` varchar(64) DEFAULT NULL,
  `update_user_name` varchar(64) DEFAULT NULL,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` int NOT NULL DEFAULT '0',
  `data_version` int NOT NULL DEFAULT '0',
  `remark` varchar(512) DEFAULT NULL,
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ç§Ÿæˆ·è®¢é˜…è®¢å•è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 DROP DATABASE IF EXISTS `rbac`*/;

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `rbac` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `rbac`;
DROP TABLE IF EXISTS `sys_area`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_area` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `parent_code` varchar(12) NOT NULL DEFAULT '0' COMMENT '父级区划代码（0=顶级）',
  `area_code` varchar(12) NOT NULL COMMENT '国标行政区划代码（6位）',
  `area_name` varchar(64) NOT NULL COMMENT '区域名称',
  `short_name` varchar(64) NOT NULL DEFAULT '' COMMENT '简称',
  `merger_name` varchar(255) NOT NULL DEFAULT '' COMMENT '组合名称（如：北京,东城）',
  `pinyin` varchar(128) NOT NULL DEFAULT '' COMMENT '拼音',
  `first_letter` char(1) NOT NULL DEFAULT '' COMMENT '拼音首字母',
  `area_level` tinyint NOT NULL COMMENT '层级 1-省 2-市 3-区/县',
  `zip_code` varchar(10) NOT NULL DEFAULT '' COMMENT '邮政编码',
  `city_code` varchar(10) NOT NULL DEFAULT '' COMMENT '电话区号',
  `lng` decimal(10,6) DEFAULT NULL COMMENT '经度',
  `lat` decimal(10,6) DEFAULT NULL COMMENT '纬度',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_area_code` (`area_code`),
  KEY `idx_parent_code` (`parent_code`),
  KEY `idx_first_letter` (`first_letter`)
) ENGINE=InnoDB AUTO_INCREMENT=4019 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='行政区划表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_dept`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_dept` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '部门ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `dept_name` varchar(128) NOT NULL COMMENT '部门名称',
  `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父部门ID 0-顶级',
  `ancestors` varchar(1024) NOT NULL DEFAULT '0' COMMENT '祖先链',
  `leader_user_id` bigint DEFAULT NULL COMMENT '部门负责人ID',
  `leader` varchar(64) DEFAULT NULL COMMENT '负责人姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态 0-禁用 1-启用',
  `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(64) DEFAULT NULL COMMENT '更新人姓名',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` int NOT NULL DEFAULT '0' COMMENT '删除标记',
  `data_version` int NOT NULL DEFAULT '0' COMMENT '数据版本号',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_parent` (`tenant_id`,`parent_id`),
  KEY `idx_ancestors` (`ancestors`(255))
) ENGINE=InnoDB AUTO_INCREMENT=204 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='部门表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_dict_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_dict_data` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ä¸»é”®',
  `dict_type` varchar(100) NOT NULL COMMENT 'å­—å…¸ç±»åž‹ç¼–ç ',
  `dict_label` varchar(100) NOT NULL COMMENT 'å­—å…¸æ ‡ç­¾',
  `dict_value` varchar(100) NOT NULL COMMENT 'å­—å…¸é”®å€¼',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT 'æŽ’åº',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT 'çŠ¶æ€ 0-ç¦ç”¨ 1-å¯ç”¨',
  `css_class` varchar(100) DEFAULT NULL COMMENT 'æ ·å¼å±žæ€§',
  `list_class` varchar(100) DEFAULT NULL COMMENT 'è¡¨æ ¼å›žæ˜¾æ ·å¼',
  `tenant_id` bigint NOT NULL COMMENT 'ç§Ÿæˆ·ID',
  `create_user_id` varchar(64) DEFAULT NULL COMMENT 'åˆ›å»ºäººID',
  `create_user_name` varchar(64) DEFAULT NULL COMMENT 'åˆ›å»ºäººå§“å',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_user_id` varchar(64) DEFAULT NULL COMMENT 'æ›´æ–°äººID',
  `update_user_name` varchar(64) DEFAULT NULL COMMENT 'æ›´æ–°äººå§“å',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `delete_flag` int NOT NULL DEFAULT '0' COMMENT 'åˆ é™¤æ ‡è®° 0-æ­£å¸¸ 1-åˆ é™¤',
  `data_version` int NOT NULL DEFAULT '0' COMMENT 'ä¹è§‚é”ç‰ˆæœ¬å·',
  `remark` varchar(255) DEFAULT NULL COMMENT 'å¤‡æ³¨',
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  KEY `idx_dict_type` (`dict_type`)
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='æ•°æ®å­—å…¸æ•°æ®è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_dict_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_dict_type` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ä¸»é”®',
  `dict_name` varchar(100) NOT NULL COMMENT 'å­—å…¸åç§°',
  `dict_type` varchar(100) NOT NULL COMMENT 'å­—å…¸ç±»åž‹ç¼–ç ',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT 'çŠ¶æ€ 0-ç¦ç”¨ 1-å¯ç”¨',
  `tenant_id` bigint NOT NULL COMMENT 'ç§Ÿæˆ·ID',
  `create_user_id` varchar(64) DEFAULT NULL COMMENT 'åˆ›å»ºäººID',
  `create_user_name` varchar(64) DEFAULT NULL COMMENT 'åˆ›å»ºäººå§“å',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_user_id` varchar(64) DEFAULT NULL COMMENT 'æ›´æ–°äººID',
  `update_user_name` varchar(64) DEFAULT NULL COMMENT 'æ›´æ–°äººå§“å',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `delete_flag` int NOT NULL DEFAULT '0' COMMENT 'åˆ é™¤æ ‡è®° 0-æ­£å¸¸ 1-åˆ é™¤',
  `data_version` int NOT NULL DEFAULT '0' COMMENT 'ä¹è§‚é”ç‰ˆæœ¬å·',
  `remark` varchar(255) DEFAULT NULL COMMENT 'å¤‡æ³¨',
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dict_type_tenant` (`dict_type`,`tenant_id`,`delete_flag`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='æ•°æ®å­—å…¸ç±»åž‹è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_export_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_export_task` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL COMMENT 'ç§Ÿæˆ·ID',
  `task_name` varchar(128) NOT NULL COMMENT 'ä»»åŠ¡åç§°',
  `task_type` varchar(64) NOT NULL COMMENT 'ä»»åŠ¡ç±»åž‹ export-å¯¼å‡º template-æ¨¡æ¿',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT 'çŠ¶æ€ 0-æŽ’é˜Ÿä¸­ 1-å¤„ç†ä¸­ 2-æˆåŠŸ 3-å¤±è´¥',
  `file_name` varchar(256) DEFAULT NULL COMMENT 'æ–‡ä»¶å',
  `file_path` varchar(512) DEFAULT NULL COMMENT 'MinIO objectName',
  `file_size` bigint DEFAULT NULL COMMENT 'æ–‡ä»¶å¤§å°(å­—èŠ‚)',
  `error_msg` varchar(512) DEFAULT NULL COMMENT 'å¤±è´¥åŽŸå› ',
  `expire_time` datetime DEFAULT NULL COMMENT 'è¿‡æœŸæ—¶é—´(7å¤©åŽè‡ªåŠ¨æ¸…ç†)',
  `download_count` int NOT NULL DEFAULT '0' COMMENT 'ä¸‹è½½æ¬¡æ•°',
  `request_params` varchar(1024) DEFAULT NULL COMMENT 'è¯·æ±‚å‚æ•°JSON',
  `create_user_id` varchar(64) DEFAULT NULL,
  `create_user_name` varchar(64) DEFAULT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_user_id` varchar(64) DEFAULT NULL,
  `update_user_name` varchar(64) DEFAULT NULL,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` int NOT NULL DEFAULT '0',
  `data_version` int NOT NULL DEFAULT '0',
  `remark` varchar(512) DEFAULT NULL,
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_user` (`tenant_id`,`create_user_id`),
  KEY `idx_tenant_status` (`tenant_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='å¯¼å‡ºä»»åŠ¡è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_login_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_login_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'æ—¥å¿—ID',
  `tenant_id` bigint NOT NULL COMMENT 'ç§Ÿæˆ·ID',
  `user_id` bigint DEFAULT NULL COMMENT 'ç”¨æˆ·IDï¼ˆç™»å½•æˆåŠŸæ—¶ï¼‰',
  `username` varchar(64) NOT NULL COMMENT 'ç™»å½•ç”¨æˆ·å',
  `login_type` tinyint NOT NULL DEFAULT '0' COMMENT 'ç™»å½•ç±»åž‹ 0-å¯†ç ç™»å½• 1-çŸ­ä¿¡ç™»å½• 2-ç¬¬ä¸‰æ–¹ç™»å½•',
  `status` tinyint NOT NULL COMMENT 'ç™»å½•çŠ¶æ€ 0-å¤±è´¥ 1-æˆåŠŸ',
  `ip` varchar(64) DEFAULT NULL COMMENT 'ç™»å½•IP',
  `location` varchar(128) DEFAULT NULL COMMENT 'ç™»å½•åœ°ç‚¹',
  `browser` varchar(128) DEFAULT NULL COMMENT 'æµè§ˆå™¨',
  `os` varchar(128) DEFAULT NULL COMMENT 'æ“ä½œç³»ç»Ÿ',
  `user_agent` varchar(512) DEFAULT NULL COMMENT 'User-Agent',
  `error_msg` varchar(512) DEFAULT NULL COMMENT 'å¤±è´¥åŽŸå› ',
  `login_time` datetime NOT NULL COMMENT 'ç™»å½•æ—¶é—´',
  `create_user_id` varchar(64) DEFAULT NULL,
  `create_user_name` varchar(64) DEFAULT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_user_id` varchar(64) DEFAULT NULL,
  `update_user_name` varchar(64) DEFAULT NULL,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` int NOT NULL DEFAULT '0',
  `data_version` int NOT NULL DEFAULT '0',
  `remark` varchar(512) DEFAULT NULL,
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_user` (`tenant_id`,`user_id`),
  KEY `idx_tenant_time` (`tenant_id`,`login_time`),
  KEY `idx_tenant_username` (`tenant_id`,`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ç™»å½•æ—¥å¿—è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID（平台级菜单为空）',
  `menu_name` varchar(64) NOT NULL COMMENT '菜单名称',
  `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父菜单ID 0-顶级',
  `menu_type` tinyint NOT NULL COMMENT '类型 0-目录 1-菜单 2-按钮',
  `path` varchar(256) DEFAULT NULL COMMENT '路由路径',
  `component` varchar(256) DEFAULT NULL COMMENT '组件路径',
  `permission` varchar(128) DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(128) DEFAULT NULL COMMENT '图标',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态 0-禁用 1-启用',
  `visible` tinyint NOT NULL DEFAULT '1' COMMENT '是否可见 0-隐藏 1-显示',
  `is_external` tinyint NOT NULL DEFAULT '0' COMMENT '是否外链',
  `is_cached` tinyint NOT NULL DEFAULT '0' COMMENT '是否缓存',
  `module` varchar(32) DEFAULT NULL COMMENT '所属模块 RBAC/WORKFLOW/WECHAT_OA',
  `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(64) DEFAULT NULL COMMENT '更新人姓名',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` int NOT NULL DEFAULT '0' COMMENT '删除标记',
  `data_version` int NOT NULL DEFAULT '0' COMMENT '数据版本号',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  KEY `idx_parent` (`parent_id`),
  KEY `idx_module` (`module`)
) ENGINE=InnoDB AUTO_INCREMENT=5036 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜单表（平台级）';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_notice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_notice` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `title` varchar(128) NOT NULL COMMENT '公告标题',
  `content` text COMMENT '公告内容',
  `notice_type` tinyint NOT NULL DEFAULT '1' COMMENT '类型 1-通知 2-公告',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态 0-草稿 1-已发布 2-已撤回',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `create_user_id` varchar(64) DEFAULT NULL,
  `create_user_name` varchar(64) DEFAULT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_user_id` varchar(64) DEFAULT NULL,
  `update_user_name` varchar(64) DEFAULT NULL,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` int NOT NULL DEFAULT '0',
  `data_version` int NOT NULL DEFAULT '0',
  `remark` varchar(512) DEFAULT NULL,
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_status` (`tenant_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通知公告表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_notice_read`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_notice_read` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `notice_id` bigint NOT NULL COMMENT '公告ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `read_time` datetime NOT NULL COMMENT '阅读时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_notice_user` (`notice_id`,`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='公告已读记录表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_operation_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `user_id` bigint DEFAULT NULL COMMENT '操作用户ID',
  `username` varchar(64) DEFAULT NULL COMMENT '操作用户名',
  `module` varchar(64) DEFAULT NULL COMMENT '操作模块',
  `operation` varchar(128) DEFAULT NULL COMMENT '操作描述',
  `method` varchar(256) DEFAULT NULL COMMENT '请求方法',
  `request_url` varchar(512) DEFAULT NULL COMMENT '请求URL',
  `request_method` varchar(16) DEFAULT NULL COMMENT 'HTTP方法',
  `request_params` text COMMENT '请求参数',
  `response_code` int DEFAULT NULL COMMENT '响应状态码',
  `error_msg` text COMMENT '错误信息',
  `ip` varchar(64) DEFAULT NULL COMMENT '操作IP',
  `location` varchar(128) DEFAULT NULL COMMENT 'IP归属地',
  `user_agent` varchar(512) DEFAULT NULL COMMENT '用户代理',
  `duration` bigint DEFAULT NULL COMMENT '执行时长(ms)',
  `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(64) DEFAULT NULL COMMENT '更新人姓名',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` int NOT NULL DEFAULT '0' COMMENT '删除标记',
  `data_version` int NOT NULL DEFAULT '0' COMMENT '数据版本号',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_user` (`tenant_id`,`user_id`),
  KEY `idx_tenant_time` (`tenant_id`,`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_password_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_password_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `password` varchar(128) NOT NULL COMMENT '历史密码(BCrypt)',
  `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(64) DEFAULT NULL COMMENT '更新人姓名',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` int NOT NULL DEFAULT '0' COMMENT '删除标记',
  `data_version` int NOT NULL DEFAULT '0' COMMENT '数据版本号',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_user` (`tenant_id`,`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='密码历史表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_post`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_post` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'å²—ä½ID',
  `tenant_id` bigint NOT NULL COMMENT 'ç§Ÿæˆ·ID',
  `post_code` varchar(64) NOT NULL COMMENT 'å²—ä½ç¼–ç ',
  `post_name` varchar(128) NOT NULL COMMENT 'å²—ä½åç§°',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT 'æŽ’åº',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT 'çŠ¶æ€ 0-ç¦ç”¨ 1-å¯ç”¨',
  `create_user_id` varchar(64) DEFAULT NULL,
  `create_user_name` varchar(64) DEFAULT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_user_id` varchar(64) DEFAULT NULL,
  `update_user_name` varchar(64) DEFAULT NULL,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` int NOT NULL DEFAULT '0',
  `data_version` int NOT NULL DEFAULT '0',
  `remark` varchar(512) DEFAULT NULL,
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_post_code` (`tenant_id`,`post_code`,`delete_flag`),
  KEY `idx_tenant_status` (`tenant_id`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='å²—ä½è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `role_name` varchar(64) NOT NULL COMMENT '角色名称',
  `role_code` varchar(64) NOT NULL COMMENT '角色编码',
  `role_level` tinyint NOT NULL DEFAULT '2' COMMENT '角色等级 0-超管 1-管理员 2-普通',
  `data_scope` tinyint NOT NULL DEFAULT '4' COMMENT '数据范围 1-全部 2-本部门及下级 3-本部门 4-仅本人 5-自定义',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态 0-禁用 1-启用',
  `is_system` tinyint NOT NULL DEFAULT '0' COMMENT '是否系统内置 0-否 1-是',
  `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(64) DEFAULT NULL COMMENT '更新人姓名',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` int NOT NULL DEFAULT '0' COMMENT '删除标记',
  `data_version` int NOT NULL DEFAULT '0' COMMENT '数据版本号',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_role_code` (`tenant_id`,`role_code`,`delete_flag`),
  KEY `idx_tenant_status` (`tenant_id`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=204 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_role_dept`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role_dept` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `dept_id` bigint NOT NULL COMMENT '部门ID',
  `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(64) DEFAULT NULL COMMENT '更新人姓名',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` int NOT NULL DEFAULT '0' COMMENT '删除标记',
  `data_version` int NOT NULL DEFAULT '0' COMMENT '数据版本号',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_role_dept` (`tenant_id`,`role_id`,`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色部门关联表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_role_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(64) DEFAULT NULL COMMENT '更新人姓名',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` int NOT NULL DEFAULT '0' COMMENT '删除标记',
  `data_version` int NOT NULL DEFAULT '0' COMMENT '数据版本号',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_role_menu` (`tenant_id`,`role_id`,`menu_id`)
) ENGINE=InnoDB AUTO_INCREMENT=752 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色菜单关联表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_sensitive_word`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_sensitive_word` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL COMMENT 'ç§Ÿæˆ·ID',
  `word` varchar(128) NOT NULL COMMENT 'æ•æ„Ÿè¯',
  `category` varchar(64) DEFAULT NULL COMMENT 'åˆ†ç±»ï¼ˆæ¶‰æ”¿/è‰²æƒ…/æš´åŠ›/å¹¿å‘Šç­‰ï¼‰',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT 'çŠ¶æ€ 0-ç¦ç”¨ 1-å¯ç”¨',
  `create_user_id` varchar(64) DEFAULT NULL,
  `create_user_name` varchar(64) DEFAULT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_user_id` varchar(64) DEFAULT NULL,
  `update_user_name` varchar(64) DEFAULT NULL,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` int NOT NULL DEFAULT '0',
  `data_version` int NOT NULL DEFAULT '0',
  `remark` varchar(512) DEFAULT NULL,
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_status` (`tenant_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='æ•æ„Ÿè¯è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_social_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_social_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL COMMENT 'ç§Ÿæˆ·ID',
  `user_id` bigint NOT NULL COMMENT 'å…³è”çš„ç³»ç»Ÿç”¨æˆ·ID',
  `social_type` varchar(32) NOT NULL COMMENT 'å¹³å°ç±»åž‹ wechat/dingtalk/github/gitee',
  `social_id` varchar(128) NOT NULL COMMENT 'ç¬¬ä¸‰æ–¹å¹³å°ç”¨æˆ·ID',
  `social_name` varchar(128) DEFAULT NULL COMMENT 'ç¬¬ä¸‰æ–¹å¹³å°ç”¨æˆ·å',
  `social_avatar` varchar(512) DEFAULT NULL COMMENT 'å¤´åƒ',
  `access_token` varchar(512) DEFAULT NULL COMMENT 'è®¿é—®ä»¤ç‰Œ',
  `refresh_token` varchar(512) DEFAULT NULL COMMENT 'åˆ·æ–°ä»¤ç‰Œ',
  `expire_time` datetime DEFAULT NULL COMMENT 'ä»¤ç‰Œè¿‡æœŸæ—¶é—´',
  `create_user_id` varchar(64) DEFAULT NULL,
  `create_user_name` varchar(64) DEFAULT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_user_id` varchar(64) DEFAULT NULL,
  `update_user_name` varchar(64) DEFAULT NULL,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` int NOT NULL DEFAULT '0',
  `data_version` int NOT NULL DEFAULT '0',
  `remark` varchar(512) DEFAULT NULL,
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_social` (`tenant_id`,`social_type`,`social_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ç¤¾äº¤ç™»å½•ç»‘å®šè¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `username` varchar(64) NOT NULL COMMENT '用户名',
  `password` varchar(128) NOT NULL COMMENT '密码(BCrypt)',
  `real_name` varchar(64) DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(128) DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(512) DEFAULT NULL COMMENT '头像URL',
  `gender` tinyint DEFAULT '0' COMMENT '性别 0-未知 1-男 2-女',
  `dept_id` bigint DEFAULT NULL COMMENT '部门ID',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态 0-禁用 1-启用',
  `role_level` tinyint NOT NULL DEFAULT '2' COMMENT '角色等级 0-租户超管 1-部门主管 2-普通',
  `invite_code` varchar(64) DEFAULT NULL COMMENT '邀请码',
  `invite_status` tinyint DEFAULT NULL COMMENT '邀请状态 0-待接受 1-已接受 2-已过期',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(64) DEFAULT NULL COMMENT '最后登录IP',
  `password_update_time` datetime DEFAULT NULL COMMENT '最后修改密码时间',
  `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(64) DEFAULT NULL COMMENT '更新人姓名',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` int NOT NULL DEFAULT '0' COMMENT '删除标记',
  `data_version` int NOT NULL DEFAULT '0' COMMENT '数据版本号',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_username` (`tenant_id`,`username`,`delete_flag`),
  UNIQUE KEY `uk_phone_global` (`phone`,`delete_flag`),
  KEY `idx_tenant_dept` (`tenant_id`,`dept_id`),
  KEY `idx_tenant_status` (`tenant_id`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=204 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_user_post`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user_post` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ä¸»é”®',
  `tenant_id` bigint NOT NULL COMMENT 'ç§Ÿæˆ·ID',
  `user_id` bigint NOT NULL COMMENT 'ç”¨æˆ·ID',
  `post_id` bigint NOT NULL COMMENT 'å²—ä½ID',
  `create_user_id` varchar(64) DEFAULT NULL,
  `create_user_name` varchar(64) DEFAULT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_user_id` varchar(64) DEFAULT NULL,
  `update_user_name` varchar(64) DEFAULT NULL,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` int NOT NULL DEFAULT '0',
  `data_version` int NOT NULL DEFAULT '0',
  `remark` varchar(512) DEFAULT NULL,
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_user_post` (`tenant_id`,`user_id`,`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ç”¨æˆ·å²—ä½å…³è”è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(64) DEFAULT NULL COMMENT '更新人姓名',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` int NOT NULL DEFAULT '0' COMMENT '删除标记',
  `data_version` int NOT NULL DEFAULT '0' COMMENT '数据版本号',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_user_role` (`tenant_id`,`user_id`,`role_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户角色关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 DROP DATABASE IF EXISTS `wechat_oa`*/;

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `wechat_oa` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `wechat_oa`;
DROP TABLE IF EXISTS `wechat_oa_account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wechat_oa_account` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `account_name` varchar(128) NOT NULL COMMENT '公众号名称',
  `app_id` varchar(64) NOT NULL COMMENT '微信AppID',
  `app_secret` varchar(128) NOT NULL COMMENT '微信AppSecret(加密存储)',
  `token` varchar(128) DEFAULT NULL COMMENT '微信Token',
  `aes_key` varchar(128) DEFAULT NULL COMMENT '消息加密密钥',
  `account_type` tinyint NOT NULL DEFAULT '0' COMMENT '类型 0-订阅号 1-服务号',
  `is_verified` tinyint NOT NULL DEFAULT '0' COMMENT '是否认证 0-否 1-是',
  `qr_code_url` varchar(512) DEFAULT NULL COMMENT '公众号二维码URL',
  `access_token` varchar(512) DEFAULT NULL COMMENT '当前AccessToken(加密存储)',
  `access_token_expire_time` datetime DEFAULT NULL COMMENT 'AccessToken过期时间',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态 0-禁用 1-启用',
  `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(64) DEFAULT NULL COMMENT '更新人姓名',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` int NOT NULL DEFAULT '0' COMMENT '删除标记',
  `data_version` int NOT NULL DEFAULT '0' COMMENT '数据版本号',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_id` (`app_id`),
  KEY `idx_tenant` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='公众号账号表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `wechat_oa_article`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wechat_oa_article` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `account_id` bigint NOT NULL COMMENT '公众号ID',
  `title` varchar(256) NOT NULL COMMENT '标题',
  `author` varchar(64) DEFAULT NULL COMMENT '作者',
  `digest` varchar(512) DEFAULT NULL COMMENT '摘要',
  `content` mediumtext COMMENT '正文(HTML)',
  `thumb_media_id` varchar(128) DEFAULT NULL COMMENT '封面素材ID',
  `thumb_url` varchar(512) DEFAULT NULL COMMENT '封面URL',
  `content_source_url` varchar(512) DEFAULT NULL COMMENT '原文链接',
  `wx_media_id` varchar(128) DEFAULT NULL COMMENT '微信端图文素材ID',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态 0-草稿 1-已发布 2-已下线',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `read_count` int NOT NULL DEFAULT '0' COMMENT '阅读数',
  `share_count` int NOT NULL DEFAULT '0' COMMENT '分享数',
  `like_count` int NOT NULL DEFAULT '0' COMMENT '点赞数',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '多图文排序',
  `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(64) DEFAULT NULL COMMENT '更新人姓名',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` int NOT NULL DEFAULT '0' COMMENT '删除标记',
  `data_version` int NOT NULL DEFAULT '0' COMMENT '数据版本号',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_account` (`tenant_id`,`account_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='图文表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `wechat_oa_auto_reply_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wechat_oa_auto_reply_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `account_id` bigint NOT NULL COMMENT '公众号ID',
  `rule_name` varchar(128) NOT NULL COMMENT '规则名称',
  `rule_type` tinyint NOT NULL COMMENT '类型 0-关注回复 1-关键词回复 2-默认回复',
  `keyword` varchar(256) DEFAULT NULL COMMENT '关键词',
  `match_type` tinyint DEFAULT NULL COMMENT '匹配方式 0-全匹配 1-半匹配',
  `reply_type` tinyint NOT NULL COMMENT '回复类型 0-文本 1-图片 2-图文',
  `reply_content` text COMMENT '回复内容',
  `reply_media_id` varchar(128) DEFAULT NULL COMMENT '回复素材ID',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态 0-禁用 1-启用',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(64) DEFAULT NULL COMMENT '更新人姓名',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` int NOT NULL DEFAULT '0' COMMENT '删除标记',
  `data_version` int NOT NULL DEFAULT '0' COMMENT '数据版本号',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_account_type` (`tenant_id`,`account_id`,`rule_type`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='自动回复规则表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `wechat_oa_fan_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wechat_oa_fan_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `account_id` bigint NOT NULL COMMENT '公众号ID',
  `openid` varchar(64) NOT NULL COMMENT '微信OpenID',
  `unionid` varchar(64) DEFAULT NULL COMMENT '微信UnionID',
  `nickname` varchar(128) DEFAULT NULL COMMENT '昵称',
  `avatar_url` varchar(512) DEFAULT NULL COMMENT '头像URL',
  `gender` tinyint DEFAULT '0' COMMENT '性别 0-未知 1-男 2-女',
  `country` varchar(64) DEFAULT NULL COMMENT '国家',
  `province` varchar(64) DEFAULT NULL COMMENT '省份',
  `city` varchar(64) DEFAULT NULL COMMENT '城市',
  `language` varchar(32) DEFAULT NULL COMMENT '语言',
  `subscribe_status` tinyint NOT NULL DEFAULT '1' COMMENT '关注状态 0-已取关 1-已关注',
  `subscribe_time` datetime DEFAULT NULL COMMENT '关注时间',
  `unsubscribe_time` datetime DEFAULT NULL COMMENT '取关时间',
  `subscribe_scene` varchar(64) DEFAULT NULL COMMENT '关注渠道',
  `is_blacklisted` tinyint NOT NULL DEFAULT '0' COMMENT '是否拉黑 0-否 1-是',
  `tag_ids` varchar(512) DEFAULT NULL COMMENT '标签ID列表(JSON)',
  `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(64) DEFAULT NULL COMMENT '更新人姓名',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` int NOT NULL DEFAULT '0' COMMENT '删除标记',
  `data_version` int NOT NULL DEFAULT '0' COMMENT '数据版本号',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account_openid` (`account_id`,`openid`),
  KEY `idx_tenant_account` (`tenant_id`,`account_id`),
  KEY `idx_subscribe` (`subscribe_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='粉丝表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `wechat_oa_material`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wechat_oa_material` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `account_id` bigint NOT NULL COMMENT '公众号ID',
  `media_id` varchar(128) DEFAULT NULL COMMENT '微信素材MediaID',
  `material_type` tinyint NOT NULL COMMENT '类型 0-图片 1-语音 2-视频 3-缩略图',
  `title` varchar(256) DEFAULT NULL COMMENT '素材标题',
  `file_name` varchar(256) DEFAULT NULL COMMENT '原始文件名',
  `file_url` varchar(512) DEFAULT NULL COMMENT '本地存储URL(MinIO)',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小(字节)',
  `wechat_url` varchar(512) DEFAULT NULL COMMENT '微信端URL',
  `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(64) DEFAULT NULL COMMENT '更新人姓名',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` int NOT NULL DEFAULT '0' COMMENT '删除标记',
  `data_version` int NOT NULL DEFAULT '0' COMMENT '数据版本号',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_account` (`tenant_id`,`account_id`),
  KEY `idx_media_id` (`media_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='素材表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `wechat_oa_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wechat_oa_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `account_id` bigint NOT NULL COMMENT '公众号ID',
  `menu_name` varchar(64) NOT NULL COMMENT '菜单名称',
  `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父菜单ID 0-一级',
  `menu_type` varchar(32) NOT NULL COMMENT '菜单类型 click/view/miniprogram等',
  `menu_key` varchar(128) DEFAULT NULL COMMENT '菜单KEY(click类型)',
  `menu_url` varchar(512) DEFAULT NULL COMMENT '菜单URL(view类型)',
  `media_id` varchar(128) DEFAULT NULL COMMENT '素材ID',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(64) DEFAULT NULL COMMENT '更新人姓名',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` int NOT NULL DEFAULT '0' COMMENT '删除标记',
  `data_version` int NOT NULL DEFAULT '0' COMMENT '数据版本号',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_account` (`tenant_id`,`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='公众号菜单表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `wechat_oa_user_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wechat_oa_user_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `account_id` bigint NOT NULL COMMENT '公众号ID',
  `wx_tag_id` int DEFAULT NULL COMMENT '微信端标签ID',
  `tag_name` varchar(64) NOT NULL COMMENT '标签名称',
  `fan_count` int NOT NULL DEFAULT '0' COMMENT '粉丝数',
  `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(64) DEFAULT NULL COMMENT '更新人姓名',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` int NOT NULL DEFAULT '0' COMMENT '删除标记',
  `data_version` int NOT NULL DEFAULT '0' COMMENT '数据版本号',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_account` (`tenant_id`,`account_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='粉丝标签表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 DROP DATABASE IF EXISTS `notify`*/;

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `notify` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `notify`;
DROP TABLE IF EXISTS `notify_channel_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notify_channel_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `channel_type` tinyint NOT NULL COMMENT '渠道 0-站内信 1-邮件 2-飞书 3-钉钉 4-企业微信',
  `enabled` tinyint NOT NULL DEFAULT '0' COMMENT '是否启用 0-否 1-是',
  `config_json` text COMMENT '渠道配置(JSON)',
  `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(64) DEFAULT NULL COMMENT '更新人姓名',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` int NOT NULL DEFAULT '0' COMMENT '删除标记',
  `data_version` int NOT NULL DEFAULT '0' COMMENT '数据版本号',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_channel` (`tenant_id`,`channel_type`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='租户通知渠道配置表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `notify_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notify_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `receiver_id` bigint NOT NULL COMMENT '接收人ID',
  `sender_id` bigint DEFAULT NULL COMMENT '发送人ID',
  `sender_name` varchar(64) DEFAULT NULL COMMENT '发送人姓名',
  `title` varchar(256) NOT NULL COMMENT '消息标题',
  `content` text COMMENT '消息内容',
  `type` tinyint NOT NULL COMMENT '类型 0-系统通知 1-审批通知 2-催办 3-公告',
  `biz_type` varchar(64) DEFAULT NULL COMMENT '业务类型',
  `biz_id` varchar(128) DEFAULT NULL COMMENT '业务ID',
  `jump_url` varchar(512) DEFAULT NULL COMMENT '跳转链接',
  `is_read` tinyint NOT NULL DEFAULT '0' COMMENT '是否已读 0-未读 1-已读',
  `read_time` datetime DEFAULT NULL COMMENT '阅读时间',
  `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(64) DEFAULT NULL COMMENT '更新人姓名',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` int NOT NULL DEFAULT '0' COMMENT '删除标记',
  `data_version` int NOT NULL DEFAULT '0' COMMENT '数据版本号',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_receiver` (`tenant_id`,`receiver_id`,`is_read`),
  KEY `idx_tenant_time` (`tenant_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='站内消息表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `notify_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notify_template` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `template_code` varchar(64) NOT NULL COMMENT '模板编码',
  `template_name` varchar(128) NOT NULL COMMENT '模板名称',
  `type` tinyint NOT NULL COMMENT '渠道 0-站内信 1-邮件 2-IM Webhook',
  `title_template` varchar(256) DEFAULT NULL COMMENT '标题模板',
  `content_template` text COMMENT '内容模板',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态 0-禁用 1-启用',
  `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(64) DEFAULT NULL COMMENT '更新人姓名',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` int NOT NULL DEFAULT '0' COMMENT '删除标记',
  `data_version` int NOT NULL DEFAULT '0' COMMENT '数据版本号',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code_type` (`template_code`,`type`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通知模板表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_sms_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_sms_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ä¸»é”®',
  `phone` varchar(20) NOT NULL COMMENT 'æ‰‹æœºå·',
  `content` varchar(500) NOT NULL COMMENT 'çŸ­ä¿¡å†…å®¹',
  `channel` varchar(32) NOT NULL COMMENT 'çŸ­ä¿¡é€šé“ï¼ˆaliyun/tencent/huaweiï¼‰',
  `template_code` varchar(64) DEFAULT NULL COMMENT 'çŸ­ä¿¡æ¨¡æ¿ç¼–ç ',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT 'å‘é€çŠ¶æ€ 0-å¤±è´¥ 1-æˆåŠŸ',
  `biz_id` varchar(128) DEFAULT NULL COMMENT 'ç¬¬ä¸‰æ–¹æ¶ˆæ¯ID',
  `error_msg` varchar(500) DEFAULT NULL COMMENT 'å¤±è´¥åŽŸå› ',
  `send_time` datetime DEFAULT NULL COMMENT 'å‘é€æ—¶é—´',
  `tenant_id` bigint NOT NULL COMMENT 'ç§Ÿæˆ·ID',
  `create_user_id` varchar(64) DEFAULT NULL COMMENT 'åˆ›å»ºäººID',
  `create_user_name` varchar(64) DEFAULT NULL COMMENT 'åˆ›å»ºäººå§“å',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_user_id` varchar(64) DEFAULT NULL COMMENT 'æ›´æ–°äººID',
  `update_user_name` varchar(64) DEFAULT NULL COMMENT 'æ›´æ–°äººå§“å',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `delete_flag` int NOT NULL DEFAULT '0' COMMENT 'åˆ é™¤æ ‡è®° 0-æ­£å¸¸ 1-åˆ é™¤',
  `data_version` int NOT NULL DEFAULT '0' COMMENT 'ä¹è§‚é”ç‰ˆæœ¬å·',
  `remark` varchar(255) DEFAULT NULL COMMENT 'å¤‡æ³¨',
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  KEY `idx_phone` (`phone`),
  KEY `idx_channel` (`channel`),
  KEY `idx_send_time` (`send_time`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='çŸ­ä¿¡å‘é€æ—¥å¿—è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 DROP DATABASE IF EXISTS `workflow`*/;

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `workflow` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `workflow`;
DROP TABLE IF EXISTS `ACT_EVT_LOG`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ACT_EVT_LOG` (
  `LOG_NR_` bigint NOT NULL AUTO_INCREMENT,
  `TYPE_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `PROC_DEF_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `PROC_INST_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `EXECUTION_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `TASK_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `TIME_STAMP_` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `USER_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `DATA_` longblob,
  `LOCK_OWNER_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `LOCK_TIME_` timestamp(3) NULL DEFAULT NULL,
  `IS_PROCESSED_` tinyint DEFAULT '0',
  PRIMARY KEY (`LOG_NR_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ACT_GE_BYTEARRAY`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ACT_GE_BYTEARRAY` (
  `ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `REV_` int DEFAULT NULL,
  `NAME_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `DEPLOYMENT_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `BYTES_` longblob,
  `GENERATED_` tinyint DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_FK_BYTEARR_DEPL` (`DEPLOYMENT_ID_`),
  CONSTRAINT `ACT_FK_BYTEARR_DEPL` FOREIGN KEY (`DEPLOYMENT_ID_`) REFERENCES `ACT_RE_DEPLOYMENT` (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ACT_GE_PROPERTY`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ACT_GE_PROPERTY` (
  `NAME_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `VALUE_` varchar(300) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `REV_` int DEFAULT NULL,
  PRIMARY KEY (`NAME_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ACT_HI_ACTINST`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ACT_HI_ACTINST` (
  `ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `REV_` int DEFAULT '1',
  `PROC_DEF_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `PROC_INST_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `EXECUTION_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `ACT_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `TASK_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `CALL_PROC_INST_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `ACT_NAME_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `ACT_TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `ASSIGNEE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `START_TIME_` datetime(3) NOT NULL,
  `END_TIME_` datetime(3) DEFAULT NULL,
  `TRANSACTION_ORDER_` int DEFAULT NULL,
  `DURATION_` bigint DEFAULT NULL,
  `DELETE_REASON_` varchar(4000) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `TENANT_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT '',
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_HI_ACT_INST_START` (`START_TIME_`),
  KEY `ACT_IDX_HI_ACT_INST_END` (`END_TIME_`),
  KEY `ACT_IDX_HI_ACT_INST_PROCINST` (`PROC_INST_ID_`,`ACT_ID_`),
  KEY `ACT_IDX_HI_ACT_INST_EXEC` (`EXECUTION_ID_`,`ACT_ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ACT_HI_ATTACHMENT`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ACT_HI_ATTACHMENT` (
  `ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `REV_` int DEFAULT NULL,
  `USER_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `NAME_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `DESCRIPTION_` varchar(4000) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `TASK_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `PROC_INST_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `URL_` varchar(4000) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `CONTENT_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `TIME_` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ACT_HI_COMMENT`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ACT_HI_COMMENT` (
  `ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `TIME_` datetime(3) NOT NULL,
  `USER_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `TASK_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `PROC_INST_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `ACTION_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `MESSAGE_` varchar(4000) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `FULL_MSG_` longblob,
  PRIMARY KEY (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ACT_HI_DETAIL`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ACT_HI_DETAIL` (
  `ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `PROC_INST_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `EXECUTION_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `TASK_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `ACT_INST_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `NAME_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `VAR_TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `REV_` int DEFAULT NULL,
  `TIME_` datetime(3) NOT NULL,
  `BYTEARRAY_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `DOUBLE_` double DEFAULT NULL,
  `LONG_` bigint DEFAULT NULL,
  `TEXT_` varchar(4000) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `TEXT2_` varchar(4000) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_HI_DETAIL_PROC_INST` (`PROC_INST_ID_`),
  KEY `ACT_IDX_HI_DETAIL_ACT_INST` (`ACT_INST_ID_`),
  KEY `ACT_IDX_HI_DETAIL_TIME` (`TIME_`),
  KEY `ACT_IDX_HI_DETAIL_NAME` (`NAME_`),
  KEY `ACT_IDX_HI_DETAIL_TASK_ID` (`TASK_ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ACT_HI_ENTITYLINK`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ACT_HI_ENTITYLINK` (
  `ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `LINK_TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `CREATE_TIME_` datetime(3) DEFAULT NULL,
  `SCOPE_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SUB_SCOPE_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_DEFINITION_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `PARENT_ELEMENT_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `REF_SCOPE_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `REF_SCOPE_TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `REF_SCOPE_DEFINITION_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `ROOT_SCOPE_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `ROOT_SCOPE_TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `HIERARCHY_TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_HI_ENT_LNK_SCOPE` (`SCOPE_ID_`,`SCOPE_TYPE_`,`LINK_TYPE_`),
  KEY `ACT_IDX_HI_ENT_LNK_REF_SCOPE` (`REF_SCOPE_ID_`,`REF_SCOPE_TYPE_`,`LINK_TYPE_`),
  KEY `ACT_IDX_HI_ENT_LNK_ROOT_SCOPE` (`ROOT_SCOPE_ID_`,`ROOT_SCOPE_TYPE_`,`LINK_TYPE_`),
  KEY `ACT_IDX_HI_ENT_LNK_SCOPE_DEF` (`SCOPE_DEFINITION_ID_`,`SCOPE_TYPE_`,`LINK_TYPE_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ACT_HI_IDENTITYLINK`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ACT_HI_IDENTITYLINK` (
  `ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `GROUP_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `USER_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `TASK_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `CREATE_TIME_` datetime(3) DEFAULT NULL,
  `PROC_INST_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SUB_SCOPE_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_DEFINITION_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_HI_IDENT_LNK_USER` (`USER_ID_`),
  KEY `ACT_IDX_HI_IDENT_LNK_SCOPE` (`SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_HI_IDENT_LNK_SUB_SCOPE` (`SUB_SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_HI_IDENT_LNK_SCOPE_DEF` (`SCOPE_DEFINITION_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_HI_IDENT_LNK_TASK` (`TASK_ID_`),
  KEY `ACT_IDX_HI_IDENT_LNK_PROCINST` (`PROC_INST_ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ACT_HI_PROCINST`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ACT_HI_PROCINST` (
  `ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `REV_` int DEFAULT '1',
  `PROC_INST_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `BUSINESS_KEY_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `PROC_DEF_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `START_TIME_` datetime(3) NOT NULL,
  `END_TIME_` datetime(3) DEFAULT NULL,
  `DURATION_` bigint DEFAULT NULL,
  `START_USER_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `START_ACT_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `END_ACT_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SUPER_PROCESS_INSTANCE_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `DELETE_REASON_` varchar(4000) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `TENANT_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT '',
  `NAME_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `CALLBACK_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `CALLBACK_TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `REFERENCE_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `REFERENCE_TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `PROPAGATED_STAGE_INST_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `BUSINESS_STATUS_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  UNIQUE KEY `PROC_INST_ID_` (`PROC_INST_ID_`),
  KEY `ACT_IDX_HI_PRO_INST_END` (`END_TIME_`),
  KEY `ACT_IDX_HI_PRO_I_BUSKEY` (`BUSINESS_KEY_`),
  KEY `ACT_IDX_HI_PRO_SUPER_PROCINST` (`SUPER_PROCESS_INSTANCE_ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ACT_HI_TASKINST`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ACT_HI_TASKINST` (
  `ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `REV_` int DEFAULT '1',
  `PROC_DEF_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `TASK_DEF_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `TASK_DEF_KEY_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `PROC_INST_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `EXECUTION_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SUB_SCOPE_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_DEFINITION_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `PROPAGATED_STAGE_INST_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `NAME_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `PARENT_TASK_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `DESCRIPTION_` varchar(4000) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `OWNER_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `ASSIGNEE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `START_TIME_` datetime(3) NOT NULL,
  `CLAIM_TIME_` datetime(3) DEFAULT NULL,
  `END_TIME_` datetime(3) DEFAULT NULL,
  `DURATION_` bigint DEFAULT NULL,
  `DELETE_REASON_` varchar(4000) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `PRIORITY_` int DEFAULT NULL,
  `DUE_DATE_` datetime(3) DEFAULT NULL,
  `FORM_KEY_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `CATEGORY_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `TENANT_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT '',
  `LAST_UPDATED_TIME_` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_HI_TASK_SCOPE` (`SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_HI_TASK_SUB_SCOPE` (`SUB_SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_HI_TASK_SCOPE_DEF` (`SCOPE_DEFINITION_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_HI_TASK_INST_PROCINST` (`PROC_INST_ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ACT_HI_TSK_LOG`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ACT_HI_TSK_LOG` (
  `ID_` bigint NOT NULL AUTO_INCREMENT,
  `TYPE_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `TASK_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `TIME_STAMP_` timestamp(3) NOT NULL,
  `USER_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `DATA_` varchar(4000) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `EXECUTION_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `PROC_INST_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `PROC_DEF_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_DEFINITION_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SUB_SCOPE_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `TENANT_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT '',
  PRIMARY KEY (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ACT_HI_VARINST`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ACT_HI_VARINST` (
  `ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `REV_` int DEFAULT '1',
  `PROC_INST_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `EXECUTION_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `TASK_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `NAME_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `VAR_TYPE_` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SUB_SCOPE_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `BYTEARRAY_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `DOUBLE_` double DEFAULT NULL,
  `LONG_` bigint DEFAULT NULL,
  `TEXT_` varchar(4000) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `TEXT2_` varchar(4000) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `META_INFO_` varchar(4000) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `CREATE_TIME_` datetime(3) DEFAULT NULL,
  `LAST_UPDATED_TIME_` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_HI_PROCVAR_NAME_TYPE` (`NAME_`,`VAR_TYPE_`),
  KEY `ACT_IDX_HI_VAR_SCOPE_ID_TYPE` (`SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_HI_VAR_SUB_ID_TYPE` (`SUB_SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_HI_PROCVAR_PROC_INST` (`PROC_INST_ID_`),
  KEY `ACT_IDX_HI_PROCVAR_TASK_ID` (`TASK_ID_`),
  KEY `ACT_IDX_HI_PROCVAR_EXE` (`EXECUTION_ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ACT_ID_BYTEARRAY`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ACT_ID_BYTEARRAY` (
  `ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `REV_` int DEFAULT NULL,
  `NAME_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `BYTES_` longblob,
  PRIMARY KEY (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ACT_ID_GROUP`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ACT_ID_GROUP` (
  `ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `REV_` int DEFAULT NULL,
  `NAME_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  PRIMARY KEY (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ACT_ID_INFO`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ACT_ID_INFO` (
  `ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `REV_` int DEFAULT NULL,
  `USER_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `TYPE_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `KEY_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `VALUE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `PASSWORD_` longblob,
  `PARENT_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  PRIMARY KEY (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ACT_ID_MEMBERSHIP`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ACT_ID_MEMBERSHIP` (
  `USER_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `GROUP_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  PRIMARY KEY (`USER_ID_`,`GROUP_ID_`),
  KEY `ACT_FK_MEMB_GROUP` (`GROUP_ID_`),
  CONSTRAINT `ACT_FK_MEMB_GROUP` FOREIGN KEY (`GROUP_ID_`) REFERENCES `ACT_ID_GROUP` (`ID_`),
  CONSTRAINT `ACT_FK_MEMB_USER` FOREIGN KEY (`USER_ID_`) REFERENCES `ACT_ID_USER` (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ACT_ID_PRIV`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ACT_ID_PRIV` (
  `ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `NAME_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  PRIMARY KEY (`ID_`),
  UNIQUE KEY `ACT_UNIQ_PRIV_NAME` (`NAME_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ACT_ID_PRIV_MAPPING`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ACT_ID_PRIV_MAPPING` (
  `ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `PRIV_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `USER_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `GROUP_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_FK_PRIV_MAPPING` (`PRIV_ID_`),
  KEY `ACT_IDX_PRIV_USER` (`USER_ID_`),
  KEY `ACT_IDX_PRIV_GROUP` (`GROUP_ID_`),
  CONSTRAINT `ACT_FK_PRIV_MAPPING` FOREIGN KEY (`PRIV_ID_`) REFERENCES `ACT_ID_PRIV` (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ACT_ID_PROPERTY`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ACT_ID_PROPERTY` (
  `NAME_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `VALUE_` varchar(300) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `REV_` int DEFAULT NULL,
  PRIMARY KEY (`NAME_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ACT_ID_TOKEN`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ACT_ID_TOKEN` (
  `ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `REV_` int DEFAULT NULL,
  `TOKEN_VALUE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `TOKEN_DATE_` timestamp(3) NULL DEFAULT NULL,
  `IP_ADDRESS_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `USER_AGENT_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `USER_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `TOKEN_DATA_` varchar(2000) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  PRIMARY KEY (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ACT_ID_USER`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ACT_ID_USER` (
  `ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `REV_` int DEFAULT NULL,
  `FIRST_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `LAST_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `DISPLAY_NAME_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `EMAIL_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `PWD_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `PICTURE_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `TENANT_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT '',
  PRIMARY KEY (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ACT_PROCDEF_INFO`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ACT_PROCDEF_INFO` (
  `ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `PROC_DEF_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `REV_` int DEFAULT NULL,
  `INFO_JSON_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  UNIQUE KEY `ACT_UNIQ_INFO_PROCDEF` (`PROC_DEF_ID_`),
  KEY `ACT_IDX_INFO_PROCDEF` (`PROC_DEF_ID_`),
  KEY `ACT_FK_INFO_JSON_BA` (`INFO_JSON_ID_`),
  CONSTRAINT `ACT_FK_INFO_JSON_BA` FOREIGN KEY (`INFO_JSON_ID_`) REFERENCES `ACT_GE_BYTEARRAY` (`ID_`),
  CONSTRAINT `ACT_FK_INFO_PROCDEF` FOREIGN KEY (`PROC_DEF_ID_`) REFERENCES `ACT_RE_PROCDEF` (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ACT_RE_DEPLOYMENT`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ACT_RE_DEPLOYMENT` (
  `ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `NAME_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `CATEGORY_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `KEY_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `TENANT_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT '',
  `DEPLOY_TIME_` timestamp(3) NULL DEFAULT NULL,
  `DERIVED_FROM_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `DERIVED_FROM_ROOT_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `PARENT_DEPLOYMENT_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `ENGINE_VERSION_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  PRIMARY KEY (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ACT_RE_MODEL`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ACT_RE_MODEL` (
  `ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `REV_` int DEFAULT NULL,
  `NAME_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `KEY_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `CATEGORY_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `CREATE_TIME_` timestamp(3) NULL DEFAULT NULL,
  `LAST_UPDATE_TIME_` timestamp(3) NULL DEFAULT NULL,
  `VERSION_` int DEFAULT NULL,
  `META_INFO_` varchar(4000) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `DEPLOYMENT_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `EDITOR_SOURCE_VALUE_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `EDITOR_SOURCE_EXTRA_VALUE_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `TENANT_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT '',
  PRIMARY KEY (`ID_`),
  KEY `ACT_FK_MODEL_SOURCE` (`EDITOR_SOURCE_VALUE_ID_`),
  KEY `ACT_FK_MODEL_SOURCE_EXTRA` (`EDITOR_SOURCE_EXTRA_VALUE_ID_`),
  KEY `ACT_FK_MODEL_DEPLOYMENT` (`DEPLOYMENT_ID_`),
  CONSTRAINT `ACT_FK_MODEL_DEPLOYMENT` FOREIGN KEY (`DEPLOYMENT_ID_`) REFERENCES `ACT_RE_DEPLOYMENT` (`ID_`),
  CONSTRAINT `ACT_FK_MODEL_SOURCE` FOREIGN KEY (`EDITOR_SOURCE_VALUE_ID_`) REFERENCES `ACT_GE_BYTEARRAY` (`ID_`),
  CONSTRAINT `ACT_FK_MODEL_SOURCE_EXTRA` FOREIGN KEY (`EDITOR_SOURCE_EXTRA_VALUE_ID_`) REFERENCES `ACT_GE_BYTEARRAY` (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ACT_RE_PROCDEF`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ACT_RE_PROCDEF` (
  `ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `REV_` int DEFAULT NULL,
  `CATEGORY_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `NAME_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `KEY_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `VERSION_` int NOT NULL,
  `DEPLOYMENT_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `RESOURCE_NAME_` varchar(4000) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `DGRM_RESOURCE_NAME_` varchar(4000) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `DESCRIPTION_` varchar(4000) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `HAS_START_FORM_KEY_` tinyint DEFAULT NULL,
  `HAS_GRAPHICAL_NOTATION_` tinyint DEFAULT NULL,
  `SUSPENSION_STATE_` int DEFAULT NULL,
  `TENANT_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT '',
  `ENGINE_VERSION_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `DERIVED_FROM_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `DERIVED_FROM_ROOT_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `DERIVED_VERSION_` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`ID_`),
  UNIQUE KEY `ACT_UNIQ_PROCDEF` (`KEY_`,`VERSION_`,`DERIVED_VERSION_`,`TENANT_ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ACT_RU_ACTINST`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ACT_RU_ACTINST` (
  `ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `REV_` int DEFAULT '1',
  `PROC_DEF_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `PROC_INST_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `EXECUTION_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `ACT_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `TASK_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `CALL_PROC_INST_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `ACT_NAME_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `ACT_TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `ASSIGNEE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `START_TIME_` datetime(3) NOT NULL,
  `END_TIME_` datetime(3) DEFAULT NULL,
  `DURATION_` bigint DEFAULT NULL,
  `TRANSACTION_ORDER_` int DEFAULT NULL,
  `DELETE_REASON_` varchar(4000) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `TENANT_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT '',
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_RU_ACTI_START` (`START_TIME_`),
  KEY `ACT_IDX_RU_ACTI_END` (`END_TIME_`),
  KEY `ACT_IDX_RU_ACTI_PROC` (`PROC_INST_ID_`),
  KEY `ACT_IDX_RU_ACTI_PROC_ACT` (`PROC_INST_ID_`,`ACT_ID_`),
  KEY `ACT_IDX_RU_ACTI_EXEC` (`EXECUTION_ID_`),
  KEY `ACT_IDX_RU_ACTI_EXEC_ACT` (`EXECUTION_ID_`,`ACT_ID_`),
  KEY `ACT_IDX_RU_ACTI_TASK` (`TASK_ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ACT_RU_DEADLETTER_JOB`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ACT_RU_DEADLETTER_JOB` (
  `ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `REV_` int DEFAULT NULL,
  `CATEGORY_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `EXCLUSIVE_` tinyint(1) DEFAULT NULL,
  `EXECUTION_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `PROCESS_INSTANCE_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `PROC_DEF_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `ELEMENT_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `ELEMENT_NAME_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SUB_SCOPE_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_DEFINITION_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `CORRELATION_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `EXCEPTION_STACK_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `EXCEPTION_MSG_` varchar(4000) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `DUEDATE_` timestamp(3) NULL DEFAULT NULL,
  `REPEAT_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `HANDLER_TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `HANDLER_CFG_` varchar(4000) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `CUSTOM_VALUES_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `CREATE_TIME_` timestamp(3) NULL DEFAULT NULL,
  `TENANT_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT '',
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_DEADLETTER_JOB_EXCEPTION_STACK_ID` (`EXCEPTION_STACK_ID_`),
  KEY `ACT_IDX_DEADLETTER_JOB_CUSTOM_VALUES_ID` (`CUSTOM_VALUES_ID_`),
  KEY `ACT_IDX_DEADLETTER_JOB_CORRELATION_ID` (`CORRELATION_ID_`),
  KEY `ACT_IDX_DJOB_SCOPE` (`SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_DJOB_SUB_SCOPE` (`SUB_SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_DJOB_SCOPE_DEF` (`SCOPE_DEFINITION_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_FK_DEADLETTER_JOB_EXECUTION` (`EXECUTION_ID_`),
  KEY `ACT_FK_DEADLETTER_JOB_PROCESS_INSTANCE` (`PROCESS_INSTANCE_ID_`),
  KEY `ACT_FK_DEADLETTER_JOB_PROC_DEF` (`PROC_DEF_ID_`),
  CONSTRAINT `ACT_FK_DEADLETTER_JOB_CUSTOM_VALUES` FOREIGN KEY (`CUSTOM_VALUES_ID_`) REFERENCES `ACT_GE_BYTEARRAY` (`ID_`),
  CONSTRAINT `ACT_FK_DEADLETTER_JOB_EXCEPTION` FOREIGN KEY (`EXCEPTION_STACK_ID_`) REFERENCES `ACT_GE_BYTEARRAY` (`ID_`),
  CONSTRAINT `ACT_FK_DEADLETTER_JOB_EXECUTION` FOREIGN KEY (`EXECUTION_ID_`) REFERENCES `ACT_RU_EXECUTION` (`ID_`),
  CONSTRAINT `ACT_FK_DEADLETTER_JOB_PROC_DEF` FOREIGN KEY (`PROC_DEF_ID_`) REFERENCES `ACT_RE_PROCDEF` (`ID_`),
  CONSTRAINT `ACT_FK_DEADLETTER_JOB_PROCESS_INSTANCE` FOREIGN KEY (`PROCESS_INSTANCE_ID_`) REFERENCES `ACT_RU_EXECUTION` (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ACT_RU_ENTITYLINK`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ACT_RU_ENTITYLINK` (
  `ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `REV_` int DEFAULT NULL,
  `CREATE_TIME_` datetime(3) DEFAULT NULL,
  `LINK_TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SUB_SCOPE_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_DEFINITION_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `PARENT_ELEMENT_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `REF_SCOPE_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `REF_SCOPE_TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `REF_SCOPE_DEFINITION_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `ROOT_SCOPE_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `ROOT_SCOPE_TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `HIERARCHY_TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_ENT_LNK_SCOPE` (`SCOPE_ID_`,`SCOPE_TYPE_`,`LINK_TYPE_`),
  KEY `ACT_IDX_ENT_LNK_REF_SCOPE` (`REF_SCOPE_ID_`,`REF_SCOPE_TYPE_`,`LINK_TYPE_`),
  KEY `ACT_IDX_ENT_LNK_ROOT_SCOPE` (`ROOT_SCOPE_ID_`,`ROOT_SCOPE_TYPE_`,`LINK_TYPE_`),
  KEY `ACT_IDX_ENT_LNK_SCOPE_DEF` (`SCOPE_DEFINITION_ID_`,`SCOPE_TYPE_`,`LINK_TYPE_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ACT_RU_EVENT_SUBSCR`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ACT_RU_EVENT_SUBSCR` (
  `ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `REV_` int DEFAULT NULL,
  `EVENT_TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `EVENT_NAME_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `EXECUTION_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `PROC_INST_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `ACTIVITY_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `CONFIGURATION_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `CREATED_` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `PROC_DEF_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SUB_SCOPE_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_DEFINITION_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_TYPE_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `LOCK_TIME_` timestamp(3) NULL DEFAULT NULL,
  `LOCK_OWNER_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `TENANT_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT '',
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_EVENT_SUBSCR_CONFIG_` (`CONFIGURATION_`),
  KEY `ACT_IDX_EVENT_SUBSCR_SCOPEREF_` (`SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_FK_EVENT_EXEC` (`EXECUTION_ID_`),
  CONSTRAINT `ACT_FK_EVENT_EXEC` FOREIGN KEY (`EXECUTION_ID_`) REFERENCES `ACT_RU_EXECUTION` (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ACT_RU_EXECUTION`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ACT_RU_EXECUTION` (
  `ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `REV_` int DEFAULT NULL,
  `PROC_INST_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `BUSINESS_KEY_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `PARENT_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `PROC_DEF_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SUPER_EXEC_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `ROOT_PROC_INST_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `ACT_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `IS_ACTIVE_` tinyint DEFAULT NULL,
  `IS_CONCURRENT_` tinyint DEFAULT NULL,
  `IS_SCOPE_` tinyint DEFAULT NULL,
  `IS_EVENT_SCOPE_` tinyint DEFAULT NULL,
  `IS_MI_ROOT_` tinyint DEFAULT NULL,
  `SUSPENSION_STATE_` int DEFAULT NULL,
  `CACHED_ENT_STATE_` int DEFAULT NULL,
  `TENANT_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT '',
  `NAME_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `START_ACT_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `START_TIME_` datetime(3) DEFAULT NULL,
  `START_USER_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `LOCK_TIME_` timestamp(3) NULL DEFAULT NULL,
  `LOCK_OWNER_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `IS_COUNT_ENABLED_` tinyint DEFAULT NULL,
  `EVT_SUBSCR_COUNT_` int DEFAULT NULL,
  `TASK_COUNT_` int DEFAULT NULL,
  `JOB_COUNT_` int DEFAULT NULL,
  `TIMER_JOB_COUNT_` int DEFAULT NULL,
  `SUSP_JOB_COUNT_` int DEFAULT NULL,
  `DEADLETTER_JOB_COUNT_` int DEFAULT NULL,
  `EXTERNAL_WORKER_JOB_COUNT_` int DEFAULT NULL,
  `VAR_COUNT_` int DEFAULT NULL,
  `ID_LINK_COUNT_` int DEFAULT NULL,
  `CALLBACK_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `CALLBACK_TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `REFERENCE_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `REFERENCE_TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `PROPAGATED_STAGE_INST_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `BUSINESS_STATUS_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_EXEC_BUSKEY` (`BUSINESS_KEY_`),
  KEY `ACT_IDC_EXEC_ROOT` (`ROOT_PROC_INST_ID_`),
  KEY `ACT_IDX_EXEC_REF_ID_` (`REFERENCE_ID_`),
  KEY `ACT_FK_EXE_PROCINST` (`PROC_INST_ID_`),
  KEY `ACT_FK_EXE_PARENT` (`PARENT_ID_`),
  KEY `ACT_FK_EXE_SUPER` (`SUPER_EXEC_`),
  KEY `ACT_FK_EXE_PROCDEF` (`PROC_DEF_ID_`),
  CONSTRAINT `ACT_FK_EXE_PARENT` FOREIGN KEY (`PARENT_ID_`) REFERENCES `ACT_RU_EXECUTION` (`ID_`) ON DELETE CASCADE,
  CONSTRAINT `ACT_FK_EXE_PROCDEF` FOREIGN KEY (`PROC_DEF_ID_`) REFERENCES `ACT_RE_PROCDEF` (`ID_`),
  CONSTRAINT `ACT_FK_EXE_PROCINST` FOREIGN KEY (`PROC_INST_ID_`) REFERENCES `ACT_RU_EXECUTION` (`ID_`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `ACT_FK_EXE_SUPER` FOREIGN KEY (`SUPER_EXEC_`) REFERENCES `ACT_RU_EXECUTION` (`ID_`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ACT_RU_EXTERNAL_JOB`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ACT_RU_EXTERNAL_JOB` (
  `ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `REV_` int DEFAULT NULL,
  `CATEGORY_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `LOCK_EXP_TIME_` timestamp(3) NULL DEFAULT NULL,
  `LOCK_OWNER_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `EXCLUSIVE_` tinyint(1) DEFAULT NULL,
  `EXECUTION_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `PROCESS_INSTANCE_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `PROC_DEF_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `ELEMENT_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `ELEMENT_NAME_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SUB_SCOPE_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_DEFINITION_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `CORRELATION_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `RETRIES_` int DEFAULT NULL,
  `EXCEPTION_STACK_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `EXCEPTION_MSG_` varchar(4000) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `DUEDATE_` timestamp(3) NULL DEFAULT NULL,
  `REPEAT_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `HANDLER_TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `HANDLER_CFG_` varchar(4000) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `CUSTOM_VALUES_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `CREATE_TIME_` timestamp(3) NULL DEFAULT NULL,
  `TENANT_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT '',
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_EXTERNAL_JOB_EXCEPTION_STACK_ID` (`EXCEPTION_STACK_ID_`),
  KEY `ACT_IDX_EXTERNAL_JOB_CUSTOM_VALUES_ID` (`CUSTOM_VALUES_ID_`),
  KEY `ACT_IDX_EXTERNAL_JOB_CORRELATION_ID` (`CORRELATION_ID_`),
  KEY `ACT_IDX_EJOB_SCOPE` (`SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_EJOB_SUB_SCOPE` (`SUB_SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_EJOB_SCOPE_DEF` (`SCOPE_DEFINITION_ID_`,`SCOPE_TYPE_`),
  CONSTRAINT `ACT_FK_EXTERNAL_JOB_CUSTOM_VALUES` FOREIGN KEY (`CUSTOM_VALUES_ID_`) REFERENCES `ACT_GE_BYTEARRAY` (`ID_`),
  CONSTRAINT `ACT_FK_EXTERNAL_JOB_EXCEPTION` FOREIGN KEY (`EXCEPTION_STACK_ID_`) REFERENCES `ACT_GE_BYTEARRAY` (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ACT_RU_HISTORY_JOB`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ACT_RU_HISTORY_JOB` (
  `ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `REV_` int DEFAULT NULL,
  `LOCK_EXP_TIME_` timestamp(3) NULL DEFAULT NULL,
  `LOCK_OWNER_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `RETRIES_` int DEFAULT NULL,
  `EXCEPTION_STACK_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `EXCEPTION_MSG_` varchar(4000) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `HANDLER_TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `HANDLER_CFG_` varchar(4000) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `CUSTOM_VALUES_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `ADV_HANDLER_CFG_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `CREATE_TIME_` timestamp(3) NULL DEFAULT NULL,
  `SCOPE_TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `TENANT_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT '',
  PRIMARY KEY (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ACT_RU_IDENTITYLINK`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ACT_RU_IDENTITYLINK` (
  `ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `REV_` int DEFAULT NULL,
  `GROUP_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `USER_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `TASK_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `PROC_INST_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `PROC_DEF_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SUB_SCOPE_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_DEFINITION_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_IDENT_LNK_USER` (`USER_ID_`),
  KEY `ACT_IDX_IDENT_LNK_GROUP` (`GROUP_ID_`),
  KEY `ACT_IDX_IDENT_LNK_SCOPE` (`SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_IDENT_LNK_SUB_SCOPE` (`SUB_SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_IDENT_LNK_SCOPE_DEF` (`SCOPE_DEFINITION_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_ATHRZ_PROCEDEF` (`PROC_DEF_ID_`),
  KEY `ACT_FK_TSKASS_TASK` (`TASK_ID_`),
  KEY `ACT_FK_IDL_PROCINST` (`PROC_INST_ID_`),
  CONSTRAINT `ACT_FK_ATHRZ_PROCEDEF` FOREIGN KEY (`PROC_DEF_ID_`) REFERENCES `ACT_RE_PROCDEF` (`ID_`),
  CONSTRAINT `ACT_FK_IDL_PROCINST` FOREIGN KEY (`PROC_INST_ID_`) REFERENCES `ACT_RU_EXECUTION` (`ID_`),
  CONSTRAINT `ACT_FK_TSKASS_TASK` FOREIGN KEY (`TASK_ID_`) REFERENCES `ACT_RU_TASK` (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ACT_RU_JOB`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ACT_RU_JOB` (
  `ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `REV_` int DEFAULT NULL,
  `CATEGORY_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `LOCK_EXP_TIME_` timestamp(3) NULL DEFAULT NULL,
  `LOCK_OWNER_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `EXCLUSIVE_` tinyint(1) DEFAULT NULL,
  `EXECUTION_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `PROCESS_INSTANCE_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `PROC_DEF_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `ELEMENT_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `ELEMENT_NAME_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SUB_SCOPE_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_DEFINITION_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `CORRELATION_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `RETRIES_` int DEFAULT NULL,
  `EXCEPTION_STACK_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `EXCEPTION_MSG_` varchar(4000) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `DUEDATE_` timestamp(3) NULL DEFAULT NULL,
  `REPEAT_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `HANDLER_TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `HANDLER_CFG_` varchar(4000) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `CUSTOM_VALUES_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `CREATE_TIME_` timestamp(3) NULL DEFAULT NULL,
  `TENANT_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT '',
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_JOB_EXCEPTION_STACK_ID` (`EXCEPTION_STACK_ID_`),
  KEY `ACT_IDX_JOB_CUSTOM_VALUES_ID` (`CUSTOM_VALUES_ID_`),
  KEY `ACT_IDX_JOB_CORRELATION_ID` (`CORRELATION_ID_`),
  KEY `ACT_IDX_JOB_SCOPE` (`SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_JOB_SUB_SCOPE` (`SUB_SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_JOB_SCOPE_DEF` (`SCOPE_DEFINITION_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_FK_JOB_EXECUTION` (`EXECUTION_ID_`),
  KEY `ACT_FK_JOB_PROCESS_INSTANCE` (`PROCESS_INSTANCE_ID_`),
  KEY `ACT_FK_JOB_PROC_DEF` (`PROC_DEF_ID_`),
  CONSTRAINT `ACT_FK_JOB_CUSTOM_VALUES` FOREIGN KEY (`CUSTOM_VALUES_ID_`) REFERENCES `ACT_GE_BYTEARRAY` (`ID_`),
  CONSTRAINT `ACT_FK_JOB_EXCEPTION` FOREIGN KEY (`EXCEPTION_STACK_ID_`) REFERENCES `ACT_GE_BYTEARRAY` (`ID_`),
  CONSTRAINT `ACT_FK_JOB_EXECUTION` FOREIGN KEY (`EXECUTION_ID_`) REFERENCES `ACT_RU_EXECUTION` (`ID_`),
  CONSTRAINT `ACT_FK_JOB_PROC_DEF` FOREIGN KEY (`PROC_DEF_ID_`) REFERENCES `ACT_RE_PROCDEF` (`ID_`),
  CONSTRAINT `ACT_FK_JOB_PROCESS_INSTANCE` FOREIGN KEY (`PROCESS_INSTANCE_ID_`) REFERENCES `ACT_RU_EXECUTION` (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ACT_RU_SUSPENDED_JOB`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ACT_RU_SUSPENDED_JOB` (
  `ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `REV_` int DEFAULT NULL,
  `CATEGORY_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `EXCLUSIVE_` tinyint(1) DEFAULT NULL,
  `EXECUTION_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `PROCESS_INSTANCE_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `PROC_DEF_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `ELEMENT_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `ELEMENT_NAME_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SUB_SCOPE_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_DEFINITION_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `CORRELATION_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `RETRIES_` int DEFAULT NULL,
  `EXCEPTION_STACK_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `EXCEPTION_MSG_` varchar(4000) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `DUEDATE_` timestamp(3) NULL DEFAULT NULL,
  `REPEAT_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `HANDLER_TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `HANDLER_CFG_` varchar(4000) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `CUSTOM_VALUES_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `CREATE_TIME_` timestamp(3) NULL DEFAULT NULL,
  `TENANT_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT '',
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_SUSPENDED_JOB_EXCEPTION_STACK_ID` (`EXCEPTION_STACK_ID_`),
  KEY `ACT_IDX_SUSPENDED_JOB_CUSTOM_VALUES_ID` (`CUSTOM_VALUES_ID_`),
  KEY `ACT_IDX_SUSPENDED_JOB_CORRELATION_ID` (`CORRELATION_ID_`),
  KEY `ACT_IDX_SJOB_SCOPE` (`SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_SJOB_SUB_SCOPE` (`SUB_SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_SJOB_SCOPE_DEF` (`SCOPE_DEFINITION_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_FK_SUSPENDED_JOB_EXECUTION` (`EXECUTION_ID_`),
  KEY `ACT_FK_SUSPENDED_JOB_PROCESS_INSTANCE` (`PROCESS_INSTANCE_ID_`),
  KEY `ACT_FK_SUSPENDED_JOB_PROC_DEF` (`PROC_DEF_ID_`),
  CONSTRAINT `ACT_FK_SUSPENDED_JOB_CUSTOM_VALUES` FOREIGN KEY (`CUSTOM_VALUES_ID_`) REFERENCES `ACT_GE_BYTEARRAY` (`ID_`),
  CONSTRAINT `ACT_FK_SUSPENDED_JOB_EXCEPTION` FOREIGN KEY (`EXCEPTION_STACK_ID_`) REFERENCES `ACT_GE_BYTEARRAY` (`ID_`),
  CONSTRAINT `ACT_FK_SUSPENDED_JOB_EXECUTION` FOREIGN KEY (`EXECUTION_ID_`) REFERENCES `ACT_RU_EXECUTION` (`ID_`),
  CONSTRAINT `ACT_FK_SUSPENDED_JOB_PROC_DEF` FOREIGN KEY (`PROC_DEF_ID_`) REFERENCES `ACT_RE_PROCDEF` (`ID_`),
  CONSTRAINT `ACT_FK_SUSPENDED_JOB_PROCESS_INSTANCE` FOREIGN KEY (`PROCESS_INSTANCE_ID_`) REFERENCES `ACT_RU_EXECUTION` (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ACT_RU_TASK`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ACT_RU_TASK` (
  `ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `REV_` int DEFAULT NULL,
  `EXECUTION_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `PROC_INST_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `PROC_DEF_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `TASK_DEF_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SUB_SCOPE_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_DEFINITION_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `PROPAGATED_STAGE_INST_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `NAME_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `PARENT_TASK_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `DESCRIPTION_` varchar(4000) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `TASK_DEF_KEY_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `OWNER_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `ASSIGNEE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `DELEGATION_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `PRIORITY_` int DEFAULT NULL,
  `CREATE_TIME_` timestamp(3) NULL DEFAULT NULL,
  `DUE_DATE_` datetime(3) DEFAULT NULL,
  `CATEGORY_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SUSPENSION_STATE_` int DEFAULT NULL,
  `TENANT_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT '',
  `FORM_KEY_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `CLAIM_TIME_` datetime(3) DEFAULT NULL,
  `IS_COUNT_ENABLED_` tinyint DEFAULT NULL,
  `VAR_COUNT_` int DEFAULT NULL,
  `ID_LINK_COUNT_` int DEFAULT NULL,
  `SUB_TASK_COUNT_` int DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_TASK_CREATE` (`CREATE_TIME_`),
  KEY `ACT_IDX_TASK_SCOPE` (`SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_TASK_SUB_SCOPE` (`SUB_SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_TASK_SCOPE_DEF` (`SCOPE_DEFINITION_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_FK_TASK_EXE` (`EXECUTION_ID_`),
  KEY `ACT_FK_TASK_PROCINST` (`PROC_INST_ID_`),
  KEY `ACT_FK_TASK_PROCDEF` (`PROC_DEF_ID_`),
  CONSTRAINT `ACT_FK_TASK_EXE` FOREIGN KEY (`EXECUTION_ID_`) REFERENCES `ACT_RU_EXECUTION` (`ID_`),
  CONSTRAINT `ACT_FK_TASK_PROCDEF` FOREIGN KEY (`PROC_DEF_ID_`) REFERENCES `ACT_RE_PROCDEF` (`ID_`),
  CONSTRAINT `ACT_FK_TASK_PROCINST` FOREIGN KEY (`PROC_INST_ID_`) REFERENCES `ACT_RU_EXECUTION` (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ACT_RU_TIMER_JOB`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ACT_RU_TIMER_JOB` (
  `ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `REV_` int DEFAULT NULL,
  `CATEGORY_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `LOCK_EXP_TIME_` timestamp(3) NULL DEFAULT NULL,
  `LOCK_OWNER_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `EXCLUSIVE_` tinyint(1) DEFAULT NULL,
  `EXECUTION_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `PROCESS_INSTANCE_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `PROC_DEF_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `ELEMENT_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `ELEMENT_NAME_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SUB_SCOPE_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_DEFINITION_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `CORRELATION_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `RETRIES_` int DEFAULT NULL,
  `EXCEPTION_STACK_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `EXCEPTION_MSG_` varchar(4000) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `DUEDATE_` timestamp(3) NULL DEFAULT NULL,
  `REPEAT_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `HANDLER_TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `HANDLER_CFG_` varchar(4000) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `CUSTOM_VALUES_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `CREATE_TIME_` timestamp(3) NULL DEFAULT NULL,
  `TENANT_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT '',
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_TIMER_JOB_EXCEPTION_STACK_ID` (`EXCEPTION_STACK_ID_`),
  KEY `ACT_IDX_TIMER_JOB_CUSTOM_VALUES_ID` (`CUSTOM_VALUES_ID_`),
  KEY `ACT_IDX_TIMER_JOB_CORRELATION_ID` (`CORRELATION_ID_`),
  KEY `ACT_IDX_TIMER_JOB_DUEDATE` (`DUEDATE_`),
  KEY `ACT_IDX_TJOB_SCOPE` (`SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_TJOB_SUB_SCOPE` (`SUB_SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_TJOB_SCOPE_DEF` (`SCOPE_DEFINITION_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_FK_TIMER_JOB_EXECUTION` (`EXECUTION_ID_`),
  KEY `ACT_FK_TIMER_JOB_PROCESS_INSTANCE` (`PROCESS_INSTANCE_ID_`),
  KEY `ACT_FK_TIMER_JOB_PROC_DEF` (`PROC_DEF_ID_`),
  CONSTRAINT `ACT_FK_TIMER_JOB_CUSTOM_VALUES` FOREIGN KEY (`CUSTOM_VALUES_ID_`) REFERENCES `ACT_GE_BYTEARRAY` (`ID_`),
  CONSTRAINT `ACT_FK_TIMER_JOB_EXCEPTION` FOREIGN KEY (`EXCEPTION_STACK_ID_`) REFERENCES `ACT_GE_BYTEARRAY` (`ID_`),
  CONSTRAINT `ACT_FK_TIMER_JOB_EXECUTION` FOREIGN KEY (`EXECUTION_ID_`) REFERENCES `ACT_RU_EXECUTION` (`ID_`),
  CONSTRAINT `ACT_FK_TIMER_JOB_PROC_DEF` FOREIGN KEY (`PROC_DEF_ID_`) REFERENCES `ACT_RE_PROCDEF` (`ID_`),
  CONSTRAINT `ACT_FK_TIMER_JOB_PROCESS_INSTANCE` FOREIGN KEY (`PROCESS_INSTANCE_ID_`) REFERENCES `ACT_RU_EXECUTION` (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ACT_RU_VARIABLE`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ACT_RU_VARIABLE` (
  `ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `REV_` int DEFAULT NULL,
  `TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `NAME_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `EXECUTION_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `PROC_INST_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `TASK_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SUB_SCOPE_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_TYPE_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `BYTEARRAY_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `DOUBLE_` double DEFAULT NULL,
  `LONG_` bigint DEFAULT NULL,
  `TEXT_` varchar(4000) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `TEXT2_` varchar(4000) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `META_INFO_` varchar(4000) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_RU_VAR_SCOPE_ID_TYPE` (`SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_RU_VAR_SUB_ID_TYPE` (`SUB_SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_FK_VAR_BYTEARRAY` (`BYTEARRAY_ID_`),
  KEY `ACT_IDX_VARIABLE_TASK_ID` (`TASK_ID_`),
  KEY `ACT_FK_VAR_EXE` (`EXECUTION_ID_`),
  KEY `ACT_FK_VAR_PROCINST` (`PROC_INST_ID_`),
  CONSTRAINT `ACT_FK_VAR_BYTEARRAY` FOREIGN KEY (`BYTEARRAY_ID_`) REFERENCES `ACT_GE_BYTEARRAY` (`ID_`),
  CONSTRAINT `ACT_FK_VAR_EXE` FOREIGN KEY (`EXECUTION_ID_`) REFERENCES `ACT_RU_EXECUTION` (`ID_`),
  CONSTRAINT `ACT_FK_VAR_PROCINST` FOREIGN KEY (`PROC_INST_ID_`) REFERENCES `ACT_RU_EXECUTION` (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `FLW_CHANNEL_DEFINITION`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `FLW_CHANNEL_DEFINITION` (
  `ID_` varchar(255) NOT NULL,
  `NAME_` varchar(255) DEFAULT NULL,
  `VERSION_` int DEFAULT NULL,
  `KEY_` varchar(255) DEFAULT NULL,
  `CATEGORY_` varchar(255) DEFAULT NULL,
  `DEPLOYMENT_ID_` varchar(255) DEFAULT NULL,
  `CREATE_TIME_` datetime(3) DEFAULT NULL,
  `TENANT_ID_` varchar(255) DEFAULT NULL,
  `RESOURCE_NAME_` varchar(255) DEFAULT NULL,
  `DESCRIPTION_` varchar(255) DEFAULT NULL,
  `TYPE_` varchar(255) DEFAULT NULL,
  `IMPLEMENTATION_` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  UNIQUE KEY `ACT_IDX_CHANNEL_DEF_UNIQ` (`KEY_`,`VERSION_`,`TENANT_ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `FLW_EV_DATABASECHANGELOG`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `FLW_EV_DATABASECHANGELOG` (
  `ID` varchar(255) NOT NULL,
  `AUTHOR` varchar(255) NOT NULL,
  `FILENAME` varchar(255) NOT NULL,
  `DATEEXECUTED` datetime NOT NULL,
  `ORDEREXECUTED` int NOT NULL,
  `EXECTYPE` varchar(10) NOT NULL,
  `MD5SUM` varchar(35) DEFAULT NULL,
  `DESCRIPTION` varchar(255) DEFAULT NULL,
  `COMMENTS` varchar(255) DEFAULT NULL,
  `TAG` varchar(255) DEFAULT NULL,
  `LIQUIBASE` varchar(20) DEFAULT NULL,
  `CONTEXTS` varchar(255) DEFAULT NULL,
  `LABELS` varchar(255) DEFAULT NULL,
  `DEPLOYMENT_ID` varchar(10) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `FLW_EV_DATABASECHANGELOGLOCK`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `FLW_EV_DATABASECHANGELOGLOCK` (
  `ID` int NOT NULL,
  `LOCKED` bit(1) NOT NULL,
  `LOCKGRANTED` datetime DEFAULT NULL,
  `LOCKEDBY` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `FLW_EVENT_DEFINITION`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `FLW_EVENT_DEFINITION` (
  `ID_` varchar(255) NOT NULL,
  `NAME_` varchar(255) DEFAULT NULL,
  `VERSION_` int DEFAULT NULL,
  `KEY_` varchar(255) DEFAULT NULL,
  `CATEGORY_` varchar(255) DEFAULT NULL,
  `DEPLOYMENT_ID_` varchar(255) DEFAULT NULL,
  `TENANT_ID_` varchar(255) DEFAULT NULL,
  `RESOURCE_NAME_` varchar(255) DEFAULT NULL,
  `DESCRIPTION_` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  UNIQUE KEY `ACT_IDX_EVENT_DEF_UNIQ` (`KEY_`,`VERSION_`,`TENANT_ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `FLW_EVENT_DEPLOYMENT`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `FLW_EVENT_DEPLOYMENT` (
  `ID_` varchar(255) NOT NULL,
  `NAME_` varchar(255) DEFAULT NULL,
  `CATEGORY_` varchar(255) DEFAULT NULL,
  `DEPLOY_TIME_` datetime(3) DEFAULT NULL,
  `TENANT_ID_` varchar(255) DEFAULT NULL,
  `PARENT_DEPLOYMENT_ID_` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `FLW_EVENT_RESOURCE`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `FLW_EVENT_RESOURCE` (
  `ID_` varchar(255) NOT NULL,
  `NAME_` varchar(255) DEFAULT NULL,
  `DEPLOYMENT_ID_` varchar(255) DEFAULT NULL,
  `RESOURCE_BYTES_` longblob,
  PRIMARY KEY (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `FLW_RU_BATCH`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `FLW_RU_BATCH` (
  `ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `REV_` int DEFAULT NULL,
  `TYPE_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `SEARCH_KEY_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SEARCH_KEY2_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `CREATE_TIME_` datetime(3) NOT NULL,
  `COMPLETE_TIME_` datetime(3) DEFAULT NULL,
  `STATUS_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `BATCH_DOC_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `TENANT_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT '',
  PRIMARY KEY (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `FLW_RU_BATCH_PART`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `FLW_RU_BATCH_PART` (
  `ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `REV_` int DEFAULT NULL,
  `BATCH_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `TYPE_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `SCOPE_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SUB_SCOPE_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SCOPE_TYPE_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SEARCH_KEY_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `SEARCH_KEY2_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `CREATE_TIME_` datetime(3) NOT NULL,
  `COMPLETE_TIME_` datetime(3) DEFAULT NULL,
  `STATUS_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `RESULT_DOC_ID_` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `TENANT_ID_` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT '',
  PRIMARY KEY (`ID_`),
  KEY `FLW_IDX_BATCH_PART` (`BATCH_ID_`),
  CONSTRAINT `FLW_FK_BATCH_PART_PARENT` FOREIGN KEY (`BATCH_ID_`) REFERENCES `FLW_RU_BATCH` (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `wf_approval_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wf_approval_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `process_instance_id` varchar(64) NOT NULL COMMENT 'Flowable流程实例ID',
  `process_def_key` varchar(128) DEFAULT NULL COMMENT '流程定义Key (冗余，方便按流程类型统计)',
  `process_name` varchar(256) DEFAULT NULL COMMENT '流程名称',
  `task_id` varchar(64) DEFAULT NULL COMMENT 'Flowable任务ID (流程级操作如撤销/撤回时为NULL)',
  `task_def_key` varchar(128) DEFAULT NULL COMMENT '节点定义Key (流程级操作时为NULL)',
  `task_name` varchar(256) DEFAULT NULL COMMENT '节点名称',
  `operator_id` bigint NOT NULL COMMENT '操作人ID',
  `operator_name` varchar(64) DEFAULT NULL COMMENT '操作人姓名',
  `operator_dept_id` bigint DEFAULT NULL COMMENT '操作人部门ID',
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
  `is_final_decision` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否终态决定: 0-否 1-是',
  `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(64) DEFAULT NULL COMMENT '更新人姓名',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` int NOT NULL DEFAULT '0' COMMENT '删除标记',
  `data_version` int NOT NULL DEFAULT '0' COMMENT '数据版本号',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_operator` (`tenant_id`,`operator_id`),
  KEY `idx_tenant_operator_action` (`tenant_id`,`operator_id`,`action`),
  KEY `idx_process_instance` (`process_instance_id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_action_time` (`tenant_id`,`action_time`),
  KEY `idx_proc_flow_time` (`process_instance_id`,`flow_type`,`action_time`),
  KEY `idx_operator_action_time` (`operator_id`,`action_time`,`delete_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='审批操作记录表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `wf_approval_statistics`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wf_approval_statistics` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `user_name` varchar(64) DEFAULT NULL COMMENT '用户姓名',
  `dept_id` bigint DEFAULT NULL COMMENT '部门ID',
  `dept_name` varchar(128) DEFAULT NULL COMMENT '部门名称',
  `process_def_key` varchar(128) DEFAULT NULL COMMENT '流程定义Key (NULL表示汇总全部)',
  `process_name` varchar(256) DEFAULT NULL COMMENT '流程名称',
  `stat_period` varchar(16) NOT NULL COMMENT '统计周期: MONTH/QUARTER/YEAR',
  `stat_date` date NOT NULL COMMENT '统计日期/周期起始日',
  `total_count` int NOT NULL DEFAULT '0' COMMENT '审批总数',
  `approved_count` int NOT NULL DEFAULT '0' COMMENT '通过数',
  `rejected_count` int NOT NULL DEFAULT '0' COMMENT '驳回数',
  `transferred_count` int NOT NULL DEFAULT '0' COMMENT '转办数',
  `delegated_count` int NOT NULL DEFAULT '0' COMMENT '委派数',
  `avg_duration_ms` bigint DEFAULT NULL COMMENT '平均耗时(ms)',
  `max_duration_ms` bigint DEFAULT NULL COMMENT '最大耗时(ms)',
  `min_duration_ms` bigint DEFAULT NULL COMMENT '最小耗时(ms)',
  `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(64) DEFAULT NULL COMMENT '更新人姓名',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` int NOT NULL DEFAULT '0' COMMENT '删除标记',
  `data_version` int NOT NULL DEFAULT '0' COMMENT '数据版本号',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_user_period_date_process` (`tenant_id`,`user_id`,`stat_period`,`stat_date`,`process_def_key`),
  KEY `idx_tenant_period` (`tenant_id`,`stat_period`,`stat_date`),
  KEY `idx_user_period_date` (`user_id`,`stat_period`,`stat_date`),
  KEY `idx_dept_period_date` (`dept_id`,`stat_period`,`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='审批统计表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `wf_copy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wf_copy` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `process_instance_id` varchar(128) NOT NULL COMMENT '流程实例ID',
  `process_name` varchar(256) DEFAULT NULL COMMENT '流程名称',
  `title` varchar(256) DEFAULT NULL COMMENT '流程标题',
  `initiator_id` bigint NOT NULL COMMENT '发起人ID',
  `initiator_name` varchar(64) DEFAULT NULL COMMENT '发起人姓名',
  `receiver_id` bigint NOT NULL COMMENT '接收人ID',
  `receiver_name` varchar(64) DEFAULT NULL COMMENT '接收人姓名',
  `task_name` varchar(256) DEFAULT NULL COMMENT '发生在哪个节点',
  `is_read` tinyint NOT NULL DEFAULT '0' COMMENT '是否已读 0-未读 1-已读',
  `read_time` datetime DEFAULT NULL COMMENT '阅读时间',
  `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(64) DEFAULT NULL COMMENT '更新人姓名',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` int NOT NULL DEFAULT '0' COMMENT '删除标记',
  `data_version` int NOT NULL DEFAULT '0' COMMENT '数据版本号',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_receiver` (`tenant_id`,`receiver_id`,`is_read`),
  KEY `idx_instance_id` (`process_instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='流程抄送表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `wf_draft`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wf_draft` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
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
  `delete_flag` int NOT NULL DEFAULT '0' COMMENT '删除标记',
  `data_version` int NOT NULL DEFAULT '0' COMMENT '数据版本号',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_business_key` (`tenant_id`,`process_def_key`,`business_key`,`delete_flag`),
  KEY `idx_tenant_user` (`tenant_id`,`create_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='流程草稿表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `wf_node_candidate`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wf_node_candidate` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `node_config_id` bigint NOT NULL COMMENT '节点配置ID (关联 wf_node_config.id)',
  `process_def_key` varchar(128) NOT NULL COMMENT '流程定义Key (冗余)',
  `node_def_key` varchar(128) NOT NULL COMMENT '节点定义Key (冗余)',
  `assign_type` varchar(32) NOT NULL COMMENT '分配策略: USER/ROLE/DEPT/GROUP/EXPRESSION/API/INITIATOR',
  `assign_value` varchar(500) NOT NULL COMMENT '策略值 (用户ID/角色编码/审批组前缀/表达式/API URL)',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(64) DEFAULT NULL COMMENT '更新人姓名',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` int NOT NULL DEFAULT '0' COMMENT '删除标记',
  `data_version` int NOT NULL DEFAULT '0' COMMENT '数据版本号',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  KEY `idx_node_config` (`node_config_id`),
  KEY `idx_process_node` (`process_def_key`,`node_def_key`,`delete_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='节点候选人来源表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `wf_node_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wf_node_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `process_definition_id` varchar(128) NOT NULL COMMENT '流程定义ID',
  `node_id` varchar(128) NOT NULL COMMENT 'BPMN节点ID',
  `node_name` varchar(256) DEFAULT NULL COMMENT '节点名称',
  `assignee_type` tinyint NOT NULL COMMENT '审批人类型 1-指定用户 2-指定角色 3-部门负责人 4-发起人自选',
  `assignee_ids` varchar(1024) DEFAULT NULL COMMENT '审批人/角色ID列表(JSON)',
  `approval_mode` tinyint NOT NULL DEFAULT '1' COMMENT '审批模式 1-或签 2-会签 3-依次',
  `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(64) DEFAULT NULL COMMENT '更新人姓名',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` int NOT NULL DEFAULT '0' COMMENT '删除标记',
  `data_version` int NOT NULL DEFAULT '0' COMMENT '数据版本号',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_def_node` (`process_definition_id`,`node_id`),
  KEY `idx_tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='流程节点审批人配置表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `wf_process_definition_ext`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wf_process_definition_ext` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `process_definition_id` varchar(128) DEFAULT '' COMMENT 'Flowable娴佺▼瀹氫箟ID',
  `process_key` varchar(128) NOT NULL COMMENT '流程标识',
  `process_name` varchar(256) NOT NULL COMMENT '流程名称',
  `category` varchar(64) DEFAULT NULL COMMENT '分类',
  `icon` varchar(512) DEFAULT NULL COMMENT '流程图标URL',
  `description` varchar(1024) DEFAULT NULL COMMENT '流程说明',
  `form_type` tinyint NOT NULL DEFAULT '0' COMMENT '表单类型 0-外链 1-内嵌JSON',
  `form_url` varchar(512) DEFAULT NULL COMMENT '表单URL',
  `form_config` text COMMENT '表单配置(JSON)',
  `is_template` tinyint NOT NULL DEFAULT '0' COMMENT '是否平台模板 0-自定义 1-模板',
  `version` int NOT NULL DEFAULT '1' COMMENT '版本号',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态 0-挂起 1-激活',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `bpmn_xml` mediumtext COMMENT 'BPMN XML鑽夌',
  `model_id` varchar(64) DEFAULT NULL COMMENT 'Flowable Model ID',
  `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(64) DEFAULT NULL COMMENT '更新人姓名',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` int NOT NULL DEFAULT '0' COMMENT '删除标记',
  `data_version` int NOT NULL DEFAULT '0' COMMENT '数据版本号',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_key` (`tenant_id`,`process_key`),
  KEY `idx_flowable_def_id` (`process_definition_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='流程定义扩展表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `wf_process_instance_ext`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wf_process_instance_ext` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `process_instance_id` varchar(128) NOT NULL COMMENT 'Flowable流程实例ID',
  `process_definition_id` varchar(128) NOT NULL COMMENT 'Flowable流程定义ID',
  `process_key` varchar(128) DEFAULT NULL COMMENT '流程标识',
  `process_name` varchar(256) DEFAULT NULL COMMENT '流程名称',
  `title` varchar(256) DEFAULT NULL COMMENT '流程标题',
  `initiator_id` bigint NOT NULL COMMENT '发起人ID',
  `initiator_name` varchar(64) DEFAULT NULL COMMENT '发起人姓名',
  `initiator_dept_id` bigint DEFAULT NULL COMMENT '发起人部门ID',
  `business_key` varchar(256) DEFAULT NULL COMMENT '业务关联键',
  `flow_type` varchar(32) DEFAULT 'NORMAL' COMMENT '流程类型: NORMAL/REJECT/AUTO_SUBMIT',
  `form_data` text COMMENT '表单数据(JSON)',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态 0-进行中 1-已完成 2-已撤回 3-已终止',
  `result` tinyint DEFAULT NULL COMMENT '结果 1-通过 2-驳回',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `duration` bigint DEFAULT NULL COMMENT '耗时(ms)',
  `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(64) DEFAULT NULL COMMENT '更新人姓名',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` int NOT NULL DEFAULT '0' COMMENT '删除标记',
  `data_version` int NOT NULL DEFAULT '0' COMMENT '数据版本号',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_instance_id` (`process_instance_id`),
  KEY `idx_tenant_initiator` (`tenant_id`,`initiator_id`),
  KEY `idx_tenant_status` (`tenant_id`,`status`),
  KEY `idx_tenant_time` (`tenant_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='流程实例扩展表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `wf_task_relation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wf_task_relation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `process_instance_id` varchar(64) NOT NULL COMMENT '流程实例ID',
  `task_id` varchar(64) NOT NULL COMMENT '当前任务ID',
  `parent_task_id` varchar(64) DEFAULT NULL COMMENT '父任务ID',
  `relation_type` varchar(32) NOT NULL COMMENT '关系类型: TRANSFER-转办/DELEGATE-委派/COUNTER_SIGN-加签',
  `operator_id` bigint NOT NULL COMMENT '操作人ID',
  `operator_name` varchar(64) DEFAULT NULL COMMENT '操作人姓名',
  `target_user_id` bigint NOT NULL COMMENT '目标用户ID',
  `target_user_name` varchar(64) DEFAULT NULL COMMENT '目标用户姓名',
  `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(64) DEFAULT NULL COMMENT '更新人姓名',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` int NOT NULL DEFAULT '0' COMMENT '删除标记',
  `data_version` int NOT NULL DEFAULT '0' COMMENT '数据版本号',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_parent` (`parent_task_id`),
  KEY `idx_process_instance` (`process_instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='任务关联关系表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `wf_urge_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wf_urge_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `process_instance_id` varchar(64) NOT NULL COMMENT '流程实例ID',
  `task_id` varchar(64) NOT NULL COMMENT '催办任务ID',
  `urge_user_id` bigint NOT NULL COMMENT '催办人ID',
  `urge_user_name` varchar(64) DEFAULT NULL COMMENT '催办人姓名',
  `target_user_id` bigint NOT NULL COMMENT '被催办人ID',
  `target_user_name` varchar(64) DEFAULT NULL COMMENT '被催办人姓名',
  `urge_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '催办时间',
  `channel` varchar(32) NOT NULL DEFAULT 'IN_APP' COMMENT '催办渠道: IN_APP/SMS/EMAIL',
  `create_user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` varchar(64) DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(64) DEFAULT NULL COMMENT '更新人姓名',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` int NOT NULL DEFAULT '0' COMMENT '删除标记',
  `data_version` int NOT NULL DEFAULT '0' COMMENT '数据版本号',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `valid_status` int NOT NULL DEFAULT '1' COMMENT '有效状态',
  `trace_id` varchar(255) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  KEY `idx_task` (`task_id`),
  KEY `idx_urge_user` (`urge_user_id`),
  KEY `idx_target_user` (`target_user_id`,`urge_time`),
  KEY `idx_proc_inst` (`process_instance_id`,`delete_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='催办记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40000 DROP DATABASE IF EXISTS `xxl_job`*/;

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `xxl_job` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `xxl_job`;
DROP TABLE IF EXISTS `xxl_job_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `xxl_job_group` (
  `id` int NOT NULL AUTO_INCREMENT,
  `app_name` varchar(64) NOT NULL COMMENT '执行器AppName',
  `title` varchar(12) NOT NULL COMMENT '执行器名称',
  `address_type` tinyint NOT NULL DEFAULT '0' COMMENT '执行器地址类型：0=自动注册、1=手动录入',
  `address_list` text COMMENT '执行器地址列表，多地址逗号分隔',
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `xxl_job_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `xxl_job_info` (
  `id` int NOT NULL AUTO_INCREMENT,
  `job_group` int NOT NULL COMMENT '执行器主键ID',
  `job_desc` varchar(255) NOT NULL,
  `add_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `author` varchar(64) DEFAULT NULL COMMENT '作者',
  `alarm_email` varchar(255) DEFAULT NULL COMMENT '报警邮件',
  `schedule_type` varchar(50) NOT NULL DEFAULT 'NONE' COMMENT '调度类型',
  `schedule_conf` varchar(128) DEFAULT NULL COMMENT '调度配置，值含义取决于调度类型',
  `misfire_strategy` varchar(50) NOT NULL DEFAULT 'DO_NOTHING' COMMENT '调度过期策略',
  `executor_route_strategy` varchar(50) DEFAULT NULL COMMENT '执行器路由策略',
  `executor_handler` varchar(255) DEFAULT NULL COMMENT '执行器任务handler',
  `executor_param` varchar(512) DEFAULT NULL COMMENT '执行器任务参数',
  `executor_block_strategy` varchar(50) DEFAULT NULL COMMENT '阻塞处理策略',
  `executor_timeout` int NOT NULL DEFAULT '0' COMMENT '任务执行超时时间，单位秒',
  `executor_fail_retry_count` int NOT NULL DEFAULT '0' COMMENT '失败重试次数',
  `glue_type` varchar(50) NOT NULL COMMENT 'GLUE类型',
  `glue_source` mediumtext COMMENT 'GLUE源代码',
  `glue_remark` varchar(128) DEFAULT NULL COMMENT 'GLUE备注',
  `glue_updatetime` datetime DEFAULT NULL COMMENT 'GLUE更新时间',
  `child_jobid` varchar(255) DEFAULT NULL COMMENT '子任务ID，多个逗号分隔',
  `trigger_status` tinyint NOT NULL DEFAULT '0' COMMENT '调度状态：0-停止，1-运行',
  `trigger_last_time` bigint NOT NULL DEFAULT '0' COMMENT '上次调度时间',
  `trigger_next_time` bigint NOT NULL DEFAULT '0' COMMENT '下次调度时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `xxl_job_lock`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `xxl_job_lock` (
  `lock_name` varchar(50) NOT NULL COMMENT '锁名称',
  PRIMARY KEY (`lock_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `xxl_job_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `xxl_job_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `job_group` int NOT NULL COMMENT '执行器主键ID',
  `job_id` int NOT NULL COMMENT '任务，主键ID',
  `executor_address` varchar(255) DEFAULT NULL COMMENT '执行器地址，本次执行的地址',
  `executor_handler` varchar(255) DEFAULT NULL COMMENT '执行器任务handler',
  `executor_param` varchar(512) DEFAULT NULL COMMENT '执行器任务参数',
  `executor_sharding_param` varchar(20) DEFAULT NULL COMMENT '执行器任务分片参数，格式如 1/2',
  `executor_fail_retry_count` int NOT NULL DEFAULT '0' COMMENT '失败重试次数',
  `trigger_time` datetime DEFAULT NULL COMMENT '调度-时间',
  `trigger_code` int NOT NULL COMMENT '调度-结果',
  `trigger_msg` text COMMENT '调度-日志',
  `handle_time` datetime DEFAULT NULL COMMENT '执行-时间',
  `handle_code` int NOT NULL COMMENT '执行-状态',
  `handle_msg` text COMMENT '执行-日志',
  `alarm_status` tinyint NOT NULL DEFAULT '0' COMMENT '告警状态：0-默认、1-无需告警、2-告警成功、3-告警失败',
  PRIMARY KEY (`id`),
  KEY `I_trigger_time` (`trigger_time`),
  KEY `I_handle_code` (`handle_code`),
  KEY `I_jobid_jobgroup` (`job_id`,`job_group`),
  KEY `I_job_id` (`job_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `xxl_job_log_report`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `xxl_job_log_report` (
  `id` int NOT NULL AUTO_INCREMENT,
  `trigger_day` datetime DEFAULT NULL COMMENT '调度-时间',
  `running_count` int NOT NULL DEFAULT '0' COMMENT '运行中-日志数量',
  `suc_count` int NOT NULL DEFAULT '0' COMMENT '执行成功-日志数量',
  `fail_count` int NOT NULL DEFAULT '0' COMMENT '执行失败-日志数量',
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `i_trigger_day` (`trigger_day`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `xxl_job_logglue`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `xxl_job_logglue` (
  `id` int NOT NULL AUTO_INCREMENT,
  `job_id` int NOT NULL COMMENT '任务，主键ID',
  `glue_type` varchar(50) DEFAULT NULL COMMENT 'GLUE类型',
  `glue_source` mediumtext COMMENT 'GLUE源代码',
  `glue_remark` varchar(128) NOT NULL COMMENT 'GLUE备注',
  `add_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `xxl_job_registry`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `xxl_job_registry` (
  `id` int NOT NULL AUTO_INCREMENT,
  `registry_group` varchar(50) NOT NULL,
  `registry_key` varchar(255) NOT NULL,
  `registry_value` varchar(255) NOT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `i_g_k_v` (`registry_group`,`registry_key`,`registry_value`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `xxl_job_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `xxl_job_user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '账号',
  `password` varchar(50) NOT NULL COMMENT '密码',
  `role` tinyint NOT NULL COMMENT '角色：0-普通用户、1-管理员',
  `permission` varchar(255) DEFAULT NULL COMMENT '权限：执行器ID列表，多个逗号分割',
  PRIMARY KEY (`id`),
  UNIQUE KEY `i_username` (`username`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

USE `platform`;

LOCK TABLES `sys_announcement` WRITE;
/*!40000 ALTER TABLE `sys_announcement` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_announcement` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `sys_api_client` WRITE;
/*!40000 ALTER TABLE `sys_api_client` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_api_client` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `sys_file` WRITE;
/*!40000 ALTER TABLE `sys_file` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_file` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `sys_global_config` WRITE;
/*!40000 ALTER TABLE `sys_global_config` DISABLE KEYS */;
INSERT INTO `sys_global_config` VALUES (1,'default_package_id','1','NUMBER','默认套餐ID',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(2,'trial_days','15','NUMBER','试用天数',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(3,'max_login_attempts','5','NUMBER','最大登录尝试次数',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(4,'password_min_length','8','NUMBER','密码最小长度',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(5,'login_lock_minutes','30','NUMBER','登录锁定时长(分钟)',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL);
/*!40000 ALTER TABLE `sys_global_config` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `sys_package` WRITE;
/*!40000 ALTER TABLE `sys_package` DISABLE KEYS */;
INSERT INTO `sys_package` VALUES (1,'免费版','FREE',0.00,0.00,10,5,10,5,1,1024,NULL,1,1,NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,'免费体验套餐',1,NULL),(2,'基础版','BASIC',299.00,2990.00,50,20,50,20,3,10240,NULL,2,1,NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,'适合小型团队',1,NULL),(3,'专业版','PRO',999.00,9990.00,200,50,200,0,10,102400,NULL,3,1,NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,'适合中型企业',1,NULL),(4,'旗舰版','ENTERPRISE',0.00,0.00,0,0,0,0,0,0,NULL,4,1,NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,'按需定制，所有配额不限',1,NULL);
/*!40000 ALTER TABLE `sys_package` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `sys_platform_user` WRITE;
/*!40000 ALTER TABLE `sys_platform_user` DISABLE KEYS */;
INSERT INTO `sys_platform_user` VALUES (1,'admin','$2a$10$9ahBy1AiHGctthgzt85OL.XVmNbigTHlH1XZGu3SRGbPbRbZeN48S','平台管理员',NULL,NULL,NULL,0,1,NULL,NULL,NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:58:57',0,0,NULL,1,NULL);
/*!40000 ALTER TABLE `sys_platform_user` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `sys_tenant` WRITE;
/*!40000 ALTER TABLE `sys_tenant` DISABLE KEYS */;
INSERT INTO `sys_tenant` VALUES (1,'默认租户','DEFAULT','系统管理员','13800000000','admin@saas-cloud.com',1,4,NULL,NULL,NULL,NULL,1,NULL,NULL,NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,'系统初始化默认租户',1,NULL),(2,'星辰科技','STAR_TECH','张星辰','13900001001','zhangxc@startech.com',1,3,NULL,NULL,NULL,NULL,101,NULL,NULL,NULL,NULL,'2026-05-23 09:48:50',NULL,NULL,'2026-05-23 09:53:30',0,0,'测试租户-专业版',1,NULL),(3,'蓝海集团','BLUE_OCEAN','李蓝海','13900002001','lilh@blueocean.com',1,2,NULL,NULL,NULL,NULL,201,NULL,NULL,NULL,NULL,'2026-05-23 09:48:50',NULL,NULL,'2026-05-23 09:53:30',0,0,'测试租户-基础版',1,NULL);
/*!40000 ALTER TABLE `sys_tenant` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `sys_tenant_order` WRITE;
/*!40000 ALTER TABLE `sys_tenant_order` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_tenant_order` ENABLE KEYS */;
UNLOCK TABLES;

USE `rbac`;

LOCK TABLES `sys_dept` WRITE;
/*!40000 ALTER TABLE `sys_dept` DISABLE KEYS */;
INSERT INTO `sys_dept` VALUES (1,1,'总部',0,'0',1,'系统管理员',NULL,1,1,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(2,1,'技术部',1,'0,1',NULL,NULL,NULL,1,1,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(3,1,'产品部',1,'0,1',NULL,NULL,NULL,2,1,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(4,1,'运营部',1,'0,1',NULL,NULL,NULL,3,1,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(5,1,'人事行政部',1,'0,1',NULL,NULL,NULL,4,1,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(6,1,'TestDept',1,'0,1',NULL,'test','13800000001',10,1,'1','admin','2026-05-19 11:13:54',NULL,NULL,'2026-05-19 03:13:53',0,0,NULL,1,NULL),(7,1,'媒体资源部',0,'0',NULL,NULL,NULL,0,1,'1','admin','2026-05-19 11:18:08','1','admin','2026-05-19 12:44:54',1,0,NULL,1,NULL),(8,1,'TestDept',0,'0',NULL,NULL,NULL,0,1,'1','admin','2026-05-19 12:02:30','1','admin','2026-05-19 12:44:59',1,0,NULL,1,NULL),(101,2,'星辰科技',0,'0',NULL,'张星辰',NULL,0,1,NULL,NULL,'2026-05-23 09:49:18',NULL,NULL,'2026-05-23 10:07:00',0,0,NULL,1,NULL),(102,2,'技术部',101,'0',NULL,'张三',NULL,1,1,NULL,NULL,'2026-05-23 09:49:18',NULL,NULL,'2026-05-23 10:07:00',0,0,NULL,1,NULL),(103,2,'产品部',101,'0',NULL,'李四',NULL,2,1,NULL,NULL,'2026-05-23 09:49:18',NULL,NULL,'2026-05-23 10:07:00',0,0,NULL,1,NULL),(104,2,'运营部',101,'0',NULL,'王五',NULL,3,1,NULL,NULL,'2026-05-23 09:49:18',NULL,NULL,'2026-05-23 10:07:00',0,0,NULL,1,NULL),(201,3,'蓝海集团',0,'0',NULL,'李蓝海',NULL,0,1,NULL,NULL,'2026-05-23 09:49:18',NULL,NULL,'2026-05-23 10:07:00',0,0,NULL,1,NULL),(202,3,'研发中心',201,'0',NULL,'刘一',NULL,1,1,NULL,NULL,'2026-05-23 09:49:18',NULL,NULL,'2026-05-23 10:07:00',0,0,NULL,1,NULL),(203,3,'市场部',201,'0',NULL,'陈七',NULL,2,1,NULL,NULL,'2026-05-23 09:49:18',NULL,NULL,'2026-05-23 10:07:00',0,0,NULL,1,NULL);
/*!40000 ALTER TABLE `sys_dept` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `sys_dict_data` WRITE;
/*!40000 ALTER TABLE `sys_dict_data` DISABLE KEYS */;
INSERT INTO `sys_dict_data` VALUES (1,'sys_user_sex','男','1',1,1,NULL,'primary',1,'1','系统管理员','2026-05-23 08:52:48',NULL,NULL,'2026-05-23 08:52:48',0,0,NULL,1,NULL),(2,'sys_user_sex','女','2',2,1,NULL,'success',1,'1','系统管理员','2026-05-23 08:52:48',NULL,NULL,'2026-05-23 08:52:48',0,0,NULL,1,NULL),(3,'sys_user_sex','未知','0',3,1,NULL,'default',1,'1','系统管理员','2026-05-23 08:52:48',NULL,NULL,'2026-05-23 08:52:48',0,0,NULL,1,NULL),(4,'sys_common_status','启用','1',1,1,NULL,'success',1,'1','系统管理员','2026-05-23 08:52:48',NULL,NULL,'2026-05-23 08:52:48',0,0,NULL,1,NULL),(5,'sys_common_status','禁用','0',2,1,NULL,'danger',1,'1','系统管理员','2026-05-23 08:52:48',NULL,NULL,'2026-05-23 08:52:48',0,0,NULL,1,NULL),(6,'sys_menu_type','目录','0',1,1,NULL,'primary',1,'1','系统管理员','2026-05-23 08:52:48',NULL,NULL,'2026-05-23 08:52:48',0,0,NULL,1,NULL),(7,'sys_menu_type','菜单','1',2,1,NULL,'success',1,'1','系统管理员','2026-05-23 08:52:48',NULL,NULL,'2026-05-23 08:52:48',0,0,NULL,1,NULL),(8,'sys_menu_type','按钮','2',3,1,NULL,'warning',1,'1','系统管理员','2026-05-23 08:52:48',NULL,NULL,'2026-05-23 08:52:48',0,0,NULL,1,NULL),(9,'sys_notice_type','通知','1',1,1,NULL,'primary',1,'1','系统管理员','2026-05-23 08:52:48',NULL,NULL,'2026-05-23 08:52:48',0,0,NULL,1,NULL),(10,'sys_notice_type','公告','2',2,1,NULL,'success',1,'1','系统管理员','2026-05-23 08:52:48',NULL,NULL,'2026-05-23 08:52:48',0,0,NULL,1,NULL),(11,'sys_notice_status','草稿','0',1,1,NULL,'default',1,'1','系统管理员','2026-05-23 08:52:48',NULL,NULL,'2026-05-23 08:52:48',0,0,NULL,1,NULL),(12,'sys_notice_status','已发布','1',2,1,NULL,'success',1,'1','系统管理员','2026-05-23 08:52:48',NULL,NULL,'2026-05-23 08:52:48',0,0,NULL,1,NULL),(13,'sys_notice_status','已撤回','2',3,1,NULL,'warning',1,'1','系统管理员','2026-05-23 08:52:48',NULL,NULL,'2026-05-23 08:52:48',0,0,NULL,1,NULL),(14,'sys_login_status','成功','1',1,1,NULL,'success',1,'1','系统管理员','2026-05-23 08:52:48',NULL,NULL,'2026-05-23 08:52:48',0,0,NULL,1,NULL),(15,'sys_login_status','失败','0',2,1,NULL,'danger',1,'1','系统管理员','2026-05-23 08:52:48',NULL,NULL,'2026-05-23 08:52:48',0,0,NULL,1,NULL),(16,'sys_oper_type','查询','1',1,1,NULL,'primary',1,'1','系统管理员','2026-05-23 08:52:48',NULL,NULL,'2026-05-23 08:52:48',0,0,NULL,1,NULL),(17,'sys_oper_type','新增','2',2,1,NULL,'success',1,'1','系统管理员','2026-05-23 08:52:48',NULL,NULL,'2026-05-23 08:52:48',0,0,NULL,1,NULL),(18,'sys_oper_type','修改','3',3,1,NULL,'warning',1,'1','系统管理员','2026-05-23 08:52:48',NULL,NULL,'2026-05-23 08:52:48',0,0,NULL,1,NULL),(19,'sys_oper_type','删除','4',4,1,NULL,'danger',1,'1','系统管理员','2026-05-23 08:52:48',NULL,NULL,'2026-05-23 08:52:48',0,0,NULL,1,NULL),(20,'sys_oper_type','导出','5',5,1,NULL,'default',1,'1','系统管理员','2026-05-23 08:52:48',NULL,NULL,'2026-05-23 08:52:48',0,0,NULL,1,NULL),(21,'sys_oper_type','导入','6',6,1,NULL,'default',1,'1','系统管理员','2026-05-23 08:52:48',NULL,NULL,'2026-05-23 08:52:48',0,0,NULL,1,NULL),(22,'sys_yes_no','是','1',1,1,NULL,'success',1,'1','系统管理员','2026-05-23 08:52:48',NULL,NULL,'2026-05-23 08:52:48',0,0,NULL,1,NULL),(23,'sys_yes_no','否','0',2,1,NULL,'danger',1,'1','系统管理员','2026-05-23 08:52:48',NULL,NULL,'2026-05-23 08:52:48',0,0,NULL,1,NULL),(24,'sys_data_scope','全部数据','1',1,1,NULL,'default',1,'1','系统管理员','2026-05-23 08:52:48',NULL,NULL,'2026-05-23 08:52:48',0,0,NULL,1,NULL),(25,'sys_data_scope','本部门数据','2',2,1,NULL,'default',1,'1','系统管理员','2026-05-23 08:52:48',NULL,NULL,'2026-05-23 08:52:48',0,0,NULL,1,NULL),(26,'sys_data_scope','本部门及以下','3',3,1,NULL,'default',1,'1','系统管理员','2026-05-23 08:52:48',NULL,NULL,'2026-05-23 08:52:48',0,0,NULL,1,NULL),(27,'sys_data_scope','仅本人数据','4',4,1,NULL,'default',1,'1','系统管理员','2026-05-23 08:52:48',NULL,NULL,'2026-05-23 08:52:48',0,0,NULL,1,NULL),(28,'sys_data_scope','自定义','5',5,1,NULL,'default',1,'1','系统管理员','2026-05-23 08:52:48',NULL,NULL,'2026-05-23 08:52:48',0,0,NULL,1,NULL),(29,'sys_task_status','排队中','0',1,1,NULL,'default',1,'1','系统管理员','2026-05-23 08:52:48',NULL,NULL,'2026-05-23 08:52:48',0,0,NULL,1,NULL),(30,'sys_task_status','处理中','1',2,1,NULL,'processing',1,'1','系统管理员','2026-05-23 08:52:48',NULL,NULL,'2026-05-23 08:52:48',0,0,NULL,1,NULL),(31,'sys_task_status','成功','2',3,1,NULL,'success',1,'1','系统管理员','2026-05-23 08:52:48',NULL,NULL,'2026-05-23 08:52:48',0,0,NULL,1,NULL),(32,'sys_task_status','失败','3',4,1,NULL,'danger',1,'1','系统管理员','2026-05-23 08:52:48',NULL,NULL,'2026-05-23 08:52:48',0,0,NULL,1,NULL);
/*!40000 ALTER TABLE `sys_dict_data` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `sys_dict_type` WRITE;
/*!40000 ALTER TABLE `sys_dict_type` DISABLE KEYS */;
INSERT INTO `sys_dict_type` VALUES (1,'用户性别','sys_user_sex',1,1,'1','系统管理员','2026-05-23 08:52:18',NULL,NULL,'2026-05-23 08:52:18',0,0,NULL,1,NULL),(2,'系统状态','sys_common_status',1,1,'1','系统管理员','2026-05-23 08:52:18',NULL,NULL,'2026-05-23 08:52:18',0,0,NULL,1,NULL),(3,'菜单类型','sys_menu_type',1,1,'1','系统管理员','2026-05-23 08:52:18',NULL,NULL,'2026-05-23 08:52:18',0,0,NULL,1,NULL),(4,'通知类型','sys_notice_type',1,1,'1','系统管理员','2026-05-23 08:52:18',NULL,NULL,'2026-05-23 08:52:18',0,0,NULL,1,NULL),(5,'通知状态','sys_notice_status',1,1,'1','系统管理员','2026-05-23 08:52:18',NULL,NULL,'2026-05-23 08:52:18',0,0,NULL,1,NULL),(6,'登录状态','sys_login_status',1,1,'1','系统管理员','2026-05-23 08:52:18',NULL,NULL,'2026-05-23 08:52:18',0,0,NULL,1,NULL),(7,'操作类型','sys_oper_type',1,1,'1','系统管理员','2026-05-23 08:52:18',NULL,NULL,'2026-05-23 08:52:18',0,0,NULL,1,NULL),(8,'是否','sys_yes_no',1,1,'1','系统管理员','2026-05-23 08:52:18',NULL,NULL,'2026-05-23 08:52:18',0,0,NULL,1,NULL),(9,'数据权限范围','sys_data_scope',1,1,'1','系统管理员','2026-05-23 08:52:18',NULL,NULL,'2026-05-23 08:52:18',0,0,NULL,1,NULL),(10,'任务状态','sys_task_status',1,1,'1','系统管理员','2026-05-23 08:52:18',NULL,NULL,'2026-05-23 08:52:18',0,0,NULL,1,NULL);
/*!40000 ALTER TABLE `sys_dict_type` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `sys_menu` WRITE;
/*!40000 ALTER TABLE `sys_menu` DISABLE KEYS */;
INSERT INTO `sys_menu` VALUES (1,NULL,'仪表盘',0,0,'/dashboard','BasicLayout',NULL,'lucide:layout-dashboard',1,1,1,0,0,'RBAC',NULL,NULL,'2026-05-19 01:31:27','1',NULL,'2026-05-19 12:13:53',1,0,NULL,1,NULL),(2,NULL,'系统管理',0,0,'/system','BasicLayout',NULL,'lucide:settings',10,1,1,0,0,'RBAC',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(3,NULL,'流程管理',0,0,'/workflow','BasicLayout',NULL,'lucide:git-branch',20,1,1,0,0,'WORKFLOW',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(4,NULL,'公众号管理',0,0,'/wechat','BasicLayout',NULL,'lucide:message-circle',30,1,1,0,0,'WECHAT_OA',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(5,NULL,'通知管理',0,0,'/notify','BasicLayout',NULL,'lucide:bell',40,1,1,0,0,'RBAC',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(6,NULL,'系统监控',0,0,'/monitor','BasicLayout',NULL,'lucide:monitor',15,1,1,0,0,'RBAC',NULL,NULL,'2026-05-23 07:47:26',NULL,NULL,'2026-05-23 07:47:26',0,0,NULL,1,NULL),(101,NULL,'分析页',1,1,'/dashboard/analytics','/views/dashboard/analytics/index',NULL,'lucide:bar-chart-3',1,1,1,0,0,'RBAC',NULL,NULL,'2026-05-19 01:31:27','1',NULL,'2026-05-19 12:13:53',1,0,NULL,1,NULL),(102,NULL,'工作台',1,1,'/dashboard/workspace','/views/dashboard/workspace/index',NULL,'lucide:monitor',2,1,1,0,0,'RBAC',NULL,NULL,'2026-05-19 01:31:27','1',NULL,'2026-05-19 12:13:53',1,0,NULL,1,NULL),(201,NULL,'用户管理',2,1,'/system/user','/views/system/user/index',NULL,'lucide:users',1,1,1,0,0,'RBAC',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(202,NULL,'角色管理',2,1,'/system/role','/views/system/role/index',NULL,'lucide:shield',2,1,1,0,0,'RBAC',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(203,NULL,'部门管理',2,1,'/system/dept','/views/system/dept/index',NULL,'lucide:building',3,1,1,0,0,'RBAC',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(204,NULL,'个人设置',2,1,'/system/profile','/views/system/profile/index',NULL,'lucide:user-cog',4,1,1,0,0,'RBAC',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(205,NULL,'操作日志',2,1,'/system/log','/views/system/log/index',NULL,'lucide:file-clock',5,1,1,0,0,'RBAC',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(206,NULL,'代码生成',2,1,'/system/generator','/views/tool/generator/index',NULL,'lucide:code',6,1,1,0,0,'RBAC',NULL,NULL,'2026-05-19 09:00:52',NULL,NULL,'2026-05-19 09:00:52',0,0,NULL,1,NULL),(207,NULL,'字典管理',2,1,'/system/dict','/views/system/dict/index',NULL,'lucide:book-open',7,1,1,0,0,'RBAC',NULL,NULL,'2026-05-23 07:47:26',NULL,NULL,'2026-05-23 07:47:26',0,0,NULL,1,NULL),(208,NULL,'岗位管理',2,1,'/system/post','/views/system/post/index',NULL,'lucide:briefcase',8,1,1,0,0,'RBAC',NULL,NULL,'2026-05-23 07:47:26',NULL,NULL,'2026-05-23 07:47:26',0,0,NULL,1,NULL),(209,NULL,'通知公告',2,1,'/system/notice','/views/system/notice/index',NULL,'lucide:megaphone',9,1,1,0,0,'RBAC',NULL,NULL,'2026-05-23 07:47:26',NULL,NULL,'2026-05-23 07:47:26',0,0,NULL,1,NULL),(210,NULL,'登录日志',2,1,'/system/login-log','/views/system/login-log/index',NULL,'lucide:log-in',10,1,1,0,0,'RBAC',NULL,NULL,'2026-05-23 07:47:26',NULL,NULL,'2026-05-23 07:47:26',0,0,NULL,1,NULL),(211,NULL,'在线用户',2,1,'/system/online-user','/views/system/online-user/index',NULL,'lucide:wifi',11,1,1,0,0,'RBAC',NULL,NULL,'2026-05-23 07:47:26',NULL,NULL,'2026-05-23 07:47:26',0,0,NULL,1,NULL),(212,NULL,'下载中心',2,1,'/system/export-task','/views/system/export-task/index',NULL,'lucide:download',12,1,1,0,0,'RBAC',NULL,NULL,'2026-05-23 07:47:26',NULL,NULL,'2026-05-23 07:47:26',0,0,NULL,1,NULL),(213,NULL,'敏感词管理',2,1,'/system/sensitive-word','/views/system/sensitive-word/index',NULL,'lucide:shield-alert',13,1,1,0,0,'RBAC',NULL,NULL,'2026-05-23 07:47:26',NULL,NULL,'2026-05-23 07:47:26',0,0,NULL,1,NULL),(214,NULL,'租户信息',2,1,'/system/tenant-self','/views/system/tenant-self/index',NULL,'lucide:home',14,1,1,0,0,'RBAC',NULL,NULL,'2026-05-23 07:47:26',NULL,NULL,'2026-05-23 07:47:26',0,0,NULL,1,NULL),(215,NULL,'菜单管理',2,1,'/system/menu','/views/system/menu/index',NULL,'lucide:list-tree',4,1,1,0,0,'RBAC',NULL,NULL,'2026-05-23 07:55:42',NULL,NULL,'2026-05-23 07:55:42',0,0,NULL,1,NULL),(301,NULL,'流程定义',3,1,'/workflow/definition','/views/workflow/definition/index',NULL,'lucide:file-text',1,1,1,0,0,'WORKFLOW',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(302,NULL,'发起流程',3,1,'/workflow/start','/views/workflow/start/index',NULL,'lucide:play-circle',2,1,1,0,0,'WORKFLOW',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(303,NULL,'我的待办',3,1,'/workflow/todo','/views/workflow/todo/index',NULL,'lucide:clock',3,1,1,0,0,'WORKFLOW',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(304,NULL,'我的已办',3,1,'/workflow/done','/views/workflow/done/index',NULL,'lucide:check-circle',4,1,1,0,0,'WORKFLOW',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(305,NULL,'我发起的',3,1,'/workflow/initiated','/views/workflow/initiated/index',NULL,'lucide:send',5,1,1,0,0,'WORKFLOW',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(306,NULL,'流程监控',3,1,'/workflow/monitor','/views/workflow/monitor/index',NULL,'lucide:activity',6,1,1,0,0,'WORKFLOW',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(401,NULL,'账号管理',4,1,'/wechat/account','/views/wechat/account/index',NULL,'lucide:key',1,1,1,0,0,'WECHAT_OA',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(402,NULL,'素材管理',4,1,'/wechat/material','/views/wechat/material/index',NULL,'lucide:image',2,1,1,0,0,'WECHAT_OA',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(403,NULL,'图文管理',4,1,'/wechat/article','/views/wechat/article/index',NULL,'lucide:newspaper',3,1,1,0,0,'WECHAT_OA',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(404,NULL,'粉丝管理',4,1,'/wechat/fan','/views/wechat/fan/index',NULL,'lucide:heart',4,1,1,0,0,'WECHAT_OA',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(405,NULL,'标签管理',4,1,'/wechat/tag','/views/wechat/tag/index',NULL,'lucide:tag',5,1,1,0,0,'WECHAT_OA',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(406,NULL,'自动回复',4,1,'/wechat/auto-reply','/views/wechat/auto-reply/index',NULL,'lucide:reply',6,1,1,0,0,'WECHAT_OA',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(407,NULL,'菜单编辑',4,1,'/wechat/menu','/views/wechat/menu/index',NULL,'lucide:menu',7,1,1,0,0,'WECHAT_OA',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(408,NULL,'数据看板',4,1,'/wechat/dashboard','/views/wechat/dashboard/index',NULL,'lucide:pie-chart',8,1,1,0,0,'WECHAT_OA',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(501,NULL,'站内消息',5,1,'/notify/message','/views/notify/message/index',NULL,'lucide:mail',1,1,1,0,0,'RBAC',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(502,NULL,'通知模板',5,1,'/notify/template','/views/notify/template/index',NULL,'lucide:file-template',2,1,1,0,0,'RBAC',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(503,NULL,'渠道配置',5,1,'/notify/channel','/views/notify/channel/index',NULL,'lucide:radio',3,1,1,0,0,'RBAC',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(601,NULL,'缓存监控',6,1,'/monitor/cache','/views/monitor/cache/index',NULL,'lucide:database',1,1,1,0,0,'RBAC',NULL,NULL,'2026-05-23 07:47:26',NULL,NULL,'2026-05-23 07:47:26',0,0,NULL,1,NULL),(602,NULL,'服务器监控',6,1,'/monitor/server','/views/monitor/server/index',NULL,'lucide:server',2,1,1,0,0,'RBAC',NULL,NULL,'2026-05-23 07:47:26',NULL,NULL,'2026-05-23 07:47:26',0,0,NULL,1,NULL),(2011,NULL,'用户查询',201,2,NULL,NULL,'system:user:query',NULL,1,1,1,0,0,'RBAC',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(2012,NULL,'用户新增',201,2,NULL,NULL,'system:user:create',NULL,2,1,1,0,0,'RBAC',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(2013,NULL,'用户编辑',201,2,NULL,NULL,'system:user:update',NULL,3,1,1,0,0,'RBAC',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(2014,NULL,'用户删除',201,2,NULL,NULL,'system:user:delete',NULL,4,1,1,0,0,'RBAC',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(2015,NULL,'重置密码',201,2,NULL,NULL,'system:user:resetpwd',NULL,5,1,1,0,0,'RBAC',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(2016,NULL,'用户导出',201,2,NULL,NULL,'system:user:export',NULL,6,1,1,0,0,'RBAC',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(2021,NULL,'角色查询',202,2,NULL,NULL,'system:role:query',NULL,1,1,1,0,0,'RBAC',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(2022,NULL,'角色新增',202,2,NULL,NULL,'system:role:create',NULL,2,1,1,0,0,'RBAC',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(2023,NULL,'角色编辑',202,2,NULL,NULL,'system:role:update',NULL,3,1,1,0,0,'RBAC',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(2024,NULL,'角色删除',202,2,NULL,NULL,'system:role:delete',NULL,4,1,1,0,0,'RBAC',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(2025,NULL,'分配权限',202,2,NULL,NULL,'system:role:assign',NULL,5,1,1,0,0,'RBAC',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(2031,NULL,'部门查询',203,2,NULL,NULL,'system:dept:query',NULL,1,1,1,0,0,'RBAC',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(2032,NULL,'部门新增',203,2,NULL,NULL,'system:dept:create',NULL,2,1,1,0,0,'RBAC',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(2033,NULL,'部门编辑',203,2,NULL,NULL,'system:dept:update',NULL,3,1,1,0,0,'RBAC',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(2034,NULL,'部门删除',203,2,NULL,NULL,'system:dept:delete',NULL,4,1,1,0,0,'RBAC',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(2051,NULL,'日志查询',205,2,NULL,NULL,'system:log:query',NULL,1,1,1,0,0,'RBAC',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(2052,NULL,'日志导出',205,2,NULL,NULL,'system:log:export',NULL,2,1,1,0,0,'RBAC',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(2053,NULL,'日志删除',205,2,NULL,NULL,'system:log:delete',NULL,3,1,1,0,0,'RBAC',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(2071,NULL,'字典查询',207,2,NULL,NULL,'system:dict:list',NULL,1,1,1,0,0,'RBAC',NULL,NULL,'2026-05-23 07:47:26',NULL,NULL,'2026-05-23 07:47:26',0,0,NULL,1,NULL),(2072,NULL,'字典新增',207,2,NULL,NULL,'system:dict:add',NULL,2,1,1,0,0,'RBAC',NULL,NULL,'2026-05-23 07:47:26',NULL,NULL,'2026-05-23 07:47:26',0,0,NULL,1,NULL),(2073,NULL,'字典修改',207,2,NULL,NULL,'system:dict:edit',NULL,3,1,1,0,0,'RBAC',NULL,NULL,'2026-05-23 07:47:26',NULL,NULL,'2026-05-23 07:47:26',0,0,NULL,1,NULL),(2074,NULL,'字典删除',207,2,NULL,NULL,'system:dict:delete',NULL,4,1,1,0,0,'RBAC',NULL,NULL,'2026-05-23 07:47:26',NULL,NULL,'2026-05-23 07:47:26',0,0,NULL,1,NULL),(2081,NULL,'岗位查询',208,2,NULL,NULL,'system:post:list',NULL,1,1,1,0,0,'RBAC',NULL,NULL,'2026-05-23 07:47:26',NULL,NULL,'2026-05-23 07:47:26',0,0,NULL,1,NULL),(2082,NULL,'岗位新增',208,2,NULL,NULL,'system:post:add',NULL,2,1,1,0,0,'RBAC',NULL,NULL,'2026-05-23 07:47:26',NULL,NULL,'2026-05-23 07:47:26',0,0,NULL,1,NULL),(2083,NULL,'岗位修改',208,2,NULL,NULL,'system:post:edit',NULL,3,1,1,0,0,'RBAC',NULL,NULL,'2026-05-23 07:47:26',NULL,NULL,'2026-05-23 07:47:26',0,0,NULL,1,NULL),(2084,NULL,'岗位删除',208,2,NULL,NULL,'system:post:delete',NULL,4,1,1,0,0,'RBAC',NULL,NULL,'2026-05-23 07:47:26',NULL,NULL,'2026-05-23 07:47:26',0,0,NULL,1,NULL),(2091,NULL,'公告查询',209,2,NULL,NULL,'system:notice:list',NULL,1,1,1,0,0,'RBAC',NULL,NULL,'2026-05-23 07:47:26',NULL,NULL,'2026-05-23 07:47:26',0,0,NULL,1,NULL),(2092,NULL,'公告新增',209,2,NULL,NULL,'system:notice:add',NULL,2,1,1,0,0,'RBAC',NULL,NULL,'2026-05-23 07:47:26',NULL,NULL,'2026-05-23 07:47:26',0,0,NULL,1,NULL),(2093,NULL,'公告修改',209,2,NULL,NULL,'system:notice:edit',NULL,3,1,1,0,0,'RBAC',NULL,NULL,'2026-05-23 07:47:26',NULL,NULL,'2026-05-23 07:47:26',0,0,NULL,1,NULL),(2094,NULL,'公告删除',209,2,NULL,NULL,'system:notice:delete',NULL,4,1,1,0,0,'RBAC',NULL,NULL,'2026-05-23 07:47:26',NULL,NULL,'2026-05-23 07:47:26',0,0,NULL,1,NULL),(2095,NULL,'公告发布',209,2,NULL,NULL,'system:notice:publish',NULL,5,1,1,0,0,'RBAC',NULL,NULL,'2026-05-23 07:47:26',NULL,NULL,'2026-05-23 07:47:26',0,0,NULL,1,NULL),(2101,NULL,'日志查询',210,2,NULL,NULL,'system:loginlog:list',NULL,1,1,1,0,0,'RBAC',NULL,NULL,'2026-05-23 07:47:26',NULL,NULL,'2026-05-23 07:47:26',0,0,NULL,1,NULL),(2102,NULL,'日志清理',210,2,NULL,NULL,'system:loginlog:clean',NULL,2,1,1,0,0,'RBAC',NULL,NULL,'2026-05-23 07:47:26',NULL,NULL,'2026-05-23 07:47:26',0,0,NULL,1,NULL),(2111,NULL,'在线查询',211,2,NULL,NULL,'system:online:list',NULL,1,1,1,0,0,'RBAC',NULL,NULL,'2026-05-23 07:47:26',NULL,NULL,'2026-05-23 07:47:26',0,0,NULL,1,NULL),(2112,NULL,'强制下线',211,2,NULL,NULL,'system:online:kick',NULL,2,1,1,0,0,'RBAC',NULL,NULL,'2026-05-23 07:47:26',NULL,NULL,'2026-05-23 07:47:26',0,0,NULL,1,NULL),(2131,NULL,'敏感词查询',213,2,NULL,NULL,'system:sensitiveword:list',NULL,1,1,1,0,0,'RBAC',NULL,NULL,'2026-05-23 07:47:26',NULL,NULL,'2026-05-23 07:47:26',0,0,NULL,1,NULL),(2132,NULL,'敏感词新增',213,2,NULL,NULL,'system:sensitiveword:add',NULL,2,1,1,0,0,'RBAC',NULL,NULL,'2026-05-23 07:47:26',NULL,NULL,'2026-05-23 07:47:26',0,0,NULL,1,NULL),(2133,NULL,'敏感词删除',213,2,NULL,NULL,'system:sensitiveword:delete',NULL,3,1,1,0,0,'RBAC',NULL,NULL,'2026-05-23 07:47:26',NULL,NULL,'2026-05-23 07:47:26',0,0,NULL,1,NULL),(2151,NULL,'新增菜单',215,2,NULL,NULL,'system:menu:add',NULL,1,1,1,0,0,'RBAC',NULL,NULL,'2026-05-23 07:55:42',NULL,NULL,'2026-05-23 07:55:42',0,0,NULL,1,NULL),(2152,NULL,'编辑菜单',215,2,NULL,NULL,'system:menu:edit',NULL,2,1,1,0,0,'RBAC',NULL,NULL,'2026-05-23 07:55:42',NULL,NULL,'2026-05-23 07:55:42',0,0,NULL,1,NULL),(2153,NULL,'删除菜单',215,2,NULL,NULL,'system:menu:delete',NULL,3,1,1,0,0,'RBAC',NULL,NULL,'2026-05-23 07:55:42',NULL,NULL,'2026-05-23 07:55:42',0,0,NULL,1,NULL),(3011,NULL,'流程定义查询',301,2,NULL,NULL,'workflow:definition:query',NULL,1,1,1,0,0,'WORKFLOW',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(3012,NULL,'流程定义新增',301,2,NULL,NULL,'workflow:definition:create',NULL,2,1,1,0,0,'WORKFLOW',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(3013,NULL,'流程定义编辑',301,2,NULL,NULL,'workflow:definition:update',NULL,3,1,1,0,0,'WORKFLOW',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(3014,NULL,'流程定义删除',301,2,NULL,NULL,'workflow:definition:delete',NULL,4,1,1,0,0,'WORKFLOW',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(3015,NULL,'流程部署',301,2,NULL,NULL,'workflow:definition:deploy',NULL,5,1,1,0,0,'WORKFLOW',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(3061,NULL,'流程监控查询',306,2,NULL,NULL,'workflow:monitor:query',NULL,1,1,1,0,0,'WORKFLOW',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(3062,NULL,'流程终止',306,2,NULL,NULL,'workflow:monitor:terminate',NULL,2,1,1,0,0,'WORKFLOW',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(4011,NULL,'账号查询',401,2,NULL,NULL,'wechat:account:query',NULL,1,1,1,0,0,'WECHAT_OA',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(4012,NULL,'账号新增',401,2,NULL,NULL,'wechat:account:create',NULL,2,1,1,0,0,'WECHAT_OA',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(4013,NULL,'账号编辑',401,2,NULL,NULL,'wechat:account:update',NULL,3,1,1,0,0,'WECHAT_OA',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(4014,NULL,'账号删除',401,2,NULL,NULL,'wechat:account:delete',NULL,4,1,1,0,0,'WECHAT_OA',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(4021,NULL,'素材查询',402,2,NULL,NULL,'wechat:material:query',NULL,1,1,1,0,0,'WECHAT_OA',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(4022,NULL,'素材上传',402,2,NULL,NULL,'wechat:material:upload',NULL,2,1,1,0,0,'WECHAT_OA',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(4023,NULL,'素材删除',402,2,NULL,NULL,'wechat:material:delete',NULL,3,1,1,0,0,'WECHAT_OA',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(4031,NULL,'图文查询',403,2,NULL,NULL,'wechat:article:query',NULL,1,1,1,0,0,'WECHAT_OA',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(4032,NULL,'图文新增',403,2,NULL,NULL,'wechat:article:create',NULL,2,1,1,0,0,'WECHAT_OA',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(4033,NULL,'图文编辑',403,2,NULL,NULL,'wechat:article:update',NULL,3,1,1,0,0,'WECHAT_OA',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(4034,NULL,'图文删除',403,2,NULL,NULL,'wechat:article:delete',NULL,4,1,1,0,0,'WECHAT_OA',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(4035,NULL,'图文发布',403,2,NULL,NULL,'wechat:article:publish',NULL,5,1,1,0,0,'WECHAT_OA',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(4041,NULL,'粉丝查询',404,2,NULL,NULL,'wechat:fan:query',NULL,1,1,1,0,0,'WECHAT_OA',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(4042,NULL,'粉丝拉黑',404,2,NULL,NULL,'wechat:fan:blacklist',NULL,2,1,1,0,0,'WECHAT_OA',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(4043,NULL,'粉丝同步',404,2,NULL,NULL,'wechat:fan:sync',NULL,3,1,1,0,0,'WECHAT_OA',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(4051,NULL,'标签查询',405,2,NULL,NULL,'wechat:tag:query',NULL,1,1,1,0,0,'WECHAT_OA',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(4052,NULL,'标签新增',405,2,NULL,NULL,'wechat:tag:create',NULL,2,1,1,0,0,'WECHAT_OA',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(4053,NULL,'标签删除',405,2,NULL,NULL,'wechat:tag:delete',NULL,3,1,1,0,0,'WECHAT_OA',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(4061,NULL,'回复规则查询',406,2,NULL,NULL,'wechat:reply:query',NULL,1,1,1,0,0,'WECHAT_OA',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(4062,NULL,'回复规则新增',406,2,NULL,NULL,'wechat:reply:create',NULL,2,1,1,0,0,'WECHAT_OA',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(4063,NULL,'回复规则编辑',406,2,NULL,NULL,'wechat:reply:update',NULL,3,1,1,0,0,'WECHAT_OA',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(4064,NULL,'回复规则删除',406,2,NULL,NULL,'wechat:reply:delete',NULL,4,1,1,0,0,'WECHAT_OA',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(4071,NULL,'菜单查询',407,2,NULL,NULL,'wechat:menu:query',NULL,1,1,1,0,0,'WECHAT_OA',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(4072,NULL,'菜单编辑',407,2,NULL,NULL,'wechat:menu:update',NULL,2,1,1,0,0,'WECHAT_OA',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(4073,NULL,'菜单同步',407,2,NULL,NULL,'wechat:menu:sync',NULL,3,1,1,0,0,'WECHAT_OA',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(5011,NULL,'消息查询',501,2,NULL,NULL,'notify:message:query',NULL,1,1,1,0,0,'RBAC',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(5012,NULL,'消息删除',501,2,NULL,NULL,'notify:message:delete',NULL,2,1,1,0,0,'RBAC',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(5021,NULL,'模板查询',502,2,NULL,NULL,'notify:template:query',NULL,1,1,1,0,0,'RBAC',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(5022,NULL,'模板新增',502,2,NULL,NULL,'notify:template:create',NULL,2,1,1,0,0,'RBAC',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(5023,NULL,'模板编辑',502,2,NULL,NULL,'notify:template:update',NULL,3,1,1,0,0,'RBAC',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(5024,NULL,'模板删除',502,2,NULL,NULL,'notify:template:delete',NULL,4,1,1,0,0,'RBAC',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(5031,NULL,'渠道查询',503,2,NULL,NULL,'notify:channel:query',NULL,1,1,1,0,0,'RBAC',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(5032,NULL,'渠道编辑',503,2,NULL,NULL,'notify:channel:update',NULL,2,1,1,0,0,'RBAC',NULL,NULL,'2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(5033,1,'Dashboard',0,0,'/dashboard','BasicLayout',NULL,'lucide:layout-dashboard',1,1,0,0,0,NULL,'1',NULL,'2026-05-19 12:28:06','1',NULL,'2026-05-19 12:43:01',1,0,NULL,1,NULL),(5034,1,'Analytics',5033,1,'/dashboard/analytics','/views/dashboard/analytics/index',NULL,'lucide:bar-chart-3',1,1,0,0,0,NULL,'1',NULL,'2026-05-19 12:28:29','1',NULL,'2026-05-19 12:43:01',1,0,NULL,1,NULL),(5035,1,'Workspace',5033,1,'/dashboard/workspace','/views/dashboard/workspace/index',NULL,'carbon:workspace',2,1,0,0,0,NULL,'1',NULL,'2026-05-19 12:28:29','1',NULL,'2026-05-19 12:43:01',1,0,NULL,1,NULL);
/*!40000 ALTER TABLE `sys_menu` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `sys_notice` WRITE;
/*!40000 ALTER TABLE `sys_notice` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_notice` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `sys_notice_read` WRITE;
/*!40000 ALTER TABLE `sys_notice_read` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_notice_read` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `sys_post` WRITE;
/*!40000 ALTER TABLE `sys_post` DISABLE KEYS */;
INSERT INTO `sys_post` VALUES (1,1,'CEO','总经理',1,1,'1','系统管理员','2026-05-23 08:52:57',NULL,NULL,'2026-05-23 08:52:57',0,0,NULL,1,NULL),(2,1,'CTO','技术总监',2,1,'1','系统管理员','2026-05-23 08:52:57',NULL,NULL,'2026-05-23 08:52:57',0,0,NULL,1,NULL),(3,1,'PM','项目经理',3,1,'1','系统管理员','2026-05-23 08:52:57',NULL,NULL,'2026-05-23 08:52:57',0,0,NULL,1,NULL),(4,1,'DEV','开发工程师',4,1,'1','系统管理员','2026-05-23 08:52:57',NULL,NULL,'2026-05-23 08:52:57',0,0,NULL,1,NULL),(5,1,'HR','人事专员',5,1,'1','系统管理员','2026-05-23 08:52:57',NULL,NULL,'2026-05-23 08:52:57',0,0,NULL,1,NULL),(6,1,'FIN','财务专员',6,1,'1','系统管理员','2026-05-23 08:52:57',NULL,NULL,'2026-05-23 08:52:57',0,0,NULL,1,NULL);
/*!40000 ALTER TABLE `sys_post` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `sys_role` WRITE;
/*!40000 ALTER TABLE `sys_role` DISABLE KEYS */;
INSERT INTO `sys_role` VALUES (1,1,'超级管理员','SUPER_ADMIN',0,1,1,1,1,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,'拥有全部权限，不可删除',1,NULL),(2,1,'管理员','ADMIN',1,1,2,1,1,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,'管理员角色，可管理用户和部门',1,NULL),(3,1,'普通用户','USER',2,4,3,1,1,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,'普通用户角色，仅查看权限',1,NULL),(4,1,'TestRole','test_role2',2,4,99,1,0,'1','admin','2026-05-19 11:12:24',NULL,NULL,'2026-05-19 03:12:22',0,0,NULL,1,NULL),(5,1,'总监','ZONGJIAN',2,4,0,1,0,'1','admin','2026-05-19 11:17:12','1','admin','2026-05-19 12:42:54',1,0,NULL,1,NULL),(101,2,'超级管理','tenant_admin',0,1,0,1,1,NULL,NULL,'2026-05-23 09:49:27',NULL,NULL,'2026-05-23 10:07:05',0,0,'星辰科技超管',1,NULL),(102,2,'管理员','admin',1,2,1,1,1,NULL,NULL,'2026-05-23 09:49:27',NULL,NULL,'2026-05-23 10:07:05',0,0,'星辰科技管理员',1,NULL),(103,2,'普通用户','user',2,4,2,1,1,NULL,NULL,'2026-05-23 09:49:27',NULL,NULL,'2026-05-23 10:07:05',0,0,'星辰科技普通用户',1,NULL),(201,3,'超级管理','tenant_admin',0,1,0,1,1,NULL,NULL,'2026-05-23 09:49:27',NULL,NULL,'2026-05-23 10:07:05',0,0,'蓝海集团超管',1,NULL),(202,3,'管理员','admin',1,2,1,1,1,NULL,NULL,'2026-05-23 09:49:27',NULL,NULL,'2026-05-23 10:07:05',0,0,'蓝海集团管理员',1,NULL),(203,3,'普通用户','user',2,4,2,1,1,NULL,NULL,'2026-05-23 09:49:27',NULL,NULL,'2026-05-23 10:07:05',0,0,'蓝海集团普通用户',1,NULL);
/*!40000 ALTER TABLE `sys_role` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `sys_role_dept` WRITE;
/*!40000 ALTER TABLE `sys_role_dept` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_role_dept` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `sys_role_menu` WRITE;
/*!40000 ALTER TABLE `sys_role_menu` DISABLE KEYS */;
INSERT INTO `sys_role_menu` VALUES (1,1,1,1,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(2,1,1,2,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(3,1,1,3,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(4,1,1,4,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(5,1,1,5,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(6,1,1,101,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(7,1,1,102,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(8,1,1,201,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(9,1,1,202,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(10,1,1,203,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(11,1,1,204,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(12,1,1,205,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(13,1,1,301,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(14,1,1,302,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(15,1,1,303,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(16,1,1,304,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(17,1,1,305,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(18,1,1,306,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(19,1,1,401,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(20,1,1,402,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(21,1,1,403,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(22,1,1,404,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(23,1,1,405,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(24,1,1,406,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(25,1,1,407,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(26,1,1,408,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(27,1,1,501,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(28,1,1,502,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(29,1,1,503,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(30,1,1,2011,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(31,1,1,2012,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(32,1,1,2013,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(33,1,1,2014,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(34,1,1,2015,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(35,1,1,2016,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(36,1,1,2021,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(37,1,1,2022,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(38,1,1,2023,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(39,1,1,2024,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(40,1,1,2025,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(41,1,1,2031,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(42,1,1,2032,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(43,1,1,2033,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(44,1,1,2034,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(45,1,1,2051,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(46,1,1,2052,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(47,1,1,2053,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(48,1,1,3011,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(49,1,1,3012,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(50,1,1,3013,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(51,1,1,3014,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(52,1,1,3015,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(53,1,1,3061,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(54,1,1,3062,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(55,1,1,4011,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(56,1,1,4012,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(57,1,1,4013,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(58,1,1,4014,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(59,1,1,4021,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(60,1,1,4022,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(61,1,1,4023,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(62,1,1,4031,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(63,1,1,4032,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(64,1,1,4033,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(65,1,1,4034,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(66,1,1,4035,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(67,1,1,4041,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(68,1,1,4042,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(69,1,1,4043,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(70,1,1,4051,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(71,1,1,4052,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(72,1,1,4053,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(73,1,1,4061,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(74,1,1,4062,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(75,1,1,4063,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(76,1,1,4064,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(77,1,1,4071,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(78,1,1,4072,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(79,1,1,4073,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(80,1,1,5011,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(81,1,1,5012,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(82,1,1,5021,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(83,1,1,5022,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(84,1,1,5023,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(85,1,1,5024,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(86,1,1,5031,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(87,1,1,5032,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(88,1,2,1,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(89,1,2,2,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(90,1,2,3,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(91,1,2,5,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(92,1,2,101,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(93,1,2,102,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(94,1,2,201,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(95,1,2,202,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(96,1,2,203,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(97,1,2,204,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(98,1,2,205,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(99,1,2,301,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(100,1,2,302,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(101,1,2,303,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(102,1,2,304,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(103,1,2,305,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(104,1,2,501,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(105,1,2,502,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(106,1,2,503,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(107,1,2,2011,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(108,1,2,2012,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(109,1,2,2013,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(110,1,2,2014,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(111,1,2,2015,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(112,1,2,2021,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(113,1,2,2022,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(114,1,2,2023,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(115,1,2,2024,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(116,1,2,2031,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(117,1,2,2032,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(118,1,2,2033,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(119,1,2,2034,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(120,1,2,2051,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(121,1,2,3011,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(122,1,2,5011,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(123,1,2,5021,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(124,1,2,5022,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(125,1,2,5023,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(126,1,2,5024,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(127,1,2,5031,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(128,1,2,5032,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(129,1,3,1,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(130,1,3,2,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(131,1,3,3,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(132,1,3,5,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(133,1,3,101,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(134,1,3,102,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(135,1,3,204,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(136,1,3,302,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(137,1,3,303,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(138,1,3,304,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(139,1,3,305,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(140,1,3,501,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(141,1,3,5011,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(142,1,1,6,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(143,1,1,207,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(144,1,1,208,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(145,1,1,209,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(146,1,1,210,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(147,1,1,211,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(148,1,1,212,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(149,1,1,213,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(150,1,1,214,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(151,1,1,601,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(152,1,1,602,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(153,1,1,2071,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(154,1,1,2072,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(155,1,1,2073,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(156,1,1,2074,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(157,1,1,2081,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(158,1,1,2082,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(159,1,1,2083,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(160,1,1,2084,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(161,1,1,2091,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(162,1,1,2092,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(163,1,1,2093,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(164,1,1,2094,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(165,1,1,2095,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(166,1,1,2101,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(167,1,1,2102,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(168,1,1,2111,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(169,1,1,2112,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(170,1,1,2131,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(171,1,1,2132,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(172,1,1,2133,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(173,1,2,6,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(174,1,2,207,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(175,1,2,208,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(176,1,2,209,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(177,1,2,210,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(178,1,2,211,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(179,1,2,212,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(180,1,2,213,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(181,1,2,214,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(182,1,2,601,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(183,1,2,602,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(184,1,2,2071,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(185,1,2,2072,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(186,1,2,2073,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(187,1,2,2074,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(188,1,2,2081,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(189,1,2,2082,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(190,1,2,2083,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(191,1,2,2084,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(192,1,2,2091,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(193,1,2,2092,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(194,1,2,2093,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(195,1,2,2094,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(196,1,2,2095,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(197,1,2,2101,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(198,1,2,2102,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(199,1,2,2111,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(200,1,2,2112,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(201,1,2,2131,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(202,1,2,2132,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(203,1,2,2133,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(204,1,3,212,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(205,1,3,214,NULL,NULL,'2026-05-23 07:48:25',NULL,NULL,'2026-05-23 07:48:25',0,0,NULL,1,NULL),(206,1,1,215,NULL,NULL,'2026-05-23 07:55:57',NULL,NULL,'2026-05-23 07:55:57',0,0,NULL,1,NULL),(207,1,1,2151,NULL,NULL,'2026-05-23 07:55:57',NULL,NULL,'2026-05-23 07:55:57',0,0,NULL,1,NULL),(208,1,1,2152,NULL,NULL,'2026-05-23 07:55:57',NULL,NULL,'2026-05-23 07:55:57',0,0,NULL,1,NULL),(209,1,1,2153,NULL,NULL,'2026-05-23 07:55:57',NULL,NULL,'2026-05-23 07:55:57',0,0,NULL,1,NULL),(210,1,2,215,NULL,NULL,'2026-05-23 07:55:57',NULL,NULL,'2026-05-23 07:55:57',0,0,NULL,1,NULL),(211,1,2,2151,NULL,NULL,'2026-05-23 07:55:57',NULL,NULL,'2026-05-23 07:55:57',0,0,NULL,1,NULL),(212,1,2,2152,NULL,NULL,'2026-05-23 07:55:57',NULL,NULL,'2026-05-23 07:55:57',0,0,NULL,1,NULL),(213,1,2,2153,NULL,NULL,'2026-05-23 07:55:57',NULL,NULL,'2026-05-23 07:55:57',0,0,NULL,1,NULL),(214,2,101,1,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(215,2,101,2,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(216,2,101,3,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(217,2,101,4,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(218,2,101,5,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(219,2,101,101,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(220,2,101,102,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(221,2,101,201,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(222,2,101,202,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(223,2,101,203,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(224,2,101,204,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(225,2,101,205,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(226,2,101,301,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(227,2,101,302,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(228,2,101,303,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(229,2,101,304,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(230,2,101,305,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(231,2,101,306,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(232,2,101,401,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(233,2,101,402,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(234,2,101,403,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(235,2,101,404,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(236,2,101,405,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(237,2,101,406,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(238,2,101,407,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(239,2,101,408,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(240,2,101,501,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(241,2,101,502,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(242,2,101,503,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(243,2,101,2011,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(244,2,101,2012,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(245,2,101,2013,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(246,2,101,2014,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(247,2,101,2015,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(248,2,101,2016,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(249,2,101,2021,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(250,2,101,2022,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(251,2,101,2023,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(252,2,101,2024,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(253,2,101,2025,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(254,2,101,2031,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(255,2,101,2032,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(256,2,101,2033,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(257,2,101,2034,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(258,2,101,2051,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(259,2,101,2052,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(260,2,101,2053,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(261,2,101,3011,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(262,2,101,3012,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(263,2,101,3013,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(264,2,101,3014,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(265,2,101,3015,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(266,2,101,3061,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(267,2,101,3062,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(268,2,101,4011,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(269,2,101,4012,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(270,2,101,4013,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(271,2,101,4014,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(272,2,101,4021,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(273,2,101,4022,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(274,2,101,4023,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(275,2,101,4031,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(276,2,101,4032,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(277,2,101,4033,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(278,2,101,4034,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(279,2,101,4035,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(280,2,101,4041,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(281,2,101,4042,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(282,2,101,4043,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(283,2,101,4051,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(284,2,101,4052,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(285,2,101,4053,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(286,2,101,4061,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(287,2,101,4062,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(288,2,101,4063,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(289,2,101,4064,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(290,2,101,4071,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(291,2,101,4072,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(292,2,101,4073,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(293,2,101,5011,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(294,2,101,5012,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(295,2,101,5021,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(296,2,101,5022,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(297,2,101,5023,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(298,2,101,5024,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(299,2,101,5031,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(300,2,101,5032,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(301,2,101,6,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(302,2,101,207,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(303,2,101,208,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(304,2,101,209,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(305,2,101,210,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(306,2,101,211,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(307,2,101,212,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(308,2,101,213,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(309,2,101,214,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(310,2,101,601,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(311,2,101,602,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(312,2,101,2071,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(313,2,101,2072,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(314,2,101,2073,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(315,2,101,2074,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(316,2,101,2081,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(317,2,101,2082,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(318,2,101,2083,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(319,2,101,2084,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(320,2,101,2091,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(321,2,101,2092,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(322,2,101,2093,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(323,2,101,2094,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(324,2,101,2095,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(325,2,101,2101,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(326,2,101,2102,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(327,2,101,2111,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(328,2,101,2112,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(329,2,101,2131,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(330,2,101,2132,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(331,2,101,2133,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(332,2,101,215,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(333,2,101,2151,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(334,2,101,2152,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(335,2,101,2153,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(341,2,102,1,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(342,2,102,2,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(343,2,102,3,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(344,2,102,5,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(345,2,102,101,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(346,2,102,102,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(347,2,102,201,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(348,2,102,202,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(349,2,102,203,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(350,2,102,204,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(351,2,102,205,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(352,2,102,301,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(353,2,102,302,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(354,2,102,303,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(355,2,102,304,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(356,2,102,305,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(357,2,102,501,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(358,2,102,502,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(359,2,102,503,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(360,2,102,2011,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(361,2,102,2012,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(362,2,102,2013,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(363,2,102,2014,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(364,2,102,2015,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(365,2,102,2021,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(366,2,102,2022,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(367,2,102,2023,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(368,2,102,2024,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(369,2,102,2031,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(370,2,102,2032,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(371,2,102,2033,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(372,2,102,2034,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(373,2,102,2051,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(374,2,102,3011,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(375,2,102,5011,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(376,2,102,5021,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(377,2,102,5022,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(378,2,102,5023,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(379,2,102,5024,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(380,2,102,5031,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(381,2,102,5032,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(382,2,102,6,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(383,2,102,207,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(384,2,102,208,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(385,2,102,209,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(386,2,102,210,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(387,2,102,211,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(388,2,102,212,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(389,2,102,213,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(390,2,102,214,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(391,2,102,601,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(392,2,102,602,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(393,2,102,2071,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(394,2,102,2072,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(395,2,102,2073,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(396,2,102,2074,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(397,2,102,2081,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(398,2,102,2082,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(399,2,102,2083,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(400,2,102,2084,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(401,2,102,2091,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(402,2,102,2092,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(403,2,102,2093,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(404,2,102,2094,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(405,2,102,2095,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(406,2,102,2101,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(407,2,102,2102,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(408,2,102,2111,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(409,2,102,2112,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(410,2,102,2131,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(411,2,102,2132,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(412,2,102,2133,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(413,2,102,215,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(414,2,102,2151,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(415,2,102,2152,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(416,2,102,2153,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(468,2,103,1,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(469,2,103,2,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(470,2,103,3,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(471,2,103,5,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(472,2,103,101,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(473,2,103,102,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(474,2,103,204,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(475,2,103,302,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(476,2,103,303,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(477,2,103,304,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(478,2,103,305,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(479,2,103,501,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(480,2,103,5011,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(481,2,103,212,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(482,2,103,214,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(483,3,201,1,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(484,3,201,2,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(485,3,201,3,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(486,3,201,4,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(487,3,201,5,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(488,3,201,101,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(489,3,201,102,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(490,3,201,201,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(491,3,201,202,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(492,3,201,203,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(493,3,201,204,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(494,3,201,205,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(495,3,201,301,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(496,3,201,302,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(497,3,201,303,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(498,3,201,304,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(499,3,201,305,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(500,3,201,306,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(501,3,201,401,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(502,3,201,402,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(503,3,201,403,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(504,3,201,404,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(505,3,201,405,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(506,3,201,406,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(507,3,201,407,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(508,3,201,408,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(509,3,201,501,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(510,3,201,502,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(511,3,201,503,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(512,3,201,2011,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(513,3,201,2012,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(514,3,201,2013,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(515,3,201,2014,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(516,3,201,2015,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(517,3,201,2016,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(518,3,201,2021,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(519,3,201,2022,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(520,3,201,2023,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(521,3,201,2024,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(522,3,201,2025,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(523,3,201,2031,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(524,3,201,2032,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(525,3,201,2033,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(526,3,201,2034,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(527,3,201,2051,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(528,3,201,2052,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(529,3,201,2053,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(530,3,201,3011,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(531,3,201,3012,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(532,3,201,3013,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(533,3,201,3014,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(534,3,201,3015,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(535,3,201,3061,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(536,3,201,3062,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(537,3,201,4011,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(538,3,201,4012,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(539,3,201,4013,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(540,3,201,4014,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(541,3,201,4021,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(542,3,201,4022,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(543,3,201,4023,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(544,3,201,4031,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(545,3,201,4032,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(546,3,201,4033,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(547,3,201,4034,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(548,3,201,4035,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(549,3,201,4041,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(550,3,201,4042,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(551,3,201,4043,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(552,3,201,4051,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(553,3,201,4052,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(554,3,201,4053,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(555,3,201,4061,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(556,3,201,4062,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(557,3,201,4063,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(558,3,201,4064,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(559,3,201,4071,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(560,3,201,4072,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(561,3,201,4073,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(562,3,201,5011,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(563,3,201,5012,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(564,3,201,5021,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(565,3,201,5022,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(566,3,201,5023,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(567,3,201,5024,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(568,3,201,5031,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(569,3,201,5032,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(570,3,201,6,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(571,3,201,207,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(572,3,201,208,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(573,3,201,209,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(574,3,201,210,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(575,3,201,211,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(576,3,201,212,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(577,3,201,213,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(578,3,201,214,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(579,3,201,601,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(580,3,201,602,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(581,3,201,2071,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(582,3,201,2072,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(583,3,201,2073,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(584,3,201,2074,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(585,3,201,2081,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(586,3,201,2082,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(587,3,201,2083,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(588,3,201,2084,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(589,3,201,2091,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(590,3,201,2092,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(591,3,201,2093,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(592,3,201,2094,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(593,3,201,2095,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(594,3,201,2101,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(595,3,201,2102,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(596,3,201,2111,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(597,3,201,2112,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(598,3,201,2131,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(599,3,201,2132,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(600,3,201,2133,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(601,3,201,215,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(602,3,201,2151,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(603,3,201,2152,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(604,3,201,2153,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(610,3,202,1,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(611,3,202,2,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(612,3,202,3,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(613,3,202,5,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(614,3,202,101,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(615,3,202,102,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(616,3,202,201,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(617,3,202,202,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(618,3,202,203,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(619,3,202,204,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(620,3,202,205,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(621,3,202,301,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(622,3,202,302,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(623,3,202,303,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(624,3,202,304,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(625,3,202,305,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(626,3,202,501,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(627,3,202,502,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(628,3,202,503,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(629,3,202,2011,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(630,3,202,2012,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(631,3,202,2013,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(632,3,202,2014,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(633,3,202,2015,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(634,3,202,2021,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(635,3,202,2022,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(636,3,202,2023,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(637,3,202,2024,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(638,3,202,2031,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(639,3,202,2032,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(640,3,202,2033,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(641,3,202,2034,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(642,3,202,2051,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(643,3,202,3011,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(644,3,202,5011,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(645,3,202,5021,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(646,3,202,5022,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(647,3,202,5023,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(648,3,202,5024,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(649,3,202,5031,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(650,3,202,5032,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(651,3,202,6,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(652,3,202,207,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(653,3,202,208,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(654,3,202,209,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(655,3,202,210,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(656,3,202,211,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(657,3,202,212,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(658,3,202,213,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(659,3,202,214,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(660,3,202,601,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(661,3,202,602,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(662,3,202,2071,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(663,3,202,2072,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(664,3,202,2073,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(665,3,202,2074,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(666,3,202,2081,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(667,3,202,2082,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(668,3,202,2083,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(669,3,202,2084,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(670,3,202,2091,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(671,3,202,2092,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(672,3,202,2093,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(673,3,202,2094,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(674,3,202,2095,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(675,3,202,2101,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(676,3,202,2102,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(677,3,202,2111,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(678,3,202,2112,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(679,3,202,2131,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(680,3,202,2132,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(681,3,202,2133,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(682,3,202,215,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(683,3,202,2151,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(684,3,202,2152,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(685,3,202,2153,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(737,3,203,1,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(738,3,203,2,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(739,3,203,3,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(740,3,203,5,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(741,3,203,101,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(742,3,203,102,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(743,3,203,204,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(744,3,203,302,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(745,3,203,303,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(746,3,203,304,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(747,3,203,305,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(748,3,203,501,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(749,3,203,5011,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(750,3,203,212,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL),(751,3,203,214,NULL,'system','2026-05-23 09:53:10',NULL,NULL,'2026-05-23 09:53:10',0,0,NULL,1,NULL);
/*!40000 ALTER TABLE `sys_role_menu` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `sys_sensitive_word` WRITE;
/*!40000 ALTER TABLE `sys_sensitive_word` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_sensitive_word` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `sys_social_user` WRITE;
/*!40000 ALTER TABLE `sys_social_user` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_social_user` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;
INSERT INTO `sys_user` VALUES (1,1,'admin','$2a$10$lwVOuywQZnaJqkuZdLeTn.GjhIWNhViECyxT.d/qdbAz0t1SLSJ7q','系统管理员','13800000000','admin@saas-cloud.com',NULL,1,1,1,0,NULL,NULL,'2026-05-19 17:42:52',NULL,'2026-05-19 11:13:18','1','系统管理员','2026-05-19 01:31:27','1','admin','2026-05-23 08:52:08',0,40,NULL,1,NULL),(2,1,'zhaomin','$2b$10$zqLFDqHvwVDxuURw6wKt5ujfEVcfFeUNFKiTkXRgVTpc8GPLcWAfG','赵敏','13800000002',NULL,NULL,2,1,1,2,NULL,NULL,NULL,NULL,NULL,NULL,'system','2026-05-23 09:52:05',NULL,NULL,'2026-05-23 10:06:47',0,0,NULL,1,NULL),(3,1,'sunli','$2b$10$zqLFDqHvwVDxuURw6wKt5ujfEVcfFeUNFKiTkXRgVTpc8GPLcWAfG','孙莉','13800000003',NULL,NULL,2,1,1,2,NULL,NULL,NULL,NULL,NULL,NULL,'system','2026-05-23 09:52:05',NULL,NULL,'2026-05-23 10:06:47',0,0,NULL,1,NULL),(101,2,'13900001001','$2b$10$zqLFDqHvwVDxuURw6wKt5ujfEVcfFeUNFKiTkXRgVTpc8GPLcWAfG','张星辰','13900001001',NULL,NULL,1,101,1,0,NULL,NULL,NULL,NULL,NULL,NULL,'system','2026-05-23 09:51:43',NULL,NULL,'2026-05-23 10:06:47',0,0,NULL,1,NULL),(102,2,'zhangsan','$2b$10$zqLFDqHvwVDxuURw6wKt5ujfEVcfFeUNFKiTkXRgVTpc8GPLcWAfG','张三','13900001002',NULL,NULL,1,102,1,2,NULL,NULL,NULL,NULL,NULL,NULL,'system','2026-05-23 09:51:43',NULL,NULL,'2026-05-23 10:06:47',0,0,NULL,1,NULL),(103,2,'lisi','$2b$10$zqLFDqHvwVDxuURw6wKt5ujfEVcfFeUNFKiTkXRgVTpc8GPLcWAfG','李四','13900001003',NULL,NULL,1,103,1,2,NULL,NULL,NULL,NULL,NULL,NULL,'system','2026-05-23 09:51:43',NULL,NULL,'2026-05-23 10:06:47',0,0,NULL,1,NULL),(104,2,'wangwu','$2b$10$zqLFDqHvwVDxuURw6wKt5ujfEVcfFeUNFKiTkXRgVTpc8GPLcWAfG','王五','13900001004',NULL,NULL,1,104,1,2,NULL,NULL,NULL,NULL,NULL,NULL,'system','2026-05-23 09:51:43',NULL,NULL,'2026-05-23 10:06:47',0,0,NULL,1,NULL),(201,3,'13900002001','$2b$10$zqLFDqHvwVDxuURw6wKt5ujfEVcfFeUNFKiTkXRgVTpc8GPLcWAfG','李蓝海','13900002001',NULL,NULL,1,201,1,0,NULL,NULL,NULL,NULL,NULL,NULL,'system','2026-05-23 09:51:54',NULL,NULL,'2026-05-23 10:06:47',0,0,NULL,1,NULL),(202,3,'liuyi','$2b$10$zqLFDqHvwVDxuURw6wKt5ujfEVcfFeUNFKiTkXRgVTpc8GPLcWAfG','刘一','13900002002',NULL,NULL,1,202,1,2,NULL,NULL,NULL,NULL,NULL,NULL,'system','2026-05-23 09:51:54',NULL,NULL,'2026-05-23 10:06:47',0,0,NULL,1,NULL),(203,3,'chenqi','$2b$10$zqLFDqHvwVDxuURw6wKt5ujfEVcfFeUNFKiTkXRgVTpc8GPLcWAfG','陈七','13900002003',NULL,NULL,1,203,1,2,NULL,NULL,NULL,NULL,NULL,NULL,'system','2026-05-23 09:51:54',NULL,NULL,'2026-05-23 10:06:47',0,0,NULL,1,NULL);
/*!40000 ALTER TABLE `sys_user` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `sys_user_post` WRITE;
/*!40000 ALTER TABLE `sys_user_post` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_user_post` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `sys_user_role` WRITE;
/*!40000 ALTER TABLE `sys_user_role` DISABLE KEYS */;
INSERT INTO `sys_user_role` VALUES (1,1,1,1,'1','系统管理员','2026-05-19 01:31:27',NULL,NULL,'2026-05-19 01:31:27',0,0,NULL,1,NULL),(2,2,101,101,NULL,'system','2026-05-23 09:52:36',NULL,NULL,'2026-05-23 09:52:36',0,0,NULL,1,NULL),(3,2,102,102,NULL,'system','2026-05-23 09:52:36',NULL,NULL,'2026-05-23 09:52:36',0,0,NULL,1,NULL),(4,2,103,103,NULL,'system','2026-05-23 09:52:36',NULL,NULL,'2026-05-23 09:52:36',0,0,NULL,1,NULL),(5,2,104,103,NULL,'system','2026-05-23 09:52:36',NULL,NULL,'2026-05-23 09:52:36',0,0,NULL,1,NULL),(6,3,201,201,NULL,'system','2026-05-23 09:52:36',NULL,NULL,'2026-05-23 09:52:36',0,0,NULL,1,NULL),(7,3,202,202,NULL,'system','2026-05-23 09:52:36',NULL,NULL,'2026-05-23 09:52:36',0,0,NULL,1,NULL),(8,3,203,203,NULL,'system','2026-05-23 09:52:36',NULL,NULL,'2026-05-23 09:52:36',0,0,NULL,1,NULL),(9,1,2,2,NULL,'system','2026-05-23 09:52:36',NULL,NULL,'2026-05-23 09:52:36',0,0,NULL,1,NULL),(10,1,3,3,NULL,'system','2026-05-23 09:52:36',NULL,NULL,'2026-05-23 09:52:36',0,0,NULL,1,NULL);
/*!40000 ALTER TABLE `sys_user_role` ENABLE KEYS */;
UNLOCK TABLES;

USE `wechat_oa`;

LOCK TABLES `wechat_oa_account` WRITE;
/*!40000 ALTER TABLE `wechat_oa_account` DISABLE KEYS */;
INSERT INTO `wechat_oa_account` VALUES (1,1,'TestOA','wxtest123','secret123',NULL,NULL,0,0,NULL,NULL,NULL,1,'1','admin','2026-05-19 11:13:18',NULL,NULL,'2026-05-19 03:13:16',0,0,NULL,1,NULL);
/*!40000 ALTER TABLE `wechat_oa_account` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `wechat_oa_article` WRITE;
/*!40000 ALTER TABLE `wechat_oa_article` DISABLE KEYS */;
/*!40000 ALTER TABLE `wechat_oa_article` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `wechat_oa_auto_reply_rule` WRITE;
/*!40000 ALTER TABLE `wechat_oa_auto_reply_rule` DISABLE KEYS */;
INSERT INTO `wechat_oa_auto_reply_rule` VALUES (1,1,1,'test',1,'hi',0,0,'hello',NULL,1,0,'1',NULL,'2026-05-19 12:49:03',NULL,NULL,'2026-05-19 04:49:01',0,0,NULL,1,NULL),(2,1,1,'gwtest',1,'hi',0,0,'hello',NULL,1,0,'1','admin','2026-05-19 12:49:24',NULL,NULL,'2026-05-19 04:49:23',0,0,NULL,1,NULL);
/*!40000 ALTER TABLE `wechat_oa_auto_reply_rule` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `wechat_oa_fan_user` WRITE;
/*!40000 ALTER TABLE `wechat_oa_fan_user` DISABLE KEYS */;
/*!40000 ALTER TABLE `wechat_oa_fan_user` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `wechat_oa_material` WRITE;
/*!40000 ALTER TABLE `wechat_oa_material` DISABLE KEYS */;
/*!40000 ALTER TABLE `wechat_oa_material` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `wechat_oa_menu` WRITE;
/*!40000 ALTER TABLE `wechat_oa_menu` DISABLE KEYS */;
/*!40000 ALTER TABLE `wechat_oa_menu` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `wechat_oa_user_tag` WRITE;
/*!40000 ALTER TABLE `wechat_oa_user_tag` DISABLE KEYS */;
INSERT INTO `wechat_oa_user_tag` VALUES (1,1,1,NULL,'test',0,'1',NULL,'2026-05-19 12:48:54',NULL,NULL,'2026-05-19 04:48:54',0,0,NULL,1,NULL),(2,1,1,NULL,'gwtest',0,'1','admin','2026-05-19 12:49:24',NULL,NULL,'2026-05-19 04:49:23',0,0,NULL,1,NULL);
/*!40000 ALTER TABLE `wechat_oa_user_tag` ENABLE KEYS */;
UNLOCK TABLES;

USE `notify`;

LOCK TABLES `notify_channel_config` WRITE;
/*!40000 ALTER TABLE `notify_channel_config` DISABLE KEYS */;
INSERT INTO `notify_channel_config` VALUES (1,1,0,0,'{}','1','admin','2026-05-19 13:31:35',NULL,NULL,'2026-05-19 05:31:34',0,0,NULL,1,NULL),(2,1,1,0,'{}','1','admin','2026-05-19 13:31:35',NULL,NULL,'2026-05-19 05:31:34',0,0,NULL,1,NULL),(3,1,2,0,'{}','1','admin','2026-05-19 13:31:35',NULL,NULL,'2026-05-19 05:31:34',0,0,NULL,1,NULL),(4,1,3,0,'{}','1','admin','2026-05-19 13:31:35',NULL,NULL,'2026-05-19 05:31:34',0,0,NULL,1,NULL),(5,1,4,0,'{}','1','admin','2026-05-19 13:31:35',NULL,NULL,'2026-05-19 05:31:34',0,0,NULL,1,NULL);
/*!40000 ALTER TABLE `notify_channel_config` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `notify_template` WRITE;
/*!40000 ALTER TABLE `notify_template` DISABLE KEYS */;
INSERT INTO `notify_template` VALUES (1,1,'TEST_001','test',0,'title','content',1,'1','admin','2026-05-19 13:24:24',NULL,NULL,'2026-05-19 05:24:22',0,0,NULL,1,NULL),(2,1,'TEST_003','test3',0,'title3','content3',1,'1','admin','2026-05-19 13:25:21',NULL,NULL,'2026-05-19 05:25:21',0,0,NULL,1,NULL),(3,1,'TEST_004','test4',0,'title4','content4',1,'1','admin','2026-05-19 13:26:53',NULL,NULL,'2026-05-19 05:26:52',0,0,NULL,1,NULL),(4,1,'ASCII_TEST','test-ascii',0,'title','content',1,'1','admin','2026-05-19 13:36:23',NULL,NULL,'2026-05-19 05:36:24',0,0,NULL,1,NULL),(5,1,'GW_TEST','gateway-test',0,'title','content',1,'1','admin','2026-05-19 13:37:18',NULL,NULL,'2026-05-19 05:37:16',0,0,NULL,1,NULL),(6,1,'FINAL_TEST','final-test',1,'hello','world',1,'1','admin','2026-05-19 13:41:31',NULL,NULL,'2026-05-19 05:41:29',0,0,NULL,1,NULL),(7,1,'VALID_TEST','valid',0,'t','c',1,'1','admin','2026-05-19 14:21:02',NULL,NULL,'2026-05-19 06:21:00',0,0,NULL,1,NULL);
/*!40000 ALTER TABLE `notify_template` ENABLE KEYS */;
UNLOCK TABLES;

USE `xxl_job`;

LOCK TABLES `xxl_job_group` WRITE;
/*!40000 ALTER TABLE `xxl_job_group` DISABLE KEYS */;
INSERT INTO `xxl_job_group` VALUES (1,'xxl-job-executor-sample','通用执行器Sample',0,NULL,'2026-09-29 16:25:57'),(2,'xxl-job-executor-sample-ai','AI执行器Sample',0,NULL,'2026-09-29 16:25:57');
/*!40000 ALTER TABLE `xxl_job_group` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `xxl_job_info` WRITE;
/*!40000 ALTER TABLE `xxl_job_info` DISABLE KEYS */;
INSERT INTO `xxl_job_info` VALUES (1,1,'示例任务01','2026-09-29 14:59:47','2026-09-29 14:59:47','XXL','','CRON','0 0 0 * * ? *','DO_NOTHING','FIRST','demoJobHandler','','SERIAL_EXECUTION',0,0,'BEAN','','GLUE代码初始化','2026-09-29 14:59:47','',0,0,0),(2,2,'Ollama示例任务01','2026-09-29 14:59:47','2026-09-29 14:59:47','XXL','','NONE','','DO_NOTHING','FIRST','ollamaJobHandler','{\n    \"input\": \"慢SQL问题分析思路\",\n    \"prompt\": \"你是一个研发工程师，擅长解决技术类问题。\"\n}','SERIAL_EXECUTION',0,0,'BEAN','','GLUE代码初始化','2026-09-29 14:59:47','',0,0,0),(3,2,'Dify示例任务','2026-09-29 14:59:47','2026-09-29 14:59:47','XXL','','NONE','','DO_NOTHING','FIRST','difyWorkflowJobHandler','{\n    \"inputs\":{\n        \"input\":\"查询班级各学科前三名\"\n    },\n    \"user\": \"xxl-job\",\n    \"baseUrl\": \"http://localhost/v1\",\n    \"apiKey\": \"app-OUVgNUOQRIMokfmuJvBJoUTN\"\n}','SERIAL_EXECUTION',0,0,'BEAN','','GLUE代码初始化','2026-09-29 14:59:47','',0,0,0);
/*!40000 ALTER TABLE `xxl_job_info` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `xxl_job_lock` WRITE;
/*!40000 ALTER TABLE `xxl_job_lock` DISABLE KEYS */;
INSERT INTO `xxl_job_lock` VALUES ('schedule_lock');
/*!40000 ALTER TABLE `xxl_job_lock` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `xxl_job_user` WRITE;
/*!40000 ALTER TABLE `xxl_job_user` DISABLE KEYS */;
INSERT INTO `xxl_job_user` VALUES (1,'admin','e10adc3949ba59abbe56e057f20f883e',1,NULL),(2,'libin','e10adc3949ba59abbe56e057f20f883e',1,'');
/*!40000 ALTER TABLE `xxl_job_user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

