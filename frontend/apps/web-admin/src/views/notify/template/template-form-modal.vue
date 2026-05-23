<script lang="ts" setup>
import {ref} from 'vue';

import {useVbenModal} from '@vben/common-ui';

import {Form, FormItem, Input, message, Select} from 'ant-design-vue';

import {requestClient} from '#/api/request';

defineOptions({ name: 'TemplateFormModal' });

const emit = defineEmits<{ success: [] }>();

const formData = ref<Record<string, any>>({});
const mode = ref<'add' | 'edit'>('add');
const formRef = ref();

const rules = {
  templateCode: [
    { message: '请输入模板编码', required: true, trigger: 'blur' },
  ],
  templateName: [
    { message: '请输入模板名称', required: true, trigger: 'blur' },
  ],
  type: [{ message: '请选择渠道', required: true, trigger: 'change' }],
  titleTemplate: [
    { message: '请输入标题模板', required: true, trigger: 'blur' },
  ],
  contentTemplate: [
    { message: '请输入内容模板', required: true, trigger: 'blur' },
  ],
};

const [Modal, modalApi] = useVbenModal({
  onOpenChange(isOpen) {
    if (!isOpen) return;
    const data = modalApi.getData<{ mode: 'add' | 'edit'; record?: any }>();
    mode.value = data.mode;
    modalApi.setState({
      title: data.mode === 'add' ? '新建模板' : '编辑模板',
    });
    if (data.mode === 'edit' && data.record) {
      formData.value = { ...data.record };
    } else {
      formData.value = { type: 0, status: 1 };
    }
  },
  async onConfirm() {
    try {
      await formRef.value?.validate();
    } catch {
      return false;
    }
    if (mode.value === 'add') {
      await requestClient.post('/notify/template', formData.value);
      message.success('新增成功');
    } else {
      await requestClient.put(
        `/notify/template/${formData.value.id}`,
        formData.value,
      );
      message.success('编辑成功');
    }
    emit('success');
    modalApi.close();
  },
  title: '新建模板',
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
      <FormItem label="模板编码" name="templateCode">
        <Input
          v-model:value="formData.templateCode"
          :disabled="mode === 'edit'"
          placeholder="请输入模板编码"
        />
      </FormItem>
      <FormItem label="模板名称" name="templateName">
        <Input
          v-model:value="formData.templateName"
          placeholder="请输入模板名称"
        />
      </FormItem>
      <FormItem label="渠道" name="type">
        <Select
          v-model:value="formData.type"
          :options="[
            { label: '站内信', value: 0 },
            { label: '邮件', value: 1 },
            { label: 'IM Webhook', value: 2 },
          ]"
        />
      </FormItem>
      <FormItem label="标题模板" name="titleTemplate">
        <Input
          v-model:value="formData.titleTemplate"
          placeholder="请输入标题模板"
        />
      </FormItem>
      <FormItem label="内容模板" name="contentTemplate">
        <Input.TextArea
          v-model:value="formData.contentTemplate"
          :rows="4"
          placeholder="请输入内容模板"
        />
      </FormItem>
    </Form>
  </Modal>
</template>
