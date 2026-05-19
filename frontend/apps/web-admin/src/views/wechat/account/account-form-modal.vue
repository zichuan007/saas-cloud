<script lang="ts" setup>
import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { Form, FormItem, Input, message, Select } from 'ant-design-vue';

import {
  createAccount,
  getAccountDetail,
  updateAccount,
  type WechatAccount,
} from '#/api/wechat/account';

defineOptions({ name: 'AccountFormModal' });

const emit = defineEmits<{ success: [] }>();

const formData = ref<Record<string, any>>({});
const mode = ref<'add' | 'edit'>('add');
const formRef = ref();

const rules = {
  accountName: [
    { message: '请输入公众号名称', required: true, trigger: 'blur' },
  ],
  appId: [{ message: '请输入AppID', required: true, trigger: 'blur' }],
  appSecret: [
    { message: '请输入AppSecret', required: true, trigger: 'blur' },
  ],
};

const [Modal, modalApi] = useVbenModal({
  async onOpenChange(isOpen) {
    if (!isOpen) return;
    const data = modalApi.getData<{ id?: number; mode: 'add' | 'edit' }>();
    mode.value = data.mode;
    modalApi.setState({ title: data.mode === 'add' ? '绑定公众号' : '编辑公众号' });
    if (data.mode === 'edit' && data.id) {
      const detail = await getAccountDetail(data.id);
      formData.value = { ...(detail as WechatAccount) };
    } else {
      formData.value = { accountType: 1 };
    }
  },
  async onConfirm() {
    try {
      await formRef.value?.validate();
    } catch {
      return false;
    }
    if (mode.value === 'add') {
      await createAccount(formData.value);
      message.success('绑定成功');
    } else {
      await updateAccount(formData.value.id, formData.value);
      message.success('编辑成功');
    }
    emit('success');
    modalApi.close();
  },
  title: '绑定公众号',
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
      <FormItem label="公众号名称" name="accountName">
        <Input v-model:value="formData.accountName" placeholder="请输入公众号名称" />
      </FormItem>
      <FormItem label="AppID" name="appId">
        <Input
          v-model:value="formData.appId"
          :disabled="mode === 'edit'"
          placeholder="请输入AppID"
        />
      </FormItem>
      <FormItem label="AppSecret" name="appSecret">
        <Input.Password
          v-model:value="formData.appSecret"
          placeholder="请输入AppSecret"
        />
      </FormItem>
      <FormItem label="Token">
        <Input
          v-model:value="formData.token"
          placeholder="微信服务器验证Token"
        />
      </FormItem>
      <FormItem label="类型">
        <Select
          v-model:value="formData.accountType"
          :options="[
            { label: '服务号', value: 1 },
            { label: '订阅号', value: 0 },
          ]"
        />
      </FormItem>
    </Form>
  </Modal>
</template>
