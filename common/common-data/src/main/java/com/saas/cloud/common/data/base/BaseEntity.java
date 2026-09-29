package com.saas.cloud.common.data.base;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.Version;

import lombok.Data;

/**
 * 实体基类，包含公共审计字段
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private String createUserId;

    @TableField(fill = FieldFill.INSERT)
    private String createUserName;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.UPDATE)
    private String updateUserId;

    @TableField(fill = FieldFill.UPDATE)
    private String updateUserName;

    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleteFlag;

    @Version
    private Integer dataVersion;

    private String remark;

    /**
     * 有效状态：1-有效 0-禁用
     */
    @TableField(fill = FieldFill.INSERT)
    private Integer validStatus;

    /**
     * 链路追踪ID，插入时从 MDC 注入
     */
    @TableField(fill = FieldFill.INSERT)
    private String traceId;
}
