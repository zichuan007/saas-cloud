package com.saas.cloud.wechat.oa.controller;

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
import com.saas.cloud.wechat.oa.entity.WechatOaMaterial;
import com.saas.cloud.wechat.oa.service.IWechatOaMaterialService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 素材管理控制器
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Tag(name = "公众号素材管理")
@RestController
@RequestMapping("/material")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class WechatOaMaterialController {

    private final IWechatOaMaterialService materialService;

    /**
     * 素材列表
     *
     * @param accountId    公众号ID
     * @param materialType 素材类型
     * @param pageNum      页码
     * @param pageSize     每页条数
     * @return 分页结果
     */
    @Operation(summary = "素材列表")
    @GetMapping("/list")
    public ApiResult<PageResult<WechatOaMaterial>> list(@RequestParam Long accountId,
                                                        @RequestParam(required = false) Byte materialType,
                                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        return ApiResult.ok(materialService.pageMaterials(accountId, materialType, pageNum, pageSize));
    }

    /**
     * 上传素材（保存素材记录）
     *
     * @param material 素材信息
     * @return 操作结果
     */
    @Operation(summary = "上传素材")
    @OperationLog(module = "素材管理", operation = "上传素材")
    @PostMapping("/upload")
    public ApiResult<Void> upload(@RequestBody WechatOaMaterial material) {
        materialService.saveMaterial(material);
        return ApiResult.ok();
    }

    /**
     * 删除素材
     *
     * @param id 素材ID
     * @return 操作结果
     */
    @Operation(summary = "删除素材")
    @OperationLog(module = "素材管理", operation = "删除素材")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable("id") Long id) {
        materialService.deleteMaterial(id);
        return ApiResult.ok();
    }

    /**
     * 同步素材到微信
     *
     * @param id 素材ID
     * @return 操作结果
     */
    @Operation(summary = "同步素材到微信")
    @OperationLog(module = "素材管理", operation = "同步素材到微信")
    @PostMapping("/{id}/sync")
    public ApiResult<Void> syncToWechat(@PathVariable("id") Long id) {
        materialService.syncToWechat(id);
        return ApiResult.ok();
    }
}
