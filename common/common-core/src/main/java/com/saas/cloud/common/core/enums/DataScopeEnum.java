package com.saas.cloud.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据范围枚举
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Getter
@AllArgsConstructor
public enum DataScopeEnum {

    ALL(1, "全部数据"),
    DEPT_AND_CHILDREN(2, "本部门及下级"),
    DEPT(3, "本部门"),
    SELF(4, "仅本人"),
    CUSTOM(5, "自定义");

    private final int code;
    private final String desc;
}
