package com.saas.cloud.generator.engine;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * 通用代码生成器配置
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
public class GeneratorConfig {

    /** JDBC 连接地址 */
    private String jdbcUrl;

    /** 数据库用户名 */
    private String username;

    /** 数据库密码 */
    private String password;

    /** 生成代码的根包名，如 com.example.demo */
    private String packageName;

    /** 作者 */
    private String author = "generator";

    /** 表前缀（生成类名时去除），如 [t_, sys_] */
    private List<String> tablePrefix = new ArrayList<>();

    /** 指定生成的表名（为空则扫描全库） */
    private List<String> includeTables = new ArrayList<>();

    /** 排除的表名 */
    private List<String> excludeTables = new ArrayList<>();

    /** CLI 模式输出目录 */
    private String outputDir = "./generated";
}
