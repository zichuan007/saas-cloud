package com.saas.cloud.rbac.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.common.core.util.DfaFilter;
import com.saas.cloud.rbac.entity.SensitiveWord;
import com.saas.cloud.rbac.mapper.SensitiveWordMapper;
import com.saas.cloud.rbac.service.ISensitiveWordService;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * 敏感词服务实现
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-23
 */
@Slf4j
@Service
public class SensitiveWordServiceImpl extends ServiceImpl<SensitiveWordMapper, SensitiveWord>
        implements ISensitiveWordService {

    private final DfaFilter dfaFilter = new DfaFilter();

    @PostConstruct
    public void initDfa() {
        rebuildDfa();
    }

    @Override
    public PageResult<SensitiveWord> pageSensitiveWords(int pageNum, int pageSize, String keyword) {
        LambdaQueryWrapper<SensitiveWord> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(SensitiveWord::getWord, keyword);
        }
        wrapper.orderByDesc(SensitiveWord::getCreateTime);
        Page<SensitiveWord> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), pageNum, pageSize);
    }

    @Override
    public void addWord(String word, String category) {
        SensitiveWord entity = new SensitiveWord();
        entity.setWord(word);
        entity.setCategory(category);
        entity.setStatus((byte) 1);
        this.save(entity);
        rebuildDfa();
    }

    @Override
    public void deleteWord(Long id) {
        this.removeById(id);
        rebuildDfa();
    }

    @Override
    public List<String> checkText(String text) {
        return dfaFilter.check(text);
    }

    @Override
    public String filterText(String text) {
        return dfaFilter.filter(text, '*');
    }

    private void rebuildDfa() {
        try {
            List<SensitiveWord> words = this.list(new LambdaQueryWrapper<SensitiveWord>()
                    .eq(SensitiveWord::getStatus, (byte) 1));
            List<String> wordList = words.stream()
                    .map(SensitiveWord::getWord)
                    .collect(Collectors.toList());
            dfaFilter.init(wordList);
            log.info("[敏感词] DFA词库重建完成, 词条数: {}", wordList.size());
        } catch (Exception e) {
            log.error("[敏感词] DFA词库重建失败", e);
        }
    }
}
