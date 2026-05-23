<script lang="ts" setup>
import {ref} from 'vue';

import {useVbenModal} from '@vben/common-ui';

import {Form, FormItem, Input, InputNumber, message} from 'ant-design-vue';

import {createDictData, updateDictData} from '#/api/system/dict';

defineOptions({ name: 'DictDataModal' });

const emit = defineEmits<{ success: [] }>();

const formData = ref<Record<string, any>>({});
const mode = ref<'add' | 'edit'>('add');
const formRef = ref();

const [Modal, modalApi] = useVbenModal({
  async onOpenChange(isOpen) {
    if (!isOpen) return;
    const data = modalApi.getData<{
      dictType?: string;
      mode: 'add' | 'edit';
      record?: Record<string, any>;
    }>();
    mode.value = data.mode;
    modalApi.setState({
      title: data.mode === 'add' ? '新增字典数据' : '编辑字典数据',
    });
    if (data.mode === 'edit' && data.record) {
      formData.value = { ...data.record };
    } else {
      formData.value = { dictType: data.dictType, sortOrder: 0, status: 1 };
    }
  },
  async onConfirm() {
    try {
      await formRef.value?.validate();
    } catch {
      return false;
    }
    if (mode.value === 'add') {
      await createDictData(formData.value);
      message.success('新增成功');
    } else {
      await updateDictData(formData.value.id, formData.value);
      message.success('编辑成功');
    }
    emit('success');
    modalApi.close();
  },
  title: '新增字典数据',
});
</script>

<template>
  <Modal>
    <Form
      ref="formRef"
      :label-col="{ span: 5 }"
      :model="formData"
      :rules="{
        dictLabel: [
          { message: '请输入字典标签', required: true, trigger: 'blur' },
        ],
        dictValue: [
          { message: '请输入字典值', required: true, trigger: 'blur' },
        ],
      }"
      :wrapper-col="{ span: 18 }"
    >
      <FormItem label="字典标签" name="dictLabel">
        <Input
          v-model:value="formData.dictLabel"
          placeholder="请输入字典标签"
        />
      </FormItem>
      <FormItem label="字典值" name="dictValue">
        <Input
          v-model:value="formData.dictValue"
          placeholder="请输入字典值"
        />
      </FormItem>
      <FormItem label="排序">
        <InputNumber
          v-model:value="formData.sortOrder"
          :min="0"
          style="width: 100%"
        />
      </FormItem>
      <FormItem label="样式">
        <Input
          v-model:value="formData.cssClass"
          placeholder="CSS 样式类名"
        />
      </FormItem>
      <FormItem label="备注">
        <Input.TextArea
          v-model:value="formData.remark"
          :rows="2"
          placeholder="备注"
        />
      </FormItem>
    </Form>
  </Modal>
</template>
