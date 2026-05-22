package com.saas.cloud.platform.api.vo;

import java.time.LocalDateTime;

import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 租户导出 VO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-22
 */
@Data
public class TenantExportVO {

    @ExcelProperty("租户编码")
    @ColumnWidth(18)
    private String tenantCode;

    @ExcelProperty("租户名称")
    @ColumnWidth(25)
    private String tenantName;

    @ExcelProperty("联系人")
    @ColumnWidth(15)
    private String contactPerson;

    @ExcelProperty("联系电话")
    @ColumnWidth(15)
    private String contactPhone;

    @ExcelProperty("联系邮箱")
    @ColumnWidth(25)
    private String contactEmail;

    @ExcelProperty("状态")
    @ColumnWidth(10)
    private String statusDesc;

    @ExcelProperty("创建时间")
    @ColumnWidth(20)
    private LocalDateTime createTime;

}
