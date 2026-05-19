package com.saas.cloud.wechat.oa.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.saas.cloud.common.data.base.TenantBaseEntity;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * ç²‰ä¸æ ‡ç­¾è¡¨
 * </p>
 *
 * @author saas-cloud
 * @since 2026-05-18
 */
@Getter
@Setter
@TableName("wechat_oa_user_tag")
public class WechatOaUserTag extends TenantBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * å…¬ä¼—å·ID
     */
    @TableField("account_id")
    private Long accountId;

    /**
     * å¾®ä¿¡ç«¯æ ‡ç­¾ID
     */
    @TableField("wx_tag_id")
    private Integer wxTagId;

    /**
     * æ ‡ç­¾åç§°
     */
    @TableField("tag_name")
    private String tagName;

    /**
     * ç²‰ä¸æ•°
     */
    @TableField("fan_count")
    private Integer fanCount;
}
