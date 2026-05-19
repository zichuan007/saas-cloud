<script lang="ts" setup>
import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import {
  Form,
  FormItem,
  Input,
  InputNumber,
  message,
  TreeSelect,
} from 'ant-design-vue';

import {
  createDept,
  type DeptRecord,
  getDeptDetail,
  getDeptTree,
  updateDept,
} from '#/api/system/dept';

defineOptions({ name: 'DeptFormModal' });

const emit = defineEmits<{ success: [] }>();

const formData = ref<Record<string, any>>({});
const mode = ref<'add' | 'edit'>('add');
const deptTreeData = ref<any[]>([]);
const formRef = ref();

const rules = {
  deptName: [{ message: '请输入部门名称', required: true, trigger: 'blur' }],
  parentId: [
    { message: '请选择上级部门', required: true, trigger: 'change' },
  ],
};

function convertTree(list: DeptRecord[]): any[] {
  return list.map((item) => ({
    children: item.children ? convertTree(item.children) : [],
    title: item.deptName,
    value: item.id,
  }));
}

const [Modal, modalApi] = useVbenModal({
  async onOpenChange(isOpen) {
    if (!isOpen) return;
    const data =
      modalApi.getData<{ id?: number; mode: 'add' | 'edit'; parentId?: number }>();
    mode.value = data.mode;
    modalApi.setState({ title: data.mode === 'add' ? '新增部门' : '编辑部门' });

    const tree = await getDeptTree();
    deptTreeData.value = [
      { children: convertTree(tree as DeptRecord[]), title: '顶级部门', value: 0 },
    ];

    if (data.mode === 'edit' && data.id) {
      const detail = await getDeptDetail(data.id);
      formData.value = { ...(detail as DeptRecord) };
    } else {
      formData.value = { parentId: data.parentId ?? 0, sortOrder: 0 };
    }
  },
  async onConfirm() {
    try {
      await formRef.value?.validate();
    } catch {
      return false;
    }
    if (mode.value === 'add') {
      await createDept(formData.value);
      message.success('新增成功');
    } else {
      await updateDept(formData.value.id, formData.value);
      message.success('编辑成功');
    }
    emit('success');
    modalApi.close();
  },
  title: '新增部门',
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
      <FormItem label="上级部门" name="parentId">
        <TreeSelect
          v-model:value="formData.parentId"
          :tree-data="deptTreeData"
          placeholder="请选择上级部门"
          tree-default-expand-all
        />
      </FormItem>
      <FormItem label="部门名称" name="deptName">
        <Input v-model:value="formData.deptName" placeholder="请输入部门名称" />
      </FormItem>
      <FormItem label="负责人">
        <Input
          v-model:value="formData.leaderUserName"
          placeholder="请输入负责人姓名"
        />
      </FormItem>
      <FormItem label="排序">
        <InputNumber v-model:value="formData.sortOrder" :min="0" />
      </FormItem>
    </Form>
  </Modal>
</template>
