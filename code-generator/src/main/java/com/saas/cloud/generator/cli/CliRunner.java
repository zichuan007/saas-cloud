package com.saas.cloud.generator.cli;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import com.saas.cloud.generator.engine.GeneratorConfig;
import com.saas.cloud.generator.engine.GeneratorEngine;
import com.saas.cloud.generator.engine.TableMeta;

/**
 * 代码生成器命令行入口
 * 用法:
 * java -cp code-generator.jar com.saas.cloud.generator.cli.CliRunner \
 *   --jdbc-url=jdbc:mysql://localhost:3306/mydb?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai \
 *   --username=root \
 *   --password=root \
 *   --package=com.example.demo \
 *   --author=developer \
 *   --tables=user,order,product \
 *   --table-prefix=t_,sys_ \
 *   --output=./generated
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
public class CliRunner {

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }

        GeneratorConfig config = parseArgs(args);
        if (config.getJdbcUrl() == null || config.getPackageName() == null) {
            System.err.println("[错误] --jdbc-url 和 --package 为必填参数");
            printUsage();
            System.exit(1);
        }

        System.out.println("========== 代码生成器 CLI 模式 ==========");
        System.out.println("  数据库: " + config.getJdbcUrl());
        System.out.println("  包名:   " + config.getPackageName());
        System.out.println("  输出:   " + config.getOutputDir());

        GeneratorEngine engine = new GeneratorEngine();
        List<TableMeta> tables = engine.scanTables(config);
        if (tables.isEmpty()) {
            System.out.println("\n[跳过] 没有发现需要生成的表");
            return;
        }

        System.out.println("  发现 " + tables.size() + " 张表\n");
        engine.generate(config, tables, Paths.get(config.getOutputDir()));
    }

    private static GeneratorConfig parseArgs(String[] args) {
        GeneratorConfig config = new GeneratorConfig();
        for (String arg : args) {
            if (arg.startsWith("--jdbc-url=")) {
                config.setJdbcUrl(arg.substring("--jdbc-url=".length()));
            } else if (arg.startsWith("--username=")) {
                config.setUsername(arg.substring("--username=".length()));
            } else if (arg.startsWith("--password=")) {
                config.setPassword(arg.substring("--password=".length()));
            } else if (arg.startsWith("--package=")) {
                config.setPackageName(arg.substring("--package=".length()));
            } else if (arg.startsWith("--author=")) {
                config.setAuthor(arg.substring("--author=".length()));
            } else if (arg.startsWith("--tables=")) {
                config.setIncludeTables(Arrays.asList(arg.substring("--tables=".length()).split(",")));
            } else if (arg.startsWith("--table-prefix=")) {
                config.setTablePrefix(Arrays.asList(arg.substring("--table-prefix=".length()).split(",")));
            } else if (arg.startsWith("--output=")) {
                config.setOutputDir(arg.substring("--output=".length()));
            }
        }
        return config;
    }

    private static void printUsage() {
        System.out.println("用法:");
        System.out.println("  java -cp code-generator.jar com.saas.cloud.generator.cli.CliRunner [参数]");
        System.out.println();
        System.out.println("必填参数:");
        System.out.println("  --jdbc-url=<url>       JDBC 连接地址");
        System.out.println("  --username=<user>      数据库用户名");
        System.out.println("  --password=<pass>      数据库密码");
        System.out.println("  --package=<pkg>        生成代码的根包名 (如 com.example.demo)");
        System.out.println();
        System.out.println("可选参数:");
        System.out.println("  --author=<name>        作者名 (默认: generator)");
        System.out.println("  --tables=<t1,t2>       指定表名，逗号分隔 (默认: 全库)");
        System.out.println("  --table-prefix=<p1,p2> 表前缀，生成类名时去除 (如 t_,sys_)");
        System.out.println("  --output=<dir>         输出目录 (默认: ./generated)");
    }
}
