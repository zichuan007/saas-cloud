package com.saas.cloud.generator.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 数据库连接请求
 * <p>前端只传连接参数，后端根据 dbType 拼 JDBC URL，避免前端传 & 被 XSS 转义</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
public class ConnectRequest {

    /** 数据库类型：MYSQL/POSTGRESQL/ORACLE */
    @NotNull(message = "数据库类型不能为空")
    private String dbType;

    /** 主机地址 */
    @NotBlank(message = "主机地址不能为空")
    private String host;

    /** 端口 */
    @NotBlank(message = "端口不能为空")
    private String port;

    /** 数据库名 */
    @NotBlank(message = "数据库名不能为空")
    private String dbName;

    /** 数据库用户名 */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 数据库密码 */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 构建完整 JDBC URL
     *
     * @return JDBC URL
     */
    public String buildJdbcUrl() {
        String type = dbType != null ? dbType.toUpperCase() : "MYSQL";
        switch (type) {
            case "MYSQL":
                return "jdbc:mysql://" + host + ":" + port + "/" + dbName
                        + "?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai";
            case "POSTGRESQL":
                return "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
            case "ORACLE":
                return "jdbc:oracle:thin:@" + host + ":" + port + ":" + dbName;
            default:
                throw new IllegalArgumentException("不支持的数据库类型: " + dbType);
        }
    }
}
