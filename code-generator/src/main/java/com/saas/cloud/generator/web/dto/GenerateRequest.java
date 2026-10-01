package com.saas.cloud.generator.web.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 代码生成请求
 * <p>继承 ConnectRequest，复用连接参数，后端统一拼 JDBC URL</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GenerateRequest extends ConnectRequest {

    /** 生成代码的根包名 */
    @NotBlank(message = "包名不能为空")
    private String packageName;

    /** 作者 */
    private String author = "generator";

    /** 表前缀 */
    private List<String> tablePrefix = new ArrayList<>();

    /** 要生成的表名列表（空=全部） */
    private List<String> tables = new ArrayList<>();

    /** 预览模式下的单表名 */
    private String previewTable;
}
