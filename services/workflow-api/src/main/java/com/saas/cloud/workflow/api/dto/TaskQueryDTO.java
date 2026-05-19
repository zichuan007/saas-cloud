package com.saas.cloud.workflow.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 任务分页查询请求DTO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
public class TaskQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 当前页码 */
    private int pageNum = 1;

    /** 每页条数 */
    private int pageSize = 10;

    /** 流程名称（模糊查询） */
    private String processName;
}
