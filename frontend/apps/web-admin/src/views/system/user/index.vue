<script lang="ts" setup>
import type {VxeGridProps} from '#/adapter/vxe-table';
import {useVbenVxeGrid} from '#/adapter/vxe-table';

import {Page, useVbenModal} from '@vben/common-ui';

import {Button, message, Popconfirm, Space, Switch} from 'ant-design-vue';
import {
  deleteUser,
  exportUsers,
  getUserList,
  resetUserPassword,
  updateUserStatus,
} from '#/api/system';

import UserFormModal from './user-form-modal.vue';

defineOptions({ name: 'SystemUser' });

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: UserFormModal,
});

const gridOptions: VxeGridProps = {
  columns: [
    { field: 'username', title: '用户名', minWidth: 120 },
    { field: 'realName', title: '姓名', minWidth: 120 },
    { field: 'phone', title: '手机号', minWidth: 130 },
    { field: 'deptName', title: '部门', minWidth: 120 },
    {
      field: 'status',
      title: '状态',
      width: 100,
      slots: { default: 'status' },
    },
    { field: 'createTime', title: '创建时间', minWidth: 160 },
    {
      field: 'action',
      fixed: 'right',
      title: '操作',
      width: 200,
      slots: { default: 'action' },
    },
  ],
  proxyConfig: {
    ajax: {
      query: async ({ page }) => {
        return await getUserList({
          pageNum: page.currentPage,
          pageSize: page.pageSize,
        });
      },
    },
  },
  pagerConfig: { enabled: true },
  toolbarConfig: { slots: { buttons: 'toolbar-btns' } },
};

const [Grid, gridApi] = useVbenVxeGrid({ gridOptions });

function handleAdd() {
  formModalApi.setData({ mode: 'add' });
  formModalApi.open();
}

function handleEdit(row: any) {
  formModalApi.setData({ id: row.id, mode: 'edit' });
  formModalApi.open();
}

async function handleDelete(id: number) {
  await deleteUser(id);
  message.success('删除成功');
  gridApi.reload();
}

async function handleStatusChange(row: any, checked: boolean) {
  await updateUserStatus(row.id, checked ? 1 : 0);
  message.success(checked ? '已启用' : '已禁用');
  gridApi.reload();
}

async function handleResetPwd(id: number) {
  await resetUserPassword(id, 'Init@1234');
  message.success('密码已重置为 Init@1234');
}

async function handleExport() {
  try {
    const res = await exportUsers();
    const blob = new Blob([res as any], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = '用户列表.xlsx';
    link.click();
    window.URL.revokeObjectURL(url);
    message.success('导出成功');
  } catch {
    message.error('导出失败');
  }
}

function handleFormSuccess() {
  gridApi.reload();
}
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="handleFormSuccess" />
    <Grid>
      <template #toolbar-btns>
        <Button type="primary" @click="handleAdd">新增用户</Button>
        <Button class="ml-2" @click="handleExport">导出</Button>
      </template>
      <template #status="{ row }">
        <Switch
          :checked="row.status === 1"
          checked-children="启用"
          un-checked-children="禁用"
          @change="(checked: any) => handleStatusChange(row, !!checked)"
        />
      </template>
      <template #action="{ row }">
        <Space>
          <Button size="small" type="link" @click="handleEdit(row)">
            编辑
          </Button>
          <Button size="small" type="link" @click="handleResetPwd(row.id)">
            重置密码
          </Button>
          <Popconfirm
            title="确定删除该用户？"
            @confirm="handleDelete(row.id)"
          >
            <Button danger size="small" type="link">删除</Button>
          </Popconfirm>
        </Space>
      </template>
    </Grid>
  </Page>
</template>
