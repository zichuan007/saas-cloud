package com.saas.cloud.rbac.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录日志 分页查询条件
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Data
public class LoginLogQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 页码 */
    private int pageNum = 1;

    /** 每页条数 */
    private int pageSize = 10;

    /** 登录用户名 */
    private String username;

    /** 登录状态 0-失败 1-成功 */
    private Integer status;

    /** 登录IP */
    private String ip;

    /** 开始时间 */
    private LocalDateTime beginTime;

    /** 结束时间 */
    private LocalDateTime endTime;
}
