package com.saas.cloud.generator.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 数据库连接请求
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
public class ConnectRequest {

    /** JDBC 连接地址 */
    @NotBlank(message = "JDBC 连接地址不能为空")
    private String jdbcUrl;

    /** 数据库用户名 */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 数据库密码 */
    @NotBlank(message = "密码不能为空")
    private String password;
}
