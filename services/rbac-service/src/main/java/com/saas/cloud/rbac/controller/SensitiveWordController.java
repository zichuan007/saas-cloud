package com.saas.cloud.rbac.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.common.log.annotation.OperationLog;
import com.saas.cloud.rbac.entity.SensitiveWord;
import com.saas.cloud.rbac.service.ISensitiveWordService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 敏感词管理
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Tag(name = "敏感词管理")
@RestController
@RequestMapping("/sensitive-word")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SensitiveWordController {

    private final ISensitiveWordService sensitiveWordService;

    /**
     * 分页查询敏感词
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param keyword  搜索关键字
     * @return 分页数据
     */
    @Operation(summary = "分页查询敏感词")
    @GetMapping("/list")
    public ApiResult<PageResult<SensitiveWord>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        return ApiResult.ok(sensitiveWordService.pageSensitiveWords(pageNum, pageSize, keyword));
    }

    /**
     * 添加敏感词
     *
     * @param params 包含 word、category 字段
     * @return 操作结果
     */
    @Operation(summary = "添加敏感词")
    @OperationLog(module = "敏感词管理", operation = "添加敏感词")
    @PostMapping
    public ApiResult<Void> add(@RequestBody Map<String, String> params) {
        sensitiveWordService.addWord(params.get("word"), params.get("category"));
        return ApiResult.ok();
    }

    /**
     * 删除敏感词
     *
     * @param id 敏感词ID
     * @return 操作结果
     */
    @Operation(summary = "删除敏感词")
    @OperationLog(module = "敏感词管理", operation = "删除敏感词")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        sensitiveWordService.deleteWord(id);
        return ApiResult.ok();
    }

    /**
     * 在线检测文本
     *
     * @param params 包含 text 字段
     * @return 命中的敏感词列表
     */
    @Operation(summary = "在线检测敏感词")
    @PostMapping("/check")
    public ApiResult<List<String>> check(@RequestBody Map<String, String> params) {
        return ApiResult.ok(sensitiveWordService.checkText(params.get("text")));
    }

    /**
     * 过滤文本
     *
     * @param params 包含 text 字段
     * @return 过滤后的文本
     */
    @Operation(summary = "过滤文本中的敏感词")
    @PostMapping("/filter")
    public ApiResult<String> filter(@RequestBody Map<String, String> params) {
        return ApiResult.ok(sensitiveWordService.filterText(params.get("text")));
    }
}
