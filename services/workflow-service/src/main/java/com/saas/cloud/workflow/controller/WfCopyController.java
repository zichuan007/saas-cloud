package com.saas.cloud.workflow.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 流程抄送表控制器（抄送相关接口已整合到 WfTaskExtController）
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Tag(name = "抄送管理")
@RestController
@RequestMapping("/copy")
public class WfCopyController {

}
