package com.saas.cloud.rbac.controller;

import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 验证码接口
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-21
 */
@Slf4j
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
    @PostMapping("/check")
    public ResponseModel check(@RequestBody CaptchaVO captchaVO) {
        return captchaService.check(captchaVO);
    }
}
