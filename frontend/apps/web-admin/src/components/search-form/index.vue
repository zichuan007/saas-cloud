<script lang="ts" setup>
import { reactive, ref } from 'vue';

import { Button, Col, DatePicker, Form, FormItem, Input, Row, Select } from 'ant-design-vue';
import { DownOutlined, UpOutlined } from '@ant-design/icons-vue';

import DictSelect from '#/components/dict-select/index.vue';

export interface SearchField {
  /** 字段名 */
  field: string;
  /** 显示标签 */
  label: string;
  /** 组件类型 */
  type: 'date' | 'dict' | 'input' | 'select';
  /** type=dict 时的字典编码 */
  dictType?: string;
  /** type=select 时的选项 */
  options?: { label: string; value: number | string }[];
  /** 占位文字 */
  placeholder?: string;
}

interface Props {
  fields: SearchField[];
  /** 默认展示字段数（超出折叠） */
  visibleCount?: number;
}

const props = withDefaults(defineProps<Props>(), {
  visibleCount: 3,
});

const emit = defineEmits<{
  search: [values: Record<string, any>];
}>();

const formData = reactive<Record<string, any>>({});
const expanded = ref(false);

const needCollapse = props.fields.length > props.visibleCount;

function handleSearch() {
  emit('search', { ...formData });
}

function handleReset() {
  for (const key of Object.keys(formData)) {
    formData[key] = undefined;
  }
  emit('search', {});
}
</script>

<template>
  <Form :model="formData" layout="inline" style="margin-bottom: 16px">
    <Row :gutter="16" style="width: 100%">
      <template v-for="(item, idx) in fields" :key="item.field">
        <Col
          v-show="expanded || idx < visibleCount"
          :lg="6"
          :md="8"
          :sm="12"
          :xs="24"
        >
          <FormItem :label="item.label" style="width: 100%">
            <Input
              v-if="item.type === 'input'"
              v-model:value="formData[item.field]"
              :placeholder="item.placeholder || `请输入${item.label}`"
              allow-clear
              @press-enter="handleSearch"
            />
            <Select
              v-else-if="item.type === 'select'"
              v-model:value="formData[item.field]"
              :options="item.options"
              :placeholder="item.placeholder || `请选择${item.label}`"
              allow-clear
            />
            <DictSelect
              v-else-if="item.type === 'dict'"
              v-model:value="formData[item.field]"
              :dict-type="item.dictType || ''"
              :placeholder="item.placeholder || `请选择${item.label}`"
            />
            <DatePicker
              v-else-if="item.type === 'date'"
              v-model:value="formData[item.field]"
              :placeholder="item.placeholder || `请选择${item.label}`"
              style="width: 100%"
            />
          </FormItem>
        </Col>
      </template>
      <Col :lg="6" :md="8" :sm="12" :xs="24">
        <FormItem>
          <Button type="primary" @click="handleSearch">搜索</Button>
          <Button class="ml-2" @click="handleReset">重置</Button>
          <Button
            v-if="needCollapse"
            class="ml-2"
            type="link"
            @click="expanded = !expanded"
          >
            {{ expanded ? '收起' : '展开' }}
            <UpOutlined v-if="expanded" />
            <DownOutlined v-else />
          </Button>
        </FormItem>
      </Col>
    </Row>
  </Form>
</template>
