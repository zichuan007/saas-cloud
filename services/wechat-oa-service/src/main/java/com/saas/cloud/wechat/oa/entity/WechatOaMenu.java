package com.saas.cloud.wechat.oa.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.saas.cloud.common.data.base.TenantBaseEntity;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * å…¬ä¼—å·èœå•è¡¨
 * </p>
 *
 * @author saas-cloud
 * @since 2026-05-18
 */
@Getter
@Setter
@TableName("wechat_oa_menu")
public class WechatOaMenu extends TenantBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * å…¬ä¼—å·ID
     */
    @TableField("account_id")
    private Long accountId;

    /**
     * èœå•åç§°
     */
    @TableField("menu_name")
    private String menuName;

    /**
     * çˆ¶èœå•ID 0-ä¸€çº§
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * èœå•ç±»åž‹ click/view/miniprogramç­‰
     */
    @TableField("menu_type")
    private String menuType;

    /**
     * èœå•KEY(clickç±»åž‹)
     */
    @TableField("menu_key")
    private String menuKey;

    /**
     * èœå•URL(viewç±»åž‹)
     */
    @TableField("menu_url")
    private String menuUrl;

    /**
     * ç´ æID
     */
    @TableField("media_id")
    private String mediaId;

    /**
     * æŽ’åº
     */
    @TableField("sort_order")
    private Integer sortOrder;
}
