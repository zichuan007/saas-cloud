package com.saas.cloud.common.log.diff;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
class DiffUtilTest {

    static class SampleEntity {
        @DiffField("名称")
        private String name;
        @DiffField("状态")
        private Integer status;
        private String ignoredField;

        SampleEntity(String name, Integer status, String ignoredField) {
            this.name = name;
            this.status = status;
            this.ignoredField = ignoredField;
        }
    }

    @Test
    void shouldReturnNullWhenBothObjectsAreEqual() {
        SampleEntity a = new SampleEntity("张三", 1, "x");
        SampleEntity b = new SampleEntity("张三", 1, "y");
        assertThat(DiffUtil.diff(a, b)).isNull();
    }

    @Test
    void shouldDetectFieldChanges() {
        SampleEntity before = new SampleEntity("张三", 1, "x");
        SampleEntity after = new SampleEntity("李四", 0, "x");
        String result = DiffUtil.diff(before, after);
        assertThat(result).contains("名称: 张三 → 李四");
        assertThat(result).contains("状态: 1 → 0");
    }

    @Test
    void shouldReturnNullWhenEitherObjectIsNull() {
        SampleEntity a = new SampleEntity("张三", 1, "x");
        assertThat(DiffUtil.diff(null, a)).isNull();
        assertThat(DiffUtil.diff(a, null)).isNull();
    }

    @Test
    void shouldHandleNullFieldValues() {
        SampleEntity before = new SampleEntity(null, 1, "x");
        SampleEntity after = new SampleEntity("李四", 1, "x");
        String result = DiffUtil.diff(before, after);
        assertThat(result).contains("名称: 空 → 李四");
    }

    @Test
    void shouldIgnoreFieldsWithoutAnnotation() {
        SampleEntity before = new SampleEntity("张三", 1, "old");
        SampleEntity after = new SampleEntity("张三", 1, "new");
        assertThat(DiffUtil.diff(before, after)).isNull();
    }
}
