<script lang="ts" setup>
import {ref} from 'vue';

import {useVbenModal} from '@vben/common-ui';

import {Form, FormItem, Input, message} from 'ant-design-vue';

import {startProcess} from '#/api/workflow/process';

defineOptions({ name: 'StartProcessModal' });

const emit = defineEmits<{ success: [] }>();

const processKey = ref('');
const processName = ref('');
const formData = ref<Record<string, any>>({
  reason: '',
  title: '',
});
const formRef = ref();

const rules = {
  title: [{ message: '请输入流程标题', required: true, trigger: 'blur' }],
};

const [Modal, modalApi] = useVbenModal({
  onOpenChange(isOpen) {
    if (!isOpen) return;
    const data = modalApi.getData<{
      processKey: string;
      processName: string;
    }>();
    processKey.value = data.processKey;
    processName.value = data.processName;
    modalApi.setState({ title: `发起 - ${data.processName}` });
    formData.value = { reason: '', title: '' };
  },
  async onConfirm() {
    try {
      await formRef.value?.validate();
    } catch {
      return false;
    }
    await startProcess({
      formData: { reason: formData.value.reason },
      processKey: processKey.value,
      title: formData.value.title,
    });
    message.success('流程发起成功');
    emit('success');
    modalApi.close();
  },
  title: '发起流程',
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
      <FormItem label="流程标题" name="title">
        <Input v-model:value="formData.title" placeholder="请输入流程标题" />
      </FormItem>
      <FormItem label="申请事由">
        <Input.TextArea
          v-model:value="formData.reason"
          placeholder="请输入申请事由"
          :rows="4"
        />
      </FormItem>
    </Form>
  </Modal>
</template>
