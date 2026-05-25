package com.saas.cloud.generator.engine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

/**
 * 通用代码生成引擎
 * 读取数据库元数据，通过 Velocity 模板生成完整 CRUD 代码，
 * 支持输出到目录、打包 zip、内存预览三种模式。
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
public class GeneratorEngine {

    /** SQL 类型 -> Java 类型映射 */
    private static final Map<String, String> TYPE_MAP = new HashMap<>();

    /** 适合作为查询条件的 Java 类型 */
    private static final Set<String> QUERY_TYPES = new HashSet<>(Arrays.asList("String", "Integer", "Long"));

    /** 审计/公共字段（生成 DTO 时排除） */
    private static final Set<String> AUDIT_COLUMNS = new HashSet<>(Arrays.asList(
            "id", "tenant_id",
            "create_user_id", "create_user_no", "create_user_name", "create_time",
            "update_user_id", "update_user_no", "update_user_name", "update_time",
            "delete_flag", "valid_status", "data_version", "remark", "trace_id"
    ));

    /** 系统表排除列表 */
    private static final Set<String> SYSTEM_TABLES = new HashSet<>(Arrays.asList(
            "flyway_schema_history", "databasechangelog", "databasechangeloglock",
            "qrtz_blob_triggers", "qrtz_calendars", "qrtz_cron_triggers",
            "qrtz_fired_triggers", "qrtz_job_details", "qrtz_locks",
            "qrtz_paused_trigger_grps", "qrtz_scheduler_state",
            "qrtz_simple_triggers", "qrtz_simprop_triggers", "qrtz_triggers"
    ));

    private static final String[] SYSTEM_TABLE_PREFIX = {"act_", "flw_"};

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

    private final VelocityEngine velocityEngine;

    public GeneratorEngine() {
        this.velocityEngine = initVelocity();
    }

    // ======================== 公共 API ========================

    /**
     * 扫描数据库，返回所有符合条件的表元数据
     */
    public List<TableMeta> scanTables(GeneratorConfig config) {
        List<TableMeta> result = new ArrayList<>();
        String database = extractDatabase(config.getJdbcUrl());

        try (Connection conn = DriverManager.getConnection(config.getJdbcUrl(), config.getUsername(), config.getPassword())) {
            DatabaseMetaData metaData = conn.getMetaData();
            List<String> tableNames = listTableNames(metaData, database, config);

            for (String tableName : tableNames) {
                Set<String> pkColumns = readPrimaryKeys(metaData, database, tableName);
                List<FieldMeta> allFields = readFields(metaData, database, tableName, pkColumns);
                String entityName = tableToEntityName(tableName, config.getTablePrefix());
                String comment = readTableComment(metaData, database, tableName);

                TableMeta table = new TableMeta();
                table.setTableName(tableName);
                table.setEntityName(entityName);
                table.setEntityLowerFirst(Character.toLowerCase(entityName.charAt(0)) + entityName.substring(1));
                table.setComment(comment);
                table.setAllFields(allFields);
                table.setBusinessFields(filterBusinessFields(allFields));
                table.setQueryFields(filterQueryFields(table.getBusinessFields()));
                table.setFieldImports(collectImports(table.getBusinessFields()));
                table.setAllFieldImports(collectImports(allFields));
                result.add(table);
            }
        } catch (SQLException e) {
            throw new RuntimeException("扫描数据库失败: " + e.getMessage(), e);
        }
        return result;
    }

    /**
     * 生成全部代码到指定目录
     */
    public void generate(GeneratorConfig config, List<TableMeta> tables, Path outputDir) {
        String packagePath = config.getPackageName().replace(".", "/");
        String javaRoot = outputDir.resolve("java").resolve(packagePath).toString();
        String resourceRoot = outputDir.resolve("resources").toString();

        generateCommonClasses(config, javaRoot);

        for (TableMeta table : tables) {
            generateTableFiles(config, table, javaRoot, resourceRoot);
            System.out.println("  [生成] " + table.getEntityName() + ": Entity + Mapper + Controller + Service + 3个DTO + VO + Converter + Vue(index + form-modal + api)");
        }
        System.out.println("\n========== 完成！共生成 " + tables.size() + " 张表 ==========");
    }

    /**
     * 生成代码并打包为 zip 字节数组
     */
    public byte[] generateZip(GeneratorConfig config) {
        try {
            Path tempDir = Files.createTempDirectory("code-gen-");
            try {
                List<TableMeta> tables = scanTables(config);
                generate(config, tables, tempDir);
                return zipDirectory(tempDir);
            } finally {
                deleteRecursive(tempDir);
            }
        } catch (IOException e) {
            throw new RuntimeException("生成代码失败: " + e.getMessage(), e);
        }
    }

    /**
     * 预览单表生成代码（不落盘，返回文件路径->内容的映射）
     */
    public Map<String, String> preview(GeneratorConfig config, String tableName) {
        Map<String, String> files = new LinkedHashMap<>();
        GeneratorConfig singleConfig = copySingleTableConfig(config, tableName);
        List<TableMeta> tables = scanTables(singleConfig);
        if (tables.isEmpty()) {
            return files;
        }

        TableMeta table = tables.get(0);
        VelocityContext ctx = buildContext(config, table);
        String e = table.getEntityName();

        files.put("entity/" + e + ".java", renderToString("templates/universal/entity.java.vm", ctx));
        files.put("mapper/" + e + "Mapper.java", renderToString("templates/universal/mapper.java.vm", ctx));
        files.put("mapper/" + e + "Mapper.xml", renderToString("templates/universal/mapperXml.vm", ctx));
        files.put("controller/" + e + "Controller.java", renderToString("templates/controller.java.vm", ctx));
        files.put("service/I" + e + "Service.java", renderToString("templates/service.java.vm", ctx));
        files.put("service/impl/" + e + "ServiceImpl.java", renderToString("templates/serviceImpl.java.vm", ctx));
        files.put("dto/" + e + "CreateDTO.java", renderToString("templates/createDTO.java.vm", ctx));
        files.put("dto/" + e + "UpdateDTO.java", renderToString("templates/updateDTO.java.vm", ctx));
        files.put("dto/" + e + "QueryDTO.java", renderToString("templates/queryDTO.java.vm", ctx));
        files.put("vo/" + e + "VO.java", renderToString("templates/vo.java.vm", ctx));
        files.put("converter/" + e + "Converter.java", renderToString("templates/converter.java.vm", ctx));

        String lowerFirst = table.getEntityLowerFirst();
        files.put("vue/" + lowerFirst + "/index.vue", renderToString("templates/vue/index.vue.vm", ctx));
        files.put("vue/" + lowerFirst + "/" + lowerFirst + "-form-modal.vue", renderToString("templates/vue/form-modal.vue.vm", ctx));
        files.put("vue/" + lowerFirst + "/api.ts", renderToString("templates/vue/api.ts.vm", ctx));

        return files;
    }

    /**
     * 仅获取表列表（不读取字段，用于前端展示可选表）
     */
    public List<Map<String, String>> listTables(GeneratorConfig config) {
        List<Map<String, String>> result = new ArrayList<>();
        String database = extractDatabase(config.getJdbcUrl());

        try (Connection conn = DriverManager.getConnection(config.getJdbcUrl(), config.getUsername(), config.getPassword())) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet rs = metaData.getTables(database, null, null, new String[]{"TABLE"});
            while (rs.next()) {
                String name = rs.getString("TABLE_NAME");
                if (isSystemTable(name)) {
                    continue;
                }
                Map<String, String> info = new LinkedHashMap<>();
                info.put("name", name);
                String comment = rs.getString("REMARKS");
                info.put("comment", comment != null && !comment.isEmpty() ? comment : name);
                result.add(info);
            }
        } catch (SQLException e) {
            throw new RuntimeException("连接数据库失败: " + e.getMessage(), e);
        }
        return result;
    }

    // ======================== 生成逻辑 ========================

    private void generateCommonClasses(GeneratorConfig config, String javaRoot) {
        VelocityContext ctx = new VelocityContext();
        ctx.put("packageName", config.getPackageName());
        ctx.put("author", config.getAuthor());
        ctx.put("date", LocalDate.now().toString());

        String resultDir = javaRoot + "/common/result";
        String exceptionDir = javaRoot + "/common/exception";

        renderToFile("templates/universal/ApiResult.java.vm", ctx, resultDir, "ApiResult.java");
        renderToFile("templates/universal/PageResult.java.vm", ctx, resultDir, "PageResult.java");
        renderToFile("templates/universal/ResultCode.java.vm", ctx, resultDir, "ResultCode.java");
        renderToFile("templates/universal/BusinessException.java.vm", ctx, exceptionDir, "BusinessException.java");
        renderToFile("templates/universal/GlobalExceptionHandler.java.vm", ctx, exceptionDir, "GlobalExceptionHandler.java");

        System.out.println("  [生成] common: ApiResult + PageResult + ResultCode + BusinessException + GlobalExceptionHandler");
    }

    private void generateTableFiles(GeneratorConfig config, TableMeta table, String javaRoot, String resourceRoot) {
        VelocityContext ctx = buildContext(config, table);
        String e = table.getEntityName();

        renderToFile("templates/universal/entity.java.vm", ctx, javaRoot + "/entity", e + ".java");
        renderToFile("templates/universal/mapper.java.vm", ctx, javaRoot + "/mapper", e + "Mapper.java");
        renderToFile("templates/universal/mapperXml.vm", ctx, resourceRoot + "/mapper", e + "Mapper.xml");
        renderToFile("templates/controller.java.vm", ctx, javaRoot + "/controller", e + "Controller.java");
        renderToFile("templates/service.java.vm", ctx, javaRoot + "/service", "I" + e + "Service.java");
        renderToFile("templates/serviceImpl.java.vm", ctx, javaRoot + "/service/impl", e + "ServiceImpl.java");
        renderToFile("templates/createDTO.java.vm", ctx, javaRoot + "/dto", e + "CreateDTO.java");
        renderToFile("templates/updateDTO.java.vm", ctx, javaRoot + "/dto", e + "UpdateDTO.java");
        renderToFile("templates/queryDTO.java.vm", ctx, javaRoot + "/dto", e + "QueryDTO.java");
        renderToFile("templates/vo.java.vm", ctx, javaRoot + "/vo", e + "VO.java");
        renderToFile("templates/converter.java.vm", ctx, javaRoot + "/converter", e + "Converter.java");

        String vueDir = javaRoot + "/../../vue/" + table.getEntityLowerFirst();
        renderToFile("templates/vue/index.vue.vm", ctx, vueDir, "index.vue");
        renderToFile("templates/vue/form-modal.vue.vm", ctx, vueDir, table.getEntityLowerFirst() + "-form-modal.vue");
        renderToFile("templates/vue/api.ts.vm", ctx, vueDir, "api.ts");
    }

    private VelocityContext buildContext(GeneratorConfig config, TableMeta table) {
        VelocityContext ctx = new VelocityContext();
        ctx.put("entity", table.getEntityName());
        ctx.put("author", config.getAuthor());
        ctx.put("date", LocalDate.now().toString());
        ctx.put("table", new HashMap<String, String>() {{
            put("comment", table.getComment());
        }});
        ctx.put("cfg", new HashMap<String, Object>() {{
            put("packageName", config.getPackageName());
            put("apiPackage", config.getPackageName());
            put("servicePackage", config.getPackageName());
            put("commonPackage", config.getPackageName() + ".common");
            put("entityLowerFirst", table.getEntityLowerFirst());
            put("serviceLowerFirst", table.getEntityLowerFirst() + "Service");
            put("tableName", table.getTableName());
            put("businessFields", table.getBusinessFields());
            put("queryFields", table.getQueryFields());
            put("fieldImports", table.getFieldImports());
            put("allFields", table.getAllFields());
            put("allFieldImports", table.getAllFieldImports());
        }});
        return ctx;
    }

    // ======================== 数据库元数据读取 ========================

    private List<String> listTableNames(DatabaseMetaData metaData, String database, GeneratorConfig config) throws SQLException {
        List<String> names = new ArrayList<>();
        ResultSet rs = metaData.getTables(database, null, null, new String[]{"TABLE"});
        while (rs.next()) {
            String name = rs.getString("TABLE_NAME");
            if (isSystemTable(name)) {
                continue;
            }
            if (!config.getExcludeTables().isEmpty() && config.getExcludeTables().contains(name)) {
                continue;
            }
            if (!config.getIncludeTables().isEmpty() && !config.getIncludeTables().contains(name)) {
                continue;
            }
            names.add(name);
        }
        return names;
    }

    private List<FieldMeta> readFields(DatabaseMetaData metaData, String database, String tableName, Set<String> pkColumns) throws SQLException {
        List<FieldMeta> fields = new ArrayList<>();
        ResultSet rs = metaData.getColumns(database, null, tableName, null);
        while (rs.next()) {
            String columnName = rs.getString("COLUMN_NAME");
            String typeName = rs.getString("TYPE_NAME").toLowerCase().split("\\s")[0];
            String comment = rs.getString("REMARKS");
            if (comment == null || comment.isEmpty()) {
                comment = columnName;
            }

            String javaType = TYPE_MAP.getOrDefault(typeName, "String");
            String propertyName = columnToProperty(columnName);
            String simpleType = javaType.contains(".") ? javaType.substring(javaType.lastIndexOf(".") + 1) : javaType;

            FieldMeta field = new FieldMeta();
            field.setColumnName(columnName);
            field.setPropertyName(propertyName);
            field.setCapitalizedName(Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1));
            field.setFullType(javaType);
            field.setPropertyType(simpleType);
            field.setComment(comment);
            field.setPrimaryKey(pkColumns.contains(columnName));
            field.setColumnType(rs.getString("TYPE_NAME"));
            field.setNullable("YES".equalsIgnoreCase(rs.getString("IS_NULLABLE")));
            field.setLength(rs.getInt("COLUMN_SIZE"));
            fields.add(field);
        }
        return fields;
    }

    private Set<String> readPrimaryKeys(DatabaseMetaData metaData, String database, String tableName) throws SQLException {
        Set<String> pks = new HashSet<>();
        ResultSet rs = metaData.getPrimaryKeys(database, null, tableName);
        while (rs.next()) {
            pks.add(rs.getString("COLUMN_NAME"));
        }
        return pks;
    }

    private String readTableComment(DatabaseMetaData metaData, String database, String tableName) throws SQLException {
        ResultSet rs = metaData.getTables(database, null, tableName, new String[]{"TABLE"});
        if (rs.next()) {
            String comment = rs.getString("REMARKS");
            if (comment != null && !comment.isEmpty()) {
                return comment;
            }
        }
        return tableName;
    }

    // ======================== 字段过滤 ========================

    private List<FieldMeta> filterBusinessFields(List<FieldMeta> allFields) {
        List<FieldMeta> result = new ArrayList<>();
        for (FieldMeta field : allFields) {
            if (!AUDIT_COLUMNS.contains(field.getColumnName())) {
                result.add(field);
            }
        }
        return result;
    }

    private List<FieldMeta> filterQueryFields(List<FieldMeta> businessFields) {
        List<FieldMeta> result = new ArrayList<>();
        for (FieldMeta field : businessFields) {
            if (QUERY_TYPES.contains(field.getPropertyType())) {
                result.add(field);
            }
        }
        return result;
    }

    private Set<String> collectImports(List<FieldMeta> fields) {
        Set<String> imports = new LinkedHashSet<>();
        for (FieldMeta field : fields) {
            if (field.getFullType().contains(".")) {
                imports.add(field.getFullType());
            }
        }
        return imports;
    }

    // ======================== 模板渲染 ========================

    private void renderToFile(String templatePath, VelocityContext ctx, String outputDir, String fileName) {
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
            Template template = velocityEngine.getTemplate(templatePath, "UTF-8");
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8)) {
                template.merge(ctx, writer);
            }
        } catch (Exception e) {
            System.err.println("    [错误] 生成失败: " + fileName + " -> " + e.getMessage());
        }
    }

    private String renderToString(String templatePath, VelocityContext ctx) {
        Template template = velocityEngine.getTemplate(templatePath, "UTF-8");
        StringWriter writer = new StringWriter();
        template.merge(ctx, writer);
        return writer.toString();
    }

    // ======================== 工具方法 ========================

    private VelocityEngine initVelocity() {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADERS, "classpath");
        ve.setProperty("resource.loader.classpath.class", ClasspathResourceLoader.class.getName());
        ve.setProperty("input.encoding", "UTF-8");
        ve.setProperty("output.encoding", "UTF-8");
        ve.init();
        return ve;
    }

    private String columnToProperty(String column) {
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

    private String tableToEntityName(String tableName, List<String> prefixes) {
        String name = tableName;
        for (String prefix : prefixes) {
            if (name.toLowerCase().startsWith(prefix.toLowerCase())) {
                name = name.substring(prefix.length());
                break;
            }
        }
        return columnToProperty("_" + name);
    }

    private String extractDatabase(String jdbcUrl) {
        String url = jdbcUrl;
        int idx = url.indexOf("?");
        if (idx > 0) {
            url = url.substring(0, idx);
        }
        return url.substring(url.lastIndexOf("/") + 1);
    }

    private boolean isSystemTable(String tableName) {
        String lower = tableName.toLowerCase();
        if (SYSTEM_TABLES.contains(lower)) {
            return true;
        }
        for (String prefix : SYSTEM_TABLE_PREFIX) {
            if (lower.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private GeneratorConfig copySingleTableConfig(GeneratorConfig source, String tableName) {
        GeneratorConfig c = new GeneratorConfig();
        c.setJdbcUrl(source.getJdbcUrl());
        c.setUsername(source.getUsername());
        c.setPassword(source.getPassword());
        c.setPackageName(source.getPackageName());
        c.setAuthor(source.getAuthor());
        c.setTablePrefix(source.getTablePrefix());
        c.setIncludeTables(Collections.singletonList(tableName));
        return c;
    }

    private byte[] zipDirectory(Path sourceDir) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos, StandardCharsets.UTF_8)) {
            Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String entryName = sourceDir.relativize(file).toString().replace("\\", "/");
                    zos.putNextEntry(new ZipEntry(entryName));
                    Files.copy(file, zos);
                    zos.closeEntry();
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        return baos.toByteArray();
    }

    private void deleteRecursive(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            return;
        }
        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path d, IOException exc) throws IOException {
                Files.delete(d);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
