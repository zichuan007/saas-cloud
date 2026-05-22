package com.saas.cloud.common.core.result;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * 分页结果包装
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<T> records;
    private long total;
    private int pageNum;
    private int pageSize;
    private long pages;

    public static <T> PageResult<T> of(List<T> records, long total, int pageNum, int pageSize) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(records);
        result.setTotal(total);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setPages((total + pageSize - 1) / pageSize);
        return result;
    }
}
