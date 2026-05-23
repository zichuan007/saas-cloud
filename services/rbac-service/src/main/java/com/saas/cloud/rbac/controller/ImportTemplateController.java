package com.saas.cloud.rbac.controller;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saas.cloud.common.core.exception.BusinessException;
import com.saas.cloud.common.excel.ExcelUtils;
import com.saas.cloud.rbac.api.vo.UserImportVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 导入模板下载
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Tag(name = "导入模板")
@RestController
@RequestMapping("/import-template")
public class ImportTemplateController {

    private static final Map<String, Class<?>> TEMPLATE_MAP;

    static {
        Map<String, Class<?>> map = new HashMap<>();
        map.put("user", UserImportVO.class);
        TEMPLATE_MAP = Collections.unmodifiableMap(map);
    }

    @Operation(summary = "下载导入模板")
    @GetMapping("/{bizType}")
    public void downloadTemplate(@PathVariable String bizType, HttpServletResponse response) throws IOException {
        Class<?> clazz = TEMPLATE_MAP.get(bizType);
        if (clazz == null) {
            throw new BusinessException("不支持的模板类型: " + bizType);
        }
        ExcelUtils.write(response, "导入模板_" + bizType, "数据", clazz, Collections.emptyList());
    }
}
