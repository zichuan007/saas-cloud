package com.saas.cloud.platform.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.log.annotation.OperationLog;
import com.saas.cloud.platform.api.vo.SysFileVO;
import com.saas.cloud.platform.service.ISysFileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 文件管理控制器
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-21
 */
@Tag(name = "文件管理")
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class FileController {

    private final ISysFileService sysFileService;

    /**
     * 上传文件
     *
     * @param file    上传文件
     * @param bizType 业务类型
     * @param bizId   关联业务ID（可选）
     * @return 文件信息
     */
    @Operation(summary = "上传文件")
    @OperationLog(module = "文件管理", operation = "上传文件")
    @PostMapping("/upload")
    @com.saas.cloud.common.redis.idempotent.Idempotent(key = "'upload:' + #file.originalFilename + ':' + #file.size", timeout = 5)
    public ApiResult<SysFileVO> upload(@RequestParam("file") MultipartFile file,
                                       @RequestParam(value = "bizType", defaultValue = "default") String bizType,
                                       @RequestParam(value = "bizId", required = false) String bizId) {
        return ApiResult.ok(sysFileService.upload(file, bizType, bizId));
    }

    /**
     * 获取文件预签名访问URL
     *
     * @param id 文件ID
     * @return 预签名URL（30分钟有效）
     */
    @Operation(summary = "获取文件预签名访问URL")
    @GetMapping("/{id}/url")
    public ApiResult<String> getUrl(@PathVariable("id") Long id) {
        return ApiResult.ok(sysFileService.getPresignedUrl(id));
    }

    /**
     * 根据业务类型和业务ID查询文件列表
     *
     * @param bizType 业务类型
     * @param bizId   业务ID
     * @return 文件列表
     */
    @Operation(summary = "根据业务类型和业务ID查询文件列表")
    @GetMapping("/list")
    public ApiResult<List<SysFileVO>> listByBiz(@RequestParam("bizType") String bizType,
                                                 @RequestParam("bizId") String bizId) {
        return ApiResult.ok(sysFileService.listByBiz(bizType, bizId));
    }

    /**
     * 删除文件
     *
     * @param id 文件ID
     * @return 操作结果
     */
    @Operation(summary = "删除文件")
    @OperationLog(module = "文件管理", operation = "删除文件")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable("id") Long id) {
        sysFileService.deleteFile(id);
        return ApiResult.ok();
    }
}
