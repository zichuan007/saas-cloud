package com.saas.cloud.workflow.api.dto;

import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 节点候选人 DTO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-08-14
 */
@Data
public class NodeCandidateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 分配策略: USER/ROLE/DEPT/GROUP/EXPRESSION/API/INITIATOR */
    @NotBlank(message = "分配策略不能为空")
    private String assignType;

    /** 策略值 */
    @NotBlank(message = "策略值不能为空")
    private String assignValue;

    /** 排序 */
    private Integer sortOrder;
}