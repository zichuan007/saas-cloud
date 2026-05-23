<script lang="ts" setup>
import {ref} from 'vue';

import {useVbenModal} from '@vben/common-ui';

import {Form, FormItem, Input, message} from 'ant-design-vue';

import {
  createDefinition,
  getDefinitionDetail,
  type ProcessDefinition,
  updateDefinition,
} from '#/api/workflow/definition';

defineOptions({ name: 'DefinitionFormModal' });

const emit = defineEmits<{ success: [] }>();

const formData = ref<Record<string, any>>({});
const mode = ref<'add' | 'edit'>('add');
const formRef = ref();

const rules = {
  processName: [
    { message: '请输入流程名称', required: true, trigger: 'blur' },
  ],
  processKey: [
    { message: '请输入流程标识', required: true, trigger: 'blur' },
  ],
};

const [Modal, modalApi] = useVbenModal({
  async onOpenChange(isOpen) {
    if (!isOpen) return;
    const data = modalApi.getData<{ id?: number; mode: 'add' | 'edit' }>();
    mode.value = data.mode;
    modalApi.setState({ title: data.mode === 'add' ? '新建流程定义' : '编辑流程定义' });
    if (data.mode === 'edit' && data.id) {
      const detail = await getDefinitionDetail(data.id);
      formData.value = { ...(detail as ProcessDefinition) };
    } else {
      formData.value = {};
    }
  },
  async onConfirm() {
    try {
      await formRef.value?.validate();
    } catch {
      return false;
    }
    if (mode.value === 'add') {
      await createDefinition(formData.value);
      message.success('新增成功');
    } else {
      await updateDefinition(formData.value.id, formData.value);
      message.success('编辑成功');
    }
    emit('success');
    modalApi.close();
  },
  title: '新建流程定义',
});
</script>

<template>
  <Modal>
    <Form
      ref="formRef"
      :label-col="{ span: 5 }"
      :model="formData"
      :rules="rules"
      :wrapper-col="{ span: 18 }"
    >
      <FormItem label="流程名称" name="processName">
        <Input
          v-model:value="formData.processName"
          placeholder="请输入流程名称"
        />
      </FormItem>
      <FormItem label="流程标识" name="processKey">
        <Input
          v-model:value="formData.processKey"
          :disabled="mode === 'edit'"
          placeholder="如: leave-apply"
        />
      </FormItem>
      <FormItem label="备注">
        <Input.TextArea
          v-model:value="formData.remark"
          placeholder="请输入备注"
          :rows="3"
        />
      </FormItem>
    </Form>
  </Modal>
</template>
