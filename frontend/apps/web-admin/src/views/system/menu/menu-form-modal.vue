<script lang="ts" setup>
import {computed, ref, watch} from 'vue';

import {useVbenModal} from '@vben/common-ui';

import {
  Form,
  FormItem,
  Input,
  InputNumber,
  message,
  RadioGroup,
  Select,
  Switch,
  TreeSelect,
} from 'ant-design-vue';

import {
  createMenu,
  getMenuTree,
  type MenuRecord,
  updateMenu,
} from '#/api/system/menu';

defineOptions({ name: 'MenuFormModal' });

const emit = defineEmits<{ success: [] }>();

const formData = ref<Record<string, any>>({});
const mode = ref<'add' | 'edit'>('add');
const menuTreeOptions = ref<any[]>([]);
const formRef = ref();

const menuTypeOptions = [
  { label: '目录', value: 0 },
  { label: '菜单', value: 1 },
  { label: '按钮', value: 2 },
];

const moduleOptions = [
  { label: 'RBAC', value: 'RBAC' },
  { label: 'WORKFLOW', value: 'WORKFLOW' },
  { label: 'WECHAT_OA', value: 'WECHAT_OA' },
  { label: 'NOTIFY', value: 'NOTIFY' },
  { label: 'PLATFORM', value: 'PLATFORM' },
];

const isDir = computed(() => formData.value.menuType === 0);
const isMenu = computed(() => formData.value.menuType === 1);
const isButton = computed(() => formData.value.menuType === 2);

const rules = computed(() => ({
  menuName: [{ message: '请输入菜单名称', required: true, trigger: 'blur' }],
  menuType: [{ message: '请选择菜单类型', required: true, trigger: 'change' }],
  ...(isButton.value
    ? { permission: [{ message: '请输入权限标识', required: true, trigger: 'blur' }] }
    : { path: [{ message: '请输入路由路径', required: true, trigger: 'blur' }] }),
}));

function convertTree(list: MenuRecord[]): any[] {
  return list.map((m) => ({
    children: m.children?.length ? convertTree(m.children) : undefined,
    label: m.name ?? m.menuName,
    value: m.id,
  }));
}

const [Modal, modalApi] = useVbenModal({
  async onOpenChange(isOpen) {
    if (!isOpen) return;
    const data = modalApi.getData<{
      id?: number;
      mode: 'add' | 'edit';
      parentId?: number;
      record?: MenuRecord;
    }>();
    mode.value = data.mode;

    const tree = (await getMenuTree()) as MenuRecord[];
    menuTreeOptions.value = [
      { children: convertTree(tree), label: '顶级目录', value: 0 },
    ];

    modalApi.setState({
      title: data.mode === 'add' ? '新增菜单' : '编辑菜单',
    });

    if (data.mode === 'edit' && data.record) {
      const r = { ...data.record };
      if (r.name && !r.menuName) {
        r.menuName = r.name;
      }
      formData.value = r;
    } else {
      formData.value = {
        isCached: 0,
        isExternal: 0,
        menuType: 0,
        module: 'RBAC',
        parentId: data.parentId ?? 0,
        sortOrder: 0,
        visible: 1,
      };
    }
  },
  async onConfirm() {
    try {
      await formRef.value?.validate();
    } catch {
      return false;
    }
    const submitData = { ...formData.value };
    if (mode.value === 'add') {
      await createMenu(submitData);
      message.success('新增成功');
    } else {
      await updateMenu(submitData.id, submitData);
      message.success('编辑成功');
    }
    emit('success');
    modalApi.close();
  },
  title: '新增菜单',
});
</script>

<template>
  <Modal class="w-[600px]">
    <Form
      ref="formRef"
      :label-col="{ span: 5 }"
      :model="formData"
      :rules="rules"
      :wrapper-col="{ span: 18 }"
    >
      <FormItem label="上级菜单" name="parentId">
        <TreeSelect
          v-model:value="formData.parentId"
          :tree-data="menuTreeOptions"
          allow-clear
          placeholder="选择上级菜单"
          tree-default-expand-all
        />
      </FormItem>
      <FormItem label="菜单类型" name="menuType">
        <RadioGroup
          v-model:value="formData.menuType"
          :options="menuTypeOptions"
          option-type="button"
        />
      </FormItem>
      <FormItem label="菜单名称" name="menuName">
        <Input v-model:value="formData.menuName" placeholder="请输入菜单名称" />
      </FormItem>
      <FormItem v-if="!isButton" label="路由路径" name="path">
        <Input v-model:value="formData.path" placeholder="如: /system/menu" />
      </FormItem>
      <FormItem v-if="isMenu" label="组件路径" name="component">
        <Input
          v-model:value="formData.component"
          placeholder="如: /views/system/menu/index"
        />
      </FormItem>
      <FormItem v-if="isMenu || isButton" label="权限标识" name="permission">
        <Input
          v-model:value="formData.permission"
          placeholder="如: system:menu:add"
        />
      </FormItem>
      <FormItem v-if="!isButton" label="图标" name="icon">
        <Input v-model:value="formData.icon" placeholder="图标名称" />
      </FormItem>
      <FormItem label="排序" name="sortOrder">
        <InputNumber v-model:value="formData.sortOrder" :min="0" />
      </FormItem>
      <FormItem v-if="!isButton" label="是否可见">
        <Switch
          :checked="formData.visible === 1"
          checked-children="显示"
          un-checked-children="隐藏"
          @change="(c: any) => (formData.visible = c ? 1 : 0)"
        />
      </FormItem>
      <FormItem v-if="isMenu" label="是否外链">
        <Switch
          :checked="formData.isExternal === 1"
          checked-children="是"
          un-checked-children="否"
          @change="(c: any) => (formData.isExternal = c ? 1 : 0)"
        />
      </FormItem>
      <FormItem v-if="isMenu" label="是否缓存">
        <Switch
          :checked="formData.isCached === 1"
          checked-children="是"
          un-checked-children="否"
          @change="(c: any) => (formData.isCached = c ? 1 : 0)"
        />
      </FormItem>
      <FormItem label="所属模块">
        <Select
          v-model:value="formData.module"
          :options="moduleOptions"
          placeholder="请选择模块"
        />
      </FormItem>
    </Form>
  </Modal>
</template>
