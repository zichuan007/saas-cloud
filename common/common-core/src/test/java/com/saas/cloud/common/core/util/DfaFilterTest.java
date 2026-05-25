package com.saas.cloud.common.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
class DfaFilterTest {

    private DfaFilter filter;

    @BeforeEach
    void setUp() {
        filter = new DfaFilter();
        filter.init(Arrays.asList("敏感词", "违禁内容", "测试"));
    }

    @Test
    void shouldDetectSensitiveWord() {
        List<String> found = filter.check("这里有敏感词出现");
        assertThat(found).containsExactly("敏感词");
    }

    @Test
    void shouldReturnEmptyWhenNoMatch() {
        List<String> found = filter.check("这是一段正常文本");
        assertThat(found).isEmpty();
    }

    @Test
    void shouldDetectMultipleWords() {
        List<String> found = filter.check("敏感词和违禁内容都在这里");
        assertThat(found).containsExactly("敏感词", "违禁内容");
    }

    @Test
    void shouldFilterAndReplace() {
        String result = filter.filter("这里有敏感词出现", '*');
        assertThat(result).isEqualTo("这里有***出现");
    }

    @Test
    void shouldHandleNullAndEmptyInput() {
        assertThat(filter.check(null)).isEmpty();
        assertThat(filter.check("")).isEmpty();
        assertThat(filter.filter(null, '*')).isNull();
        assertThat(filter.filter("", '*')).isEmpty();
    }

    @Test
    void shouldHandleEmptyWordList() {
        DfaFilter emptyFilter = new DfaFilter();
        emptyFilter.init(Collections.emptyList());
        assertThat(emptyFilter.check("任何内容")).isEmpty();
    }
}
