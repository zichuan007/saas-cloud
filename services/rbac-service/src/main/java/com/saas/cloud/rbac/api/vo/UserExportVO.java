package com.saas.cloud.rbac.api.vo;

import java.time.LocalDateTime;

import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 用户导出 VO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-22
 */
@Data
public class UserExportVO {

    @ExcelProperty("用户名")
    @ColumnWidth(15)
    private String username;

    @ExcelProperty("姓名")
    @ColumnWidth(15)
    private String realName;

    @ExcelProperty("手机号")
    @ColumnWidth(15)
    private String phone;

    @ExcelProperty("邮箱")
    @ColumnWidth(25)
    private String email;

    @ExcelProperty("状态")
    @ColumnWidth(10)
    private String statusDesc;

    @ExcelProperty("创建时间")
    @ColumnWidth(20)
    private LocalDateTime createTime;

}
