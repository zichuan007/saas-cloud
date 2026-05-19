package com.saas.cloud.platform.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.saas.cloud.common.data.base.BaseEntity;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * å…¨å±€é…ç½®è¡¨
 * </p>
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
     * é…ç½®é”®
     */
    @TableField("config_key")
    private String configKey;

    /**
     * é…ç½®å€¼
     */
    @TableField("config_value")
    private String configValue;

    /**
     * å€¼ç±»åž‹
     */
    @TableField("config_type")
    private String configType;

    /**
     * é…ç½®è¯´æ˜Ž
     */
    @TableField("description")
    private String description;
}
