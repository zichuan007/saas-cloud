package com.saas.cloud.rbac.api.vo;

import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 用户导入 VO
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Data
public class UserImportVO {

    @ExcelProperty("用户名(必填)")
    @ColumnWidth(18)
    private String username;

    @ExcelProperty("密码(必填)")
    @ColumnWidth(18)
    private String password;

    @ExcelProperty("姓名")
    @ColumnWidth(15)
    private String realName;

    @ExcelProperty("手机号")
    @ColumnWidth(15)
    private String phone;

    @ExcelProperty("邮箱")
    @ColumnWidth(25)
    private String email;

    @ExcelProperty("部门ID")
    @ColumnWidth(12)
    private Long deptId;
}
