package com.saas.cloud.rbac.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Getter;
import lombok.Setter;

/**
 * 行政区划表（全局平台级数据，不走租户隔离）
 *
 * @author saas-cloud
 * @version V3.0
 * @since 2026-05-23
 */
@Getter
@Setter
@TableName("sys_area")
public class Area implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 父级区划代码（0=顶级） */
    @TableField("parent_code")
    private String parentCode;

    /** 国标行政区划代码（6位） */
    @TableField("area_code")
    private String areaCode;

    /** 区域名称 */
    @TableField("area_name")
    private String areaName;

    /** 简称 */
    @TableField("short_name")
    private String shortName;

    /** 组合名称（如：北京,东城） */
    @TableField("merger_name")
    private String mergerName;

    /** 拼音 */
    @TableField("pinyin")
    private String pinyin;

    /** 拼音首字母 */
    @TableField("first_letter")
    private String firstLetter;

    /** 层级 1-省 2-市 3-区/县 */
    @TableField("area_level")
    private Byte areaLevel;

    /** 邮政编码 */
    @TableField("zip_code")
    private String zipCode;

    /** 电话区号 */
    @TableField("city_code")
    private String cityCode;

    /** 经度 */
    @TableField("lng")
    private BigDecimal lng;

    /** 纬度 */
    @TableField("lat")
    private BigDecimal lat;

    /** 排序 */
    @TableField("sort_order")
    private Integer sortOrder;
}
