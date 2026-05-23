package com.saas.cloud.rbac.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 岗位 视图对象
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Data
public class PostVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 岗位ID */
    private Long id;

    /** 岗位编码 */
    private String postCode;

    /** 岗位名称 */
    private String postName;

    /** 排序 */
    private Integer sortOrder;

    /** 状态 0-禁用 1-启用 */
    private Integer status;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createTime;
}
