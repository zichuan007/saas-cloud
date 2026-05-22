package com.saas.cloud.platform.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.saas.cloud.common.data.base.BaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 全局配置表
 *
 * @author saas-cloud
 * @since 2026-05-18
 */
@Getter
@Setter
@TableName("sys_global_config")
public class GlobalConfig extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 配置键
     */
    @TableField("config_key")
    private String configKey;

    /**
     * 配置值
     */
    @TableField("config_value")
    private String configValue;

    /**
     * 值类型
     */
    @TableField("config_type")
    private String configType;

    /**
     * 配置说明
     */
    @TableField("description")
    private String description;
}
