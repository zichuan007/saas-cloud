<script lang="ts" setup>
import {computed, ref} from 'vue';

import {useVbenModal} from '@vben/common-ui';

import {Form, FormItem, Input, message, Select} from 'ant-design-vue';

import {createUser, getUserDetail, updateUser} from '#/api/system';
import {getDeptTree} from '#/api/system/dept';
import {getRoleList} from '#/api/system/role';

defineOptions({ name: 'UserFormModal' });

const emit = defineEmits<{ success: [] }>();

const formData = ref<Record<string, any>>({});
const mode = ref<'add' | 'edit'>('add');
const deptOptions = ref<any[]>([]);
const roleOptions = ref<any[]>([]);
const formRef = ref();

const rules = computed(() => ({
  username: [{ message: '请输入用户名', required: true, trigger: 'blur' }],
  realName: [{ message: '请输入姓名', required: true, trigger: 'blur' }],
  ...(mode.value === 'add'
    ? { password: [{ message: '请输入初始密码', required: true, trigger: 'blur' }] }
    : {}),
}));

function flattenDeptTree(tree: any[], result: any[] = [], prefix = '') {
  for (const item of tree) {
    result.push({
      label: prefix + item.deptName,
      value: item.id,
    });
    if (item.children?.length) {
      flattenDeptTree(item.children, result, `${prefix}${item.deptName} / `);
    }
  }
  return result;
}

const [Modal, modalApi] = useVbenModal({
  async onOpenChange(isOpen) {
    if (!isOpen) return;

    const data = modalApi.getData<{ id?: number; mode: 'add' | 'edit' }>();
    mode.value = data.mode;

    const [deptTree, roles] = await Promise.all([
      getDeptTree(),
      getRoleList(),
    ]);
    deptOptions.value = flattenDeptTree(deptTree as any[]);
    roleOptions.value = (roles as any[]).map((r: any) => ({
      label: r.roleName,
      value: r.id,
    }));

    modalApi.setState({ title: data.mode === 'add' ? '新增用户' : '编辑用户' });
    if (data.mode === 'edit' && data.id) {
      const detail = await getUserDetail(data.id);
      formData.value = { ...detail };
    } else {
      formData.value = { status: 1 };
    }
  },
  async onConfirm() {
    try {
      await formRef.value?.validate();
    } catch {
      return false;
    }
    if (mode.value === 'add') {
      await createUser(formData.value as any);
      message.success('新增成功');
    } else {
      await updateUser(formData.value.id, formData.value);
      message.success('编辑成功');
    }
    emit('success');
    modalApi.close();
  },
  title: '新增用户',
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
      <FormItem label="用户名" name="username">
        <Input
          v-model:value="formData.username"
          :disabled="mode === 'edit'"
          placeholder="请输入用户名"
        />
      </FormItem>
      <FormItem v-if="mode === 'add'" label="密码" name="password">
        <Input.Password
          v-model:value="formData.password"
          placeholder="请输入初始密码"
        />
      </FormItem>
      <FormItem label="姓名" name="realName">
        <Input v-model:value="formData.realName" placeholder="请输入姓名" />
      </FormItem>
      <FormItem label="手机号">
        <Input v-model:value="formData.phone" placeholder="请输入手机号" />
      </FormItem>
      <FormItem label="部门">
        <Select
          v-model:value="formData.deptId"
          :options="deptOptions"
          placeholder="请选择部门"
        />
      </FormItem>
      <FormItem label="角色">
        <Select
          v-model:value="formData.roleIds"
          :options="roleOptions"
          mode="multiple"
          placeholder="请选择角色"
        />
      </FormItem>
    </Form>
  </Modal>
</template>
