package com.saas.cloud.rbac.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.rbac.entity.SensitiveWord;

/**
 * 敏感词服务
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
public interface ISensitiveWordService extends IService<SensitiveWord> {

    /**
     * 分页查询敏感词
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param keyword  关键字
     * @return 分页结果
     */
    PageResult<SensitiveWord> pageSensitiveWords(int pageNum, int pageSize, String keyword);

    /**
     * 添加敏感词
     *
     * @param word     敏感词
     * @param category 分类
     */
    void addWord(String word, String category);

    /**
     * 删除敏感词
     *
     * @param id 敏感词ID
     */
    void deleteWord(Long id);

    /**
     * 检测文本中的敏感词
     *
     * @param text 待检测文本
     * @return 命中的敏感词列表
     */
    List<String> checkText(String text);

    /**
     * 过滤文本（敏感词替换为 ***）
     *
     * @param text 待过滤文本
     * @return 过滤后的文本
     */
    String filterText(String text);
}
