package com.saas.cloud.rbac.api.vo;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

/**
 * 行政区划 VO
 *
 * @author saas-cloud
 * @version V3.0
 * @since 2026-05-23
 */
@Data
public class AreaVO {

    /** 主键 */
    private Long id;

    /** 国标行政区划代码（6位） */
    private String areaCode;

    /** 父级区划代码 */
    private String parentCode;

    /** 区域名称 */
    private String areaName;

    /** 简称 */
    private String shortName;

    /** 组合名称（如：北京,东城） */
    private String mergerName;

    /** 拼音 */
    private String pinyin;

    /** 拼音首字母 */
    private String firstLetter;

    /** 层级 1-省 2-市 3-区/县 */
    private Byte areaLevel;

    /** 邮政编码 */
    private String zipCode;

    /** 电话区号 */
    private String cityCode;

    /** 经度 */
    private BigDecimal lng;

    /** 纬度 */
    private BigDecimal lat;

    /** 子级列表 */
    private List<AreaVO> children;
}
