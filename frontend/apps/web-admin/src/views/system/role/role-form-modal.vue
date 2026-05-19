<script lang="ts" setup>
import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { Form, FormItem, Input, InputNumber, message } from 'ant-design-vue';

import {
  createRole,
  getRoleDetail,
  type RoleRecord,
  updateRole,
} from '#/api/system/role';

defineOptions({ name: 'RoleFormModal' });

const emit = defineEmits<{ success: [] }>();

const formData = ref<Record<string, any>>({});
const mode = ref<'add' | 'edit'>('add');
const formRef = ref();

const rules = {
  roleName: [{ message: '请输入角色名称', required: true, trigger: 'blur' }],
  roleCode: [{ message: '请输入角色标识', required: true, trigger: 'blur' }],
};

const [Modal, modalApi] = useVbenModal({
  async onOpenChange(isOpen) {
    if (!isOpen) return;
    const data = modalApi.getData<{ id?: number; mode: 'add' | 'edit' }>();
    mode.value = data.mode;
    modalApi.setState({ title: data.mode === 'add' ? '新增角色' : '编辑角色' });
    if (data.mode === 'edit' && data.id) {
      const detail = await getRoleDetail(data.id);
      formData.value = { ...(detail as RoleRecord) };
    } else {
      formData.value = { sortOrder: 0, status: 1 };
    }
  },
  async onConfirm() {
    try {
      await formRef.value?.validate();
    } catch {
      return false;
    }
    if (mode.value === 'add') {
      await createRole(formData.value);
      message.success('新增成功');
    } else {
      await updateRole(formData.value.id, formData.value);
      message.success('编辑成功');
    }
    emit('success');
    modalApi.close();
  },
  title: '新增角色',
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
      <FormItem label="角色名称" name="roleName">
        <Input v-model:value="formData.roleName" placeholder="请输入角色名称" />
      </FormItem>
      <FormItem label="角色标识" name="roleCode">
        <Input
          v-model:value="formData.roleCode"
          :disabled="mode === 'edit'"
          placeholder="如: admin, editor"
        />
      </FormItem>
      <FormItem label="排序">
        <InputNumber v-model:value="formData.sortOrder" :min="0" />
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
