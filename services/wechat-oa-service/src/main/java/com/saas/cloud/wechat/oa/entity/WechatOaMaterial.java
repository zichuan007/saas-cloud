package com.saas.cloud.wechat.oa.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.saas.cloud.common.data.base.TenantBaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 素材表
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
     * 公众号ID
     */
    @TableField("account_id")
    private Long accountId;

    /**
     * 微信素材MediaID
     */
    @TableField("media_id")
    private String mediaId;

    /**
     * 类型 0-图片 1-语音 2-视频 3-缩略图
     */
    @TableField("material_type")
    private Byte materialType;

    /**
     * 素材标题
     */
    @TableField("title")
    private String title;

    /**
     * 原始文件名
     */
    @TableField("file_name")
    private String fileName;

    /**
     * 本地存储URL(MinIO)
     */
    @TableField("file_url")
    private String fileUrl;

    /**
     * 文件大小(字节)
     */
    @TableField("file_size")
    private Long fileSize;

    /**
     * 微信端URL
     */
    @TableField("wechat_url")
    private String wechatUrl;
}
