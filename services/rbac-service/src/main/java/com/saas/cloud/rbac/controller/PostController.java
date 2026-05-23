package com.saas.cloud.rbac.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.common.log.annotation.OperationLog;
import com.saas.cloud.rbac.api.dto.PostCreateDTO;
import com.saas.cloud.rbac.api.dto.PostUpdateDTO;
import com.saas.cloud.rbac.api.vo.PostVO;
import com.saas.cloud.rbac.service.IPostService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 岗位管理 接口
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Slf4j
@Tag(name = "岗位管理")
@RestController
@RequestMapping("/post")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class PostController {

    private final IPostService postService;

    /**
     * 分页查询岗位列表
     *
     * @param postName 岗位名称
     * @param status   状态
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    @Operation(summary = "分页查询岗位列表")
    @GetMapping("/list")
    public ApiResult<PageResult<PostVO>> list(
            @RequestParam(required = false) String postName,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResult.ok(postService.queryPage(postName, status, pageNum, pageSize));
    }

    /**
     * 下拉选择（全量返回启用岗位）
     *
     * @return 岗位列表
     */
    @Operation(summary = "下拉选择岗位列表")
    @GetMapping("/select")
    public ApiResult<List<PostVO>> selectList() {
        return ApiResult.ok(postService.selectList());
    }

    /**
     * 岗位详情
     *
     * @param id 岗位ID
     * @return 详情
     */
    @Operation(summary = "岗位详情")
    @GetMapping("/{id}")
    public ApiResult<PostVO> detail(@PathVariable Long id) {
        return ApiResult.ok(postService.queryById(id));
    }

    /**
     * 创建岗位
     *
     * @param createDTO 创建参数
     * @return 主键ID
     */
    @Operation(summary = "创建岗位")
    @OperationLog(module = "岗位管理", operation = "创建岗位")
    @PostMapping
    public ApiResult<Long> create(@Valid @RequestBody PostCreateDTO createDTO) {
        return ApiResult.ok(postService.create(createDTO));
    }

    /**
     * 更新岗位
     *
     * @param id        岗位ID
     * @param updateDTO 修改参数
     * @return 操作结果
     */
    @Operation(summary = "更新岗位")
    @OperationLog(module = "岗位管理", operation = "更新岗位")
    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable Long id, @Valid @RequestBody PostUpdateDTO updateDTO) {
        updateDTO.setId(id);
        postService.update(updateDTO);
        return ApiResult.ok();
    }

    /**
     * 删除岗位
     *
     * @param id 岗位ID
     * @return 操作结果
     */
    @Operation(summary = "删除岗位")
    @OperationLog(module = "岗位管理", operation = "删除岗位")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        postService.deleteById(id);
        return ApiResult.ok();
    }
}
