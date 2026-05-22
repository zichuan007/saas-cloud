package com.saas.cloud.platform.api.dto;

import java.io.Serializable;

import lombok.Data;

/**
 * 系统公告查询请求DTO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
public class AnnouncementQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 当前页码 */
    private int pageNum = 1;

    /** 每页条数 */
    private int pageSize = 10;

    /** 公告标题（模糊查询） */
    private String title;

    /** 状态 0-草稿 1-已发布 2-已下线 */
    private Byte status;

    /** 类型 0-通知 1-维护 2-功能更新 */
    private Byte type;
}
