package com.saas.cloud.wechat.oa.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.saas.cloud.common.data.base.TenantBaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * ç´ æè¡¨
 * </p>
 *
 * @author saas-cloud
 * @since 2026-05-18
 */
@Getter
@Setter
@TableName("wechat_oa_material")
public class WechatOaMaterial extends TenantBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * å…¬ä¼—å·ID
     */
    @TableField("account_id")
    private Long accountId;

    /**
     * å¾®ä¿¡ç´ æMediaID
     */
    @TableField("media_id")
    private String mediaId;

    /**
     * ç±»åž‹ 0-å›¾ç‰‡ 1-è¯­éŸ³ 2-è§†é¢‘ 3-ç¼©ç•¥å›¾
     */
    @TableField("material_type")
    private Byte materialType;

    /**
     * ç´ ææ ‡é¢˜
     */
    @TableField("title")
    private String title;

    /**
     * åŽŸå§‹æ–‡ä»¶å
     */
    @TableField("file_name")
    private String fileName;

    /**
     * æœ¬åœ°å­˜å‚¨URL(MinIO)
     */
    @TableField("file_url")
    private String fileUrl;

    /**
     * æ–‡ä»¶å¤§å°(å­—èŠ‚)
     */
    @TableField("file_size")
    private Long fileSize;

    /**
     * å¾®ä¿¡ç«¯URL
     */
    @TableField("wechat_url")
    private String wechatUrl;
}
