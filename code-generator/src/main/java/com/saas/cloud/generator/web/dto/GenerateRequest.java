package com.saas.cloud.generator.web.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

/**
 * 代码生成请求
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
public class GenerateRequest {

    /** JDBC 连接地址 */
    @NotBlank(message = "JDBC 连接地址不能为空")
    private String jdbcUrl;

    /** 数据库用户名 */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 数据库密码 */
    @NotBlank(message = "密码不能为空")
    private String password;

    /** 生成代码的根包名 */
    @NotBlank(message = "包名不能为空")
    private String packageName;

    /** 作者 */
    private String author = "generator";

    /** 表前缀 */
    private List<String> tablePrefix = new ArrayList<>();

    /** 要生成的表名列表（空=全部） */
    private List<String> tables = new ArrayList<>();

    /** 预览模式下的单表名 */
    private String previewTable;
}
