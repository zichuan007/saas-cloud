package com.saas.cloud.workflow.controller;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.workflow.tunnel.mapper.FlowDraftMapper;
import com.saas.cloud.workflow.tunnel.dataobject.FlowDraftDO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "流程草稿")
@RestController
@RequestMapping("/draft")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DraftController {

    private final FlowDraftMapper draftMapper;

    @GetMapping("/my")
    @Operation(summary = "我的草稿列表")
    public ApiResult<List<FlowDraftDO>> myDrafts() {
        return ApiResult.ok(List.of());
    }

    @PostMapping
    @Operation(summary = "保存草稿")
    public ApiResult<Void> save(@RequestBody FlowDraftDO draft) {
        draftMapper.insert(draft);
        return ApiResult.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除草稿")
    public ApiResult<Void> delete(@PathVariable Long id) {
        draftMapper.deleteById(id);
        return ApiResult.ok();
    }
}