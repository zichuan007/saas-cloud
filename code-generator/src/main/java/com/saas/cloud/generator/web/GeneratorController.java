package com.saas.cloud.generator.web;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.generator.engine.GeneratorConfig;
import com.saas.cloud.generator.engine.GeneratorEngine;
import com.saas.cloud.generator.web.dto.ConnectRequest;
import com.saas.cloud.generator.web.dto.GenerateRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 代码生成器 Web 接口
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Slf4j
@RestController
@RequestMapping("/generator")
@CrossOrigin(origins = "*")
public class GeneratorController {

    private final GeneratorEngine engine = new GeneratorEngine();

    /**
     * 测试数据库连接并返回表列表
     *
     * @param req 连接信息
     * @return 表列表
     */
    @PostMapping("/connect")
    public ApiResult<List<Map<String, String>>> connect(@Valid @RequestBody ConnectRequest req) {
        GeneratorConfig config = new GeneratorConfig();
        config.setJdbcUrl(req.getJdbcUrl());
        config.setUsername(req.getUsername());
        config.setPassword(req.getPassword());

        List<Map<String, String>> tables = engine.listTables(config);
        log.info("连接成功，发现 {} 张表", tables.size());
        return ApiResult.ok(tables);
    }

    /**
     * 预览单表生成代码
     *
     * @param req 生成配置（含 previewTable）
     * @return 文件名->代码内容的映射
     */
    @PostMapping("/preview")
    public ApiResult<Map<String, String>> preview(@Valid @RequestBody GenerateRequest req) {
        GeneratorConfig config = toConfig(req);
        Map<String, String> files = engine.preview(config, req.getPreviewTable());
        return ApiResult.ok(files);
    }

    /**
     * 生成代码并下载 zip
     *
     * @param req      生成配置
     * @param response HTTP 响应
     */
    @PostMapping("/download")
    public void download(@Valid @RequestBody GenerateRequest req, HttpServletResponse response) throws IOException {
        GeneratorConfig config = toConfig(req);
        config.setIncludeTables(req.getTables());

        log.info("开始生成代码: package={}, tables={}", config.getPackageName(), config.getIncludeTables());
        byte[] zipBytes = engine.generateZip(config);

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=generated-code.zip");
        response.setContentLength(zipBytes.length);
        response.getOutputStream().write(zipBytes);
        response.getOutputStream().flush();
        log.info("代码生成完成，zip 大小: {} bytes", zipBytes.length);
    }

    private GeneratorConfig toConfig(GenerateRequest req) {
        GeneratorConfig config = new GeneratorConfig();
        config.setJdbcUrl(req.getJdbcUrl());
        config.setUsername(req.getUsername());
        config.setPassword(req.getPassword());
        config.setPackageName(req.getPackageName());
        config.setAuthor(req.getAuthor());
        config.setTablePrefix(req.getTablePrefix());
        return config;
    }
}
