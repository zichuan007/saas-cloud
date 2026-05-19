package com.saas.cloud.workflow.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 流程实例分页查询请求DTO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
public class ProcessQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 当前页码 */
    private int pageNum = 1;

    /** 每页条数 */
    private int pageSize = 10;

    /** 流程名称（模糊查询） */
    private String processName;

    /** 状态 0-进行中 1-已完成 2-已撤回 3-已终止 */
    private Byte status;
}
