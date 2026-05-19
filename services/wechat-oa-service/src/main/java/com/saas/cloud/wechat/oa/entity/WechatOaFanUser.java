package com.saas.cloud.wechat.oa.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.saas.cloud.common.data.base.TenantBaseEntity;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 粉丝表
 * </p>
 *
 * @author saas-cloud
 * @since 2026-05-18
 */
@Getter
@Setter
@TableName("wechat_oa_fan_user")
public class WechatOaFanUser extends TenantBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 公众号ID
     */
    @TableField("account_id")
    private Long accountId;

    /**
     * 微信OpenID
     */
    @TableField("openid")
    private String openid;

    /**
     * 微信UnionID
     */
    @TableField("unionid")
    private String unionid;

    /**
     * 昵称
     */
    @TableField("nickname")
    private String nickname;

    /**
     * 头像URL
     */
    @TableField("avatar_url")
    private String avatarUrl;

    /**
     * 性别 0-未知 1-男 2-女
     */
    @TableField("gender")
    private Byte gender;

    /**
     * 国家
     */
    @TableField("country")
    private String country;

    /**
     * 省份
     */
    @TableField("province")
    private String province;

    /**
     * 城市
     */
    @TableField("city")
    private String city;

    /**
     * 语言
     */
    @TableField("language")
    private String language;

    /**
     * 关注状态 0-已取关 1-已关注
     */
    @TableField("subscribe_status")
    private Byte subscribeStatus;

    /**
     * 关注时间
     */
    @TableField("subscribe_time")
    private LocalDateTime subscribeTime;

    /**
     * 取关时间
     */
    @TableField("unsubscribe_time")
    private LocalDateTime unsubscribeTime;

    /**
     * 关注渠道
     */
    @TableField("subscribe_scene")
    private String subscribeScene;

    /**
     * 是否拉黑 0-否 1-是
     */
    @TableField("is_blacklisted")
    private Byte isBlacklisted;

    /**
     * 标签ID列表(JSON)
     */
    @TableField("tag_ids")
    private String tagIds;
}
