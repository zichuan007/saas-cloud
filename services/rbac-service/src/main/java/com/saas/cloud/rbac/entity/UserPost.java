package com.saas.cloud.rbac.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.saas.cloud.common.data.base.TenantBaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 用户岗位关联表
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Getter
@Setter
@TableName("sys_user_post")
public class UserPost extends TenantBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 岗位ID */
    @TableField("post_id")
    private Long postId;
}
