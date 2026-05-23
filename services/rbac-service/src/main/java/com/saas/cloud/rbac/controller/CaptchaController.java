package com.saas.cloud.rbac.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 验证码接口
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-21
 */
@Slf4j
@Tag(name = "验证码")
@RestController
@RequestMapping("/captcha")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class CaptchaController {

    private final CaptchaService captchaService;

    /**
     * 获取验证码
     *
     * @param captchaVO 验证码请求参数
     * @return 验证码数据（含底图和滑块）
     */
    @Operation(summary = "获取验证码")
    @PostMapping("/get")
    public ResponseModel get(@RequestBody CaptchaVO captchaVO) {
        return captchaService.get(captchaVO);
    }

    /**
     * 校验验证码
     *
     * @param captchaVO 验证码校验参数
     * @return 校验结果
     */
    @Operation(summary = "校验验证码")
    @PostMapping("/check")
    public ResponseModel check(@RequestBody CaptchaVO captchaVO) {
        return captchaService.check(captchaVO);
    }
}
