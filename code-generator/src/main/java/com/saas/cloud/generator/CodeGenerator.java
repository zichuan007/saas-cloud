package com.saas.cloud.generator;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.TemplateType;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;
import com.saas.cloud.common.data.base.BaseEntity;
import com.saas.cloud.common.data.base.TenantBaseEntity;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * MyBatis Plus 多库批量代码生成器
 * <p>
 * 自动扫描数据库所有表，一键生成完整 CRUD：
 * Entity/Mapper/XML/Service/Controller → -service 模块
 * CreateDTO/UpdateDTO/QueryDTO/VO → -api 模块
 * MapStruct Converter → -service 模块
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
public class CodeGenerator {

    private static final String AUTHOR = "saas-cloud";
    private static final String PARENT_PACKAGE = "com.saas.cloud";
    private static final String[] TABLE_PREFIX = {"sys_", "t_", "tb_"};

    private static final String[] BASE_ENTITY_COLUMNS = {
            "id", "create_user_id", "create_user_name", "create_time",
            "update_user_id", "update_user_name", "update_time",
            "delete_flag", "data_version", "remark"
    };

    private static final String[] TENANT_ENTITY_COLUMNS = {
            "id", "tenant_id", "create_user_id", "create_user_name", "create_time",
            "update_user_id", "update_user_name", "update_time",
            "delete_flag", "data_version", "remark"
    };

    private static final Set<String> SUPER_COLUMN_SET = new HashSet<>(Arrays.asList(TENANT_ENTITY_COLUMNS));

    private static final Set<String> EXCLUDE_TABLES = new HashSet<>(Arrays.asList(
            "flyway_schema_history", "DATABASECHANGELOG", "DATABASECHANGELOGLOCK",
            "qrtz_blob_triggers", "qrtz_calendars", "qrtz_cron_triggers",
            "qrtz_fired_triggers", "qrtz_job_details", "qrtz_locks",
            "qrtz_paused_trigger_grps", "qrtz_scheduler_state",
            "qrtz_simple_triggers", "qrtz_simprop_triggers", "qrtz_triggers"
    ));

    private static final String[] EXCLUDE_TABLE_PREFIX = {"act_", "flw_"};

    /** SQL 类型 -> Java 类型映射 */
    private static final Map<String, String> TYPE_MAP = new HashMap<>();

    static {
        TYPE_MAP.put("bigint", "Long");
        TYPE_MAP.put("int", "Integer");
        TYPE_MAP.put("integer", "Integer");
        TYPE_MAP.put("tinyint", "Integer");
        TYPE_MAP.put("smallint", "Integer");
        TYPE_MAP.put("mediumint", "Integer");
        TYPE_MAP.put("varchar", "String");
        TYPE_MAP.put("char", "String");
        TYPE_MAP.put("text", "String");
        TYPE_MAP.put("longtext", "String");
        TYPE_MAP.put("mediumtext", "String");
        TYPE_MAP.put("tinytext", "String");
        TYPE_MAP.put("decimal", "java.math.BigDecimal");
        TYPE_MAP.put("numeric", "java.math.BigDecimal");
        TYPE_MAP.put("double", "Double");
        TYPE_MAP.put("float", "Float");
        TYPE_MAP.put("datetime", "java.time.LocalDateTime");
        TYPE_MAP.put("timestamp", "java.time.LocalDateTime");
        TYPE_MAP.put("date", "java.time.LocalDate");
        TYPE_MAP.put("time", "java.time.LocalTime");
        TYPE_MAP.put("bit", "Boolean");
        TYPE_MAP.put("boolean", "Boolean");
        TYPE_MAP.put("blob", "byte[]");
    }

    /** 适合作为查询条件的 Java 类型 */
    private static final Set<String> QUERY_TYPES = new HashSet<>(Arrays.asList(
            "String", "Integer", "Long"
    ));

    private static final String PROJECT_ROOT;

    static {
        String dir = System.getProperty("user.dir");
        PROJECT_ROOT = dir.contains("code-generator")
                ? dir.substring(0, dir.lastIndexOf("code-generator"))
                : dir + "/";
    }

    // ======================== 数据库配置 ========================

    private static List<DatabaseConfig> buildDatabaseConfigs() {
        List<DatabaseConfig> configs = new ArrayList<>();

        configs.add(new DatabaseConfig(
                "localhost", 3306, "platform", "root", "root",
                "platform-service", "platform-api", "platform", false
        ));

        configs.add(new DatabaseConfig(
                "localhost", 3306, "rbac", "root", "root",
                "rbac-service", "rbac-api", "rbac", true
        ));

        configs.add(new DatabaseConfig(
                "localhost", 3306, "workflow", "root", "root",
                "workflow-service", "workflow-api", "workflow", true
        ));

        configs.add(new DatabaseConfig(
                "localhost", 3306, "wechat_oa", "root", "root",
                "wechat-oa-service", "wechat-oa-api", "wechat.oa", true
        ));

        configs.add(new DatabaseConfig(
                "localhost", 3306, "notify", "root", "root",
                "notify-service", "notify-api", "notify", true
        ));

        return configs;
    }

    // ======================== 主程序 ========================

    public static void main(String[] args) {
        List<DatabaseConfig> configs = buildDatabaseConfigs();
        int totalTables = 0;

        VelocityEngine velocityEngine = initVelocity();

        for (DatabaseConfig config : configs) {
            System.out.println("\n========== 开始处理: " + config.database + " -> " + config.serviceModule + " ==========");

            List<String> tables = scanTables(config);
            if (tables.isEmpty()) {
                System.out.println("  [跳过] 没有发现需要生成的表");
                continue;
            }

            System.out.println("  发现 " + tables.size() + " 张表: " + tables);

            // 1. MyBatis Plus 生成 Entity/Mapper/XML -> -service
            generateMybatisPlus(config, tables);

            // 2. 自定义生成 Controller/Service/ServiceImpl/DTO/VO/Converter
            for (String tableName : tables) {
                List<FieldInfo> fields = readTableFields(config, tableName);
                String entityName = tableToEntityName(tableName);
                generateCustomFiles(velocityEngine, config, entityName, tableName, fields);
            }

            totalTables += tables.size();
        }

        System.out.println("\n========== 全部完成！共处理 " + configs.size() + " 个库，" + totalTables + " 张表 ==========");
    }

    // ======================== MyBatis Plus 标准生成 ========================

    private static void generateMybatisPlus(DatabaseConfig config, List<String> tables) {
        String javaOutput = PROJECT_ROOT + "services/" + config.serviceModule + "/src/main/java";
        String xmlOutput = PROJECT_ROOT + "services/" + config.serviceModule + "/src/main/resources/mapper";

        Class<?> superClass = config.hasTenantId ? TenantBaseEntity.class : BaseEntity.class;
        String[] superColumns = config.hasTenantId ? TENANT_ENTITY_COLUMNS : BASE_ENTITY_COLUMNS;

        FastAutoGenerator.create(config.getJdbcUrl(), config.username, config.password)
                .globalConfig(builder -> builder
                        .author(AUTHOR)
                        .outputDir(javaOutput)
                        .dateType(DateType.TIME_PACK)
                        .disableOpenDir()
                        .commentDate("yyyy-MM-dd")
                )
                .packageConfig(builder -> builder
                        .parent(PARENT_PACKAGE)
                        .moduleName(config.modulePackage)
                        .entity("entity")
                        .mapper("mapper")
                        .pathInfo(Collections.singletonMap(OutputFile.xml, xmlOutput))
                )
                .strategyConfig(builder -> builder
                        .addInclude(tables.toArray(new String[0]))
                        .addTablePrefix(TABLE_PREFIX)

                        .entityBuilder()
                        .superClass(superClass)
                        .enableLombok()
                        .enableTableFieldAnnotation()
                        .naming(NamingStrategy.underline_to_camel)
                        .columnNaming(NamingStrategy.underline_to_camel)
                        .logicDeleteColumnName("delete_flag")
                        .versionColumnName("data_version")
                        .addSuperEntityColumns(superColumns)

                        .mapperBuilder()
                        .enableMapperAnnotation()
                )
                .templateConfig(builder -> {
                    builder.disable(TemplateType.CONTROLLER, TemplateType.SERVICE);
                })
                .templateEngine(new VelocityTemplateEngine())
                .execute();

        System.out.println("  [完成] Entity/Mapper/XML -> " + javaOutput);
    }

    // ======================== 自定义文件生成 (DTO/VO/Converter) ========================

    private static void generateCustomFiles(VelocityEngine ve, DatabaseConfig config,
                                            String entityName, String tableName, List<FieldInfo> allFields) {
        List<FieldInfo> businessFields = filterBusinessFields(allFields);
        List<FieldInfo> queryFields = filterQueryFields(businessFields);
        Set<String> fieldImports = collectImports(businessFields);
        String tableComment = readTableComment(config, tableName);

        String apiPackage = PARENT_PACKAGE + "." + config.modulePackage;
        String servicePackage = PARENT_PACKAGE + "." + config.modulePackage;
        String entityLowerFirst = Character.toLowerCase(entityName.charAt(0)) + entityName.substring(1);
        String serviceLowerFirst = Character.toLowerCase(entityName.charAt(0)) + entityName.substring(1) + "Service";

        String apiJavaRoot = PROJECT_ROOT + "services/" + config.apiModule + "/src/main/java";
        String serviceJavaRoot = PROJECT_ROOT + "services/" + config.serviceModule + "/src/main/java";

        VelocityContext ctx = new VelocityContext();
        ctx.put("entity", entityName);
        ctx.put("author", AUTHOR);
        ctx.put("date", java.time.LocalDate.now().toString());
        ctx.put("table", new HashMap<String, String>() {{
            put("comment", tableComment);
        }});
        ctx.put("cfg", new HashMap<String, Object>() {{
            put("apiPackage", apiPackage);
            put("servicePackage", servicePackage);
            put("commonPackage", "com.saas.cloud.common.core");
            put("entityLowerFirst", entityLowerFirst);
            put("serviceLowerFirst", serviceLowerFirst);
            put("businessFields", businessFields);
            put("queryFields", queryFields);
            put("fieldImports", fieldImports);
        }});

        // DTO/VO -> -api 模块
        String dtoDir = apiJavaRoot + "/" + apiPackage.replace(".", "/") + "/dto";
        String voDir = apiJavaRoot + "/" + apiPackage.replace(".", "/") + "/vo";
        renderTemplate(ve, "templates/createDTO.java.vm", ctx, dtoDir, entityName + "CreateDTO.java");
        renderTemplate(ve, "templates/updateDTO.java.vm", ctx, dtoDir, entityName + "UpdateDTO.java");
        renderTemplate(ve, "templates/queryDTO.java.vm", ctx, dtoDir, entityName + "QueryDTO.java");
        renderTemplate(ve, "templates/vo.java.vm", ctx, voDir, entityName + "VO.java");

        // Controller -> -service 模块
        String controllerDir = serviceJavaRoot + "/" + servicePackage.replace(".", "/") + "/controller";
        renderTemplate(ve, "templates/controller.java.vm", ctx, controllerDir, entityName + "Controller.java");

        // Service 接口 -> -service 模块
        String serviceDir = serviceJavaRoot + "/" + servicePackage.replace(".", "/") + "/service";
        renderTemplate(ve, "templates/service.java.vm", ctx, serviceDir, "I" + entityName + "Service.java");

        // ServiceImpl -> -service 模块
        String serviceImplDir = serviceJavaRoot + "/" + servicePackage.replace(".", "/") + "/service/impl";
        renderTemplate(ve, "templates/serviceImpl.java.vm", ctx, serviceImplDir, entityName + "ServiceImpl.java");

        // Converter -> -service 模块
        String converterDir = serviceJavaRoot + "/" + servicePackage.replace(".", "/") + "/converter";
        renderTemplate(ve, "templates/converter.java.vm", ctx, converterDir, entityName + "Converter.java");

        System.out.println("  [生成] " + entityName + ": Controller + Service + 3个DTO + VO + Converter");
    }

    // ======================== 表字段解析 ========================

    private static List<FieldInfo> readTableFields(DatabaseConfig config, String tableName) {
        List<FieldInfo> fields = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(config.getJdbcUrl(), config.username, config.password)) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet rs = metaData.getColumns(config.database, null, tableName, null);
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                String typeName = rs.getString("TYPE_NAME").toLowerCase().split("\\s")[0];
                String comment = rs.getString("REMARKS");
                if (comment == null || comment.isEmpty()) {
                    comment = columnName;
                }
                boolean nullable = "YES".equalsIgnoreCase(rs.getString("IS_NULLABLE"));
                int columnSize = rs.getInt("COLUMN_SIZE");
                String javaType = TYPE_MAP.getOrDefault(typeName, "String");
                String propertyName = columnToProperty(columnName);
                String simpleType = javaType.contains(".") ? javaType.substring(javaType.lastIndexOf(".") + 1) : javaType;
                fields.add(new FieldInfo(columnName, propertyName, javaType, simpleType, comment, nullable, columnSize));
            }
        } catch (Exception e) {
            System.err.println("  [错误] 读取表字段失败: " + tableName + " -> " + e.getMessage());
        }
        return fields;
    }

    private static String readTableComment(DatabaseConfig config, String tableName) {
        try (Connection conn = DriverManager.getConnection(config.getJdbcUrl(), config.username, config.password)) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet rs = metaData.getTables(config.database, null, tableName, new String[]{"TABLE"});
            if (rs.next()) {
                String comment = rs.getString("REMARKS");
                if (comment != null && !comment.isEmpty()) {
                    return comment;
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return tableName;
    }

    /** 过滤掉 BaseEntity/TenantBaseEntity 已有的字段，只保留业务字段 */
    private static List<FieldInfo> filterBusinessFields(List<FieldInfo> fields) {
        List<FieldInfo> result = new ArrayList<>();
        for (FieldInfo field : fields) {
            if (!SUPER_COLUMN_SET.contains(field.columnName)) {
                result.add(field);
            }
        }
        return result;
    }

    /** 从业务字段中提取适合做查询条件的字段（String/Integer/Long） */
    private static List<FieldInfo> filterQueryFields(List<FieldInfo> businessFields) {
        List<FieldInfo> result = new ArrayList<>();
        for (FieldInfo field : businessFields) {
            if (QUERY_TYPES.contains(field.propertyType)) {
                result.add(field);
            }
        }
        return result;
    }

    /** 收集需要 import 的非 java.lang 类型 */
    private static Set<String> collectImports(List<FieldInfo> fields) {
        Set<String> imports = new LinkedHashSet<>();
        for (FieldInfo field : fields) {
            if (field.fullType.contains(".")) {
                imports.add(field.fullType);
            }
        }
        return imports;
    }

    // ======================== 工具方法 ========================

    private static VelocityEngine initVelocity() {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADERS, "classpath");
        ve.setProperty("resource.loader.classpath.class", ClasspathResourceLoader.class.getName());
        ve.setProperty("input.encoding", "UTF-8");
        ve.setProperty("output.encoding", "UTF-8");
        ve.init();
        return ve;
    }

    private static void renderTemplate(VelocityEngine ve, String templatePath, VelocityContext ctx,
                                       String outputDir, String fileName) {
        try {
            File dir = new File(outputDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File outputFile = new File(dir, fileName);
            if (outputFile.exists()) {
                System.out.println("    [跳过] 已存在: " + fileName);
                return;
            }
            Template template = ve.getTemplate(templatePath, "UTF-8");
            try (Writer writer = new FileWriter(outputFile)) {
                template.merge(ctx, writer);
            }
        } catch (Exception e) {
            System.err.println("    [错误] 生成失败: " + fileName + " -> " + e.getMessage());
        }
    }

    /** 下划线列名 -> 驼峰属性名 */
    private static String columnToProperty(String column) {
        StringBuilder sb = new StringBuilder();
        boolean upper = false;
        for (char c : column.toCharArray()) {
            if (c == '_') {
                upper = true;
            } else {
                sb.append(upper ? Character.toUpperCase(c) : c);
                upper = false;
            }
        }
        return sb.toString();
    }

    /** 表名 -> 实体类名（去前缀 + 首字母大写） */
    private static String tableToEntityName(String tableName) {
        String name = tableName;
        for (String prefix : TABLE_PREFIX) {
            if (name.toLowerCase().startsWith(prefix)) {
                name = name.substring(prefix.length());
                break;
            }
        }
        return columnToProperty("_" + name);
    }

    private static List<String> scanTables(DatabaseConfig config) {
        List<String> tables = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(config.getJdbcUrl(), config.username, config.password)) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet rs = metaData.getTables(config.database, null, null, new String[]{"TABLE"});
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                if (!shouldExclude(tableName)) {
                    tables.add(tableName);
                }
            }
        } catch (Exception e) {
            System.err.println("  [错误] 连接数据库失败: " + config.database + " -> " + e.getMessage());
        }
        return tables;
    }

    private static boolean shouldExclude(String tableName) {
        String lower = tableName.toLowerCase();
        if (EXCLUDE_TABLES.contains(lower)) {
            return true;
        }
        for (String prefix : EXCLUDE_TABLE_PREFIX) {
            if (lower.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    // ======================== 内部类 ========================

    static class FieldInfo {
        final String columnName;
        final String propertyName;
        final String fullType;
        final String propertyType;
        final String comment;
        final boolean nullable;
        final int length;

        FieldInfo(String columnName, String propertyName, String fullType, String propertyType,
                  String comment, boolean nullable, int length) {
            this.columnName = columnName;
            this.propertyName = propertyName;
            this.fullType = fullType;
            this.propertyType = propertyType;
            this.comment = comment;
            this.nullable = nullable;
            this.length = length;
        }

        public String getColumnName() { return columnName; }
        public String getPropertyName() { return propertyName; }
        public String getFullType() { return fullType; }
        public String getPropertyType() { return propertyType; }
        public String getComment() { return comment; }
        public boolean isNullable() { return nullable; }
        public int getLength() { return length; }

        public String getCapitalizedName() {
            return Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
        }
    }

    static class DatabaseConfig {
        final String host;
        final int port;
        final String database;
        final String username;
        final String password;
        final String serviceModule;
        final String apiModule;
        final String modulePackage;
        final boolean hasTenantId;

        DatabaseConfig(String host, int port, String database, String username, String password,
                       String serviceModule, String apiModule, String modulePackage, boolean hasTenantId) {
            this.host = host;
            this.port = port;
            this.database = database;
            this.username = username;
            this.password = password;
            this.serviceModule = serviceModule;
            this.apiModule = apiModule;
            this.modulePackage = modulePackage;
            this.hasTenantId = hasTenantId;
        }

        String getJdbcUrl() {
            return "jdbc:mysql://" + host + ":" + port + "/" + database
                    + "?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai"
                    + "&useInformationSchema=true";
        }
    }
}
