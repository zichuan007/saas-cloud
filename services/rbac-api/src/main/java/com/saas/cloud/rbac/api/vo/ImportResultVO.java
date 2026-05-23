package com.saas.cloud.rbac.api.vo;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * 通用导入结果 VO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Data
public class ImportResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 总行数 */
    private int total;

    /** 成功数 */
    private int success;

    /** 失败数 */
    private int fail;

    /** 错误明细（第N行: 原因） */
    private List<String> errorDetails;

    public static ImportResultVO of(int total, int success, int fail, List<String> errorDetails) {
        ImportResultVO vo = new ImportResultVO();
        vo.setTotal(total);
        vo.setSuccess(success);
        vo.setFail(fail);
        vo.setErrorDetails(errorDetails);
        return vo;
    }
}
