<script lang="ts" setup>
import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { Form, FormItem, Input, InputNumber, message } from 'ant-design-vue';

import {
  createPackage,
  getPackageDetail,
  type PackageRecord,
  updatePackage,
} from '#/api/platform/package';

defineOptions({ name: 'PackageFormModal' });

const emit = defineEmits<{ success: [] }>();

const formData = ref<Record<string, any>>({});
const mode = ref<'add' | 'edit'>('add');

const [Modal, modalApi] = useVbenModal({
  async onOpenChange(isOpen) {
    if (!isOpen) return;
    const data = modalApi.getData<{ id?: number; mode: 'add' | 'edit' }>();
    mode.value = data.mode;
    modalApi.setState({ title: data.mode === 'add' ? '新建套餐' : '编辑套餐' });
    if (data.mode === 'edit' && data.id) {
      const detail = await getPackageDetail(data.id);
      formData.value = { ...(detail as PackageRecord) };
    } else {
      formData.value = {
        maxDeptCount: 10,
        maxProcessDefinition: 5,
        maxRoleCount: 10,
        maxUserCount: 50,
        maxWechatAccount: 1,
        status: 1,
      };
    }
  },
  async onConfirm() {
    if (mode.value === 'add') {
      await createPackage(formData.value);
      message.success('新增成功');
    } else {
      await updatePackage(formData.value.id, formData.value);
      message.success('编辑成功');
    }
    emit('success');
    modalApi.close();
  },
  title: '新建套餐',
});
</script>

<template>
  <Modal>
    <Form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
      <FormItem label="套餐名称" required>
        <Input
          v-model:value="formData.packageName"
          placeholder="如: 基础版, 专业版"
        />
      </FormItem>
      <FormItem label="用户上限">
        <InputNumber v-model:value="formData.maxUserCount" :min="1" />
      </FormItem>
      <FormItem label="角色上限">
        <InputNumber v-model:value="formData.maxRoleCount" :min="1" />
      </FormItem>
      <FormItem label="部门上限">
        <InputNumber v-model:value="formData.maxDeptCount" :min="1" />
      </FormItem>
      <FormItem label="公众号上限">
        <InputNumber v-model:value="formData.maxWechatAccount" :min="0" />
      </FormItem>
      <FormItem label="流程定义上限">
        <InputNumber v-model:value="formData.maxProcessDefinition" :min="0" />
      </FormItem>
      <FormItem label="描述">
        <Input.TextArea
          v-model:value="formData.description"
          placeholder="套餐描述"
          :rows="3"
        />
      </FormItem>
    </Form>
  </Modal>
</template>
