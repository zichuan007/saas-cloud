package com.saas.cloud.rbac.api.vo;

import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色导出 VO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-22
 */
@Data
public class RoleExportVO {

    @ExcelProperty("角色名称")
    @ColumnWidth(20)
    private String roleName;

    @ExcelProperty("角色编码")
    @ColumnWidth(20)
    private String roleCode;

    @ExcelProperty("角色等级")
    @ColumnWidth(12)
    private String roleLevelDesc;

    @ExcelProperty("数据范围")
    @ColumnWidth(15)
    private String dataScopeDesc;

    @ExcelProperty("状态")
    @ColumnWidth(10)
    private String statusDesc;

    @ExcelProperty("创建时间")
    @ColumnWidth(20)
    private LocalDateTime createTime;
}
