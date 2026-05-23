<script lang="ts" setup>
import {ref} from 'vue';

import {useVbenModal} from '@vben/common-ui';

import {Form, FormItem, Input, message, RadioGroup, RadioButton} from 'ant-design-vue';

import {createNotice, updateNotice} from '#/api/system/notice';

defineOptions({ name: 'NoticeFormModal' });

const emit = defineEmits<{ success: [] }>();

const formData = ref<Record<string, any>>({});
const mode = ref<'add' | 'edit'>('add');
const formRef = ref();

const [Modal, modalApi] = useVbenModal({
  async onOpenChange(isOpen) {
    if (!isOpen) return;
    const data = modalApi.getData<{
      mode: 'add' | 'edit';
      record?: Record<string, any>;
    }>();
    mode.value = data.mode;
    modalApi.setState({
      title: data.mode === 'add' ? '新增公告' : '编辑公告',
    });
    if (data.mode === 'edit' && data.record) {
      formData.value = { ...data.record };
    } else {
      formData.value = { noticeType: 1 };
    }
  },
  async onConfirm() {
    try {
      await formRef.value?.validate();
    } catch {
      return false;
    }
    if (mode.value === 'add') {
      await createNotice(formData.value);
      message.success('新增成功');
    } else {
      await updateNotice(formData.value.id, formData.value);
      message.success('编辑成功');
    }
    emit('success');
    modalApi.close();
  },
  title: '新增公告',
});
</script>

<template>
  <Modal>
    <Form
      ref="formRef"
      :label-col="{ span: 4 }"
      :model="formData"
      :rules="{
        title: [
          { message: '请输入公告标题', required: true, trigger: 'blur' },
        ],
        content: [
          { message: '请输入公告内容', required: true, trigger: 'blur' },
        ],
      }"
      :wrapper-col="{ span: 19 }"
    >
      <FormItem label="公告类型">
        <RadioGroup v-model:value="formData.noticeType">
          <RadioButton :value="1">通知</RadioButton>
          <RadioButton :value="2">公告</RadioButton>
        </RadioGroup>
      </FormItem>
      <FormItem label="标题" name="title">
        <Input
          v-model:value="formData.title"
          placeholder="请输入公告标题"
        />
      </FormItem>
      <FormItem label="内容" name="content">
        <Input.TextArea
          v-model:value="formData.content"
          :rows="6"
          placeholder="请输入公告内容"
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
