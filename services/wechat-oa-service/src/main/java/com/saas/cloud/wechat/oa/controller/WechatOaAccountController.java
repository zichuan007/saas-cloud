package com.saas.cloud.wechat.oa.controller;

import com.saas.cloud.common.core.result.ApiResult;
import com.saas.cloud.common.log.annotation.OperationLog;
import com.saas.cloud.wechat.oa.entity.WechatOaAccount;
import com.saas.cloud.wechat.oa.service.IWechatOaAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公众号账号控制器
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@RestController
@RequestMapping("/account")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class WechatOaAccountController {

    private final IWechatOaAccountService accountService;

    /**
     * 公众号列表
     *
     * @return 公众号列表
     */
    @GetMapping("/list")
    public ApiResult<List<WechatOaAccount>> list() {
        return ApiResult.ok(accountService.listAccounts());
    }

    /**
     * 公众号详情
     *
     * @param id 公众号ID
     * @return 公众号详情
     */
    @GetMapping("/{id}")
    public ApiResult<WechatOaAccount> detail(@PathVariable("id") Long id) {
        return ApiResult.ok(accountService.getAccountDetail(id));
    }

    /**
     * 绑定公众号
     *
     * @param account 公众号信息
     * @return 操作结果
     */
    @OperationLog(module = "公众号管理", operation = "绑定公众号")
    @PostMapping
    public ApiResult<Void> bind(@RequestBody WechatOaAccount account) {
        accountService.bindAccount(account);
        return ApiResult.ok();
    }

    /**
     * 编辑公众号信息
     *
     * @param id      公众号ID
     * @param account 公众号信息
     * @return 操作结果
     */
    @OperationLog(module = "公众号管理", operation = "编辑公众号")
    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable("id") Long id,
                                  @RequestBody WechatOaAccount account) {
        accountService.updateAccount(id, account);
        return ApiResult.ok();
    }

    /**
     * 解绑公众号
     *
     * @param id 公众号ID
     * @return 操作结果
     */
    @OperationLog(module = "公众号管理", operation = "解绑公众号")
    @DeleteMapping("/{id}")
    public ApiResult<Void> unbind(@PathVariable("id") Long id) {
        accountService.unbindAccount(id);
        return ApiResult.ok();
    }
}
