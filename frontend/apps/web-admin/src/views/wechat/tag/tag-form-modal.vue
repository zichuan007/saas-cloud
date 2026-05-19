<script lang="ts" setup>
import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { Form, FormItem, Input, message } from 'ant-design-vue';

import { createTag, updateTag } from '#/api/wechat/tag';

defineOptions({ name: 'TagFormModal' });

const emit = defineEmits<{ success: [] }>();

const formData = ref<{ accountId?: number; id?: number; tagName: string }>({
  tagName: '',
});
const mode = ref<'add' | 'edit'>('add');
const formRef = ref();

const rules = {
  tagName: [{ message: '请输入标签名称', required: true, trigger: 'blur' }],
};

const [Modal, modalApi] = useVbenModal({
  onOpenChange(isOpen) {
    if (!isOpen) return;
    const data =
      modalApi.getData<{
        accountId?: number;
        id?: number;
        mode: 'add' | 'edit';
        tagName?: string;
      }>();
    mode.value = data.mode;
    modalApi.setState({ title: data.mode === 'add' ? '新建标签' : '编辑标签' });
    if (data.mode === 'edit') {
      formData.value = {
        accountId: data.accountId,
        id: data.id,
        tagName: data.tagName ?? '',
      };
    } else {
      formData.value = { accountId: data.accountId, tagName: '' };
    }
  },
  async onConfirm() {
    try {
      await formRef.value?.validate();
    } catch {
      return false;
    }
    if (mode.value === 'add') {
      await createTag({
        accountId: formData.value.accountId!,
        tagName: formData.value.tagName,
      });
      message.success('新增成功');
    } else {
      await updateTag(formData.value.id!, { tagName: formData.value.tagName });
      message.success('编辑成功');
    }
    emit('success');
    modalApi.close();
  },
  title: '新建标签',
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
      <FormItem label="标签名称" name="tagName">
        <Input v-model:value="formData.tagName" placeholder="请输入标签名称" />
      </FormItem>
    </Form>
  </Modal>
</template>
