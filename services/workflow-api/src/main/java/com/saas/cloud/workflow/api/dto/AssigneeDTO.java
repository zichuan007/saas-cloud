package com.saas.cloud.workflow.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 审批人 DTO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssigneeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private String userId;

    /** 用户姓名 */
    private String userName;
}