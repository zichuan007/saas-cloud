<script lang="ts" setup>
import {ref} from 'vue';

import {Page, useVbenModal} from '@vben/common-ui';

import {Button, message, Modal, Popconfirm, Space, Switch, Table, Tree,} from 'ant-design-vue';

import {
  assignRoleMenus,
  deleteRole,
  exportRoles,
  getRoleDetail,
  getRoleList,
  type RoleRecord,
  updateRoleStatus,
} from '#/api/system/role';
import {getMenuTree} from '#/api/system/menu';

import RoleFormModal from './role-form-modal.vue';

defineOptions({ name: 'SystemRole' });

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: RoleFormModal,
});

const roleList = ref<RoleRecord[]>([]);
const loading = ref(false);

async function loadRoles() {
  loading.value = true;
  try {
    roleList.value = (await getRoleList()) as RoleRecord[];
  } finally {
    loading.value = false;
  }
}
loadRoles();

function handleAdd() {
  formModalApi.setData({ mode: 'add' });
  formModalApi.open();
}

function handleEdit(row: RoleRecord) {
  formModalApi.setData({ id: row.id, mode: 'edit' });
  formModalApi.open();
}

async function handleDelete(id: number) {
  await deleteRole(id);
  message.success('删除成功');
  await loadRoles();
}

async function handleStatusChange(row: RoleRecord, checked: boolean) {
  await updateRoleStatus(row.id, checked ? 1 : 0);
  message.success(checked ? '已启用' : '已禁用');
  await loadRoles();
}

const menuTreeVisible = ref(false);
const menuTreeData = ref<any[]>([]);
const checkedMenuIds = ref<number[]>([]);
const currentRoleId = ref<number>(0);

function convertMenuTree(menus: any[]): any[] {
  return menus.map((m) => ({
    children: m.children ? convertMenuTree(m.children) : [],
    key: m.id,
    title: m.name ?? m.menuName,
  }));
}

async function handleAssignMenu(row: RoleRecord) {
  currentRoleId.value = row.id;
  const [menuTree, detail] = await Promise.all([
    getMenuTree(),
    getRoleDetail(row.id),
  ]);
  menuTreeData.value = convertMenuTree(menuTree as any[]);
  checkedMenuIds.value = (detail as RoleRecord).menuIds ?? [];
  menuTreeVisible.value = true;
}

async function handleSaveMenus() {
  await assignRoleMenus(currentRoleId.value, checkedMenuIds.value);
  message.success('菜单分配成功');
  menuTreeVisible.value = false;
}

async function handleExport() {
  try {
    const res = await exportRoles();
    const blob = new Blob([res as any], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = '角色列表.xlsx';
    link.click();
    window.URL.revokeObjectURL(url);
    message.success('导出成功');
  } catch {
    message.error('导出失败');
  }
}
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="loadRoles" />
    <div class="p-4">
      <div class="mb-4 flex justify-between">
        <h3 class="text-lg font-medium">角色管理</h3>
        <Button type="primary" @click="handleAdd">新增角色</Button>
        <Button class="ml-2" @click="handleExport">导出</Button>
      </div>
      <Table
        :columns="[
          { title: '角色名称', dataIndex: 'roleName', key: 'roleName' },
          { title: '角色标识', dataIndex: 'roleCode', key: 'roleCode' },
          { title: '排序', dataIndex: 'sortOrder', key: 'sortOrder', width: 80 },
          { title: '状态', key: 'status', width: 100 },
          { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
          { title: '操作', key: 'action', width: 240, fixed: 'right' },
        ]"
        :data-source="roleList"
        :loading="loading"
        :pagination="false"
        row-key="id"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <Switch
              :checked="record.status === 1"
              checked-children="启用"
              un-checked-children="禁用"
              @change="(c: any) => handleStatusChange(record, !!c)"
            />
          </template>
          <template v-if="column.key === 'action'">
            <Space>
              <Button size="small" type="link" @click="handleEdit(record)">
                编辑
              </Button>
              <Button
                size="small"
                type="link"
                @click="handleAssignMenu(record)"
              >
                分配菜单
              </Button>
              <Popconfirm
                title="确定删除该角色？"
                @confirm="handleDelete(record.id)"
              >
                <Button danger size="small" type="link">删除</Button>
              </Popconfirm>
            </Space>
          </template>
        </template>
      </Table>
    </div>
    <Modal
      v-model:open="menuTreeVisible"
      title="分配菜单权限"
      @ok="handleSaveMenus"
    >
      <Tree
        v-model:checkedKeys="checkedMenuIds"
        :tree-data="menuTreeData"
        checkable
        default-expand-all
      />
    </Modal>
  </Page>
</template>
