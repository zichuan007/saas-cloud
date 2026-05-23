<script lang="ts" setup>
import {ref} from 'vue';

import {useVbenModal} from '@vben/common-ui';

import {Form, FormItem, Input, message, Select} from 'ant-design-vue';

import {type AutoReplyRule, createAutoReply, updateAutoReply,} from '#/api/wechat/auto-reply';

defineOptions({ name: 'AutoReplyFormModal' });

const emit = defineEmits<{ success: [] }>();

const formData = ref<Record<string, any>>({});
const mode = ref<'add' | 'edit'>('add');
const formRef = ref();

const rules = {
  ruleName: [{ message: '请输入规则名称', required: true, trigger: 'blur' }],
  replyContent: [
    { message: '请输入回复内容', required: true, trigger: 'blur' },
  ],
};

const [Modal, modalApi] = useVbenModal({
  onOpenChange(isOpen) {
    if (!isOpen) return;
    const data =
      modalApi.getData<{
        accountId?: number;
        id?: number;
        mode: 'add' | 'edit';
        record?: AutoReplyRule;
      }>();
    mode.value = data.mode;
    modalApi.setState({ title: data.mode === 'add' ? '新建规则' : '编辑规则' });
    if (data.mode === 'edit' && data.record) {
      formData.value = { ...data.record, accountId: data.accountId };
    } else {
      formData.value = {
        accountId: data.accountId,
        matchType: 0,
        replyType: 0,
        ruleType: 1,
      };
    }
  },
  async onConfirm() {
    try {
      await formRef.value?.validate();
    } catch {
      return false;
    }
    if (mode.value === 'add') {
      await createAutoReply(formData.value);
      message.success('新增成功');
    } else {
      await updateAutoReply(formData.value.id, formData.value);
      message.success('编辑成功');
    }
    emit('success');
    modalApi.close();
  },
  title: '新建规则',
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
      <FormItem label="规则名称" name="ruleName">
        <Input v-model:value="formData.ruleName" placeholder="请输入规则名称" />
      </FormItem>
      <FormItem label="类型">
        <Select
          v-model:value="formData.ruleType"
          :options="[
            { label: '关键词回复', value: 1 },
            { label: '关注回复', value: 0 },
            { label: '默认回复', value: 2 },
          ]"
        />
      </FormItem>
      <FormItem v-if="formData.ruleType === 1" label="关键词" name="keyword">
        <Input v-model:value="formData.keyword" placeholder="请输入触发关键词" />
      </FormItem>
      <FormItem v-if="formData.ruleType === 1" label="匹配方式">
        <Select
          v-model:value="formData.matchType"
          :options="[
            { label: '精确匹配', value: 0 },
            { label: '模糊匹配', value: 1 },
          ]"
        />
      </FormItem>
      <FormItem label="回复内容" name="replyContent">
        <Input.TextArea
          v-model:value="formData.replyContent"
          placeholder="请输入回复内容"
          :rows="4"
        />
      </FormItem>
    </Form>
  </Modal>
</template>
