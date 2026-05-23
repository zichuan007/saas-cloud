<script lang="ts" setup>
import type {VxeGridProps} from '#/adapter/vxe-table';
import {useVbenVxeGrid} from '#/adapter/vxe-table';

import {Page, useVbenModal} from '@vben/common-ui';

import {Button, message, Popconfirm, Space, Tag} from 'ant-design-vue';
import {
  deleteTenant,
  exportTenants,
  freezeTenant,
  getTenantList,
  type TenantRecord,
  unfreezeTenant,
} from '#/api/platform/tenant';

import TenantFormModal from './tenant-form-modal.vue';

defineOptions({ name: 'PlatformTenant' });

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: TenantFormModal,
});

const gridOptions: VxeGridProps = {
  columns: [
    { field: 'tenantName', minWidth: 160, title: '租户名称' },
    { field: 'tenantCode', title: '租户编码', width: 120 },
    { field: 'contactPerson', title: '联系人', width: 100 },
    { field: 'phone', title: '手机号', width: 130 },
    { field: 'packageName', title: '套餐', width: 100 },
    { field: 'userCount', title: '用户数', width: 80 },
    {
      field: 'status',
      slots: { default: 'status' },
      title: '状态',
      width: 100,
    },
    { field: 'expireTime', title: '到期时间', width: 120 },
    { field: 'createTime', title: '创建时间', width: 160 },
    {
      field: 'action',
      fixed: 'right',
      slots: { default: 'action' },
      title: '操作',
      width: 260,
    },
  ],
  pagerConfig: { enabled: true },
  proxyConfig: {
    ajax: {
      query: async ({ page }) => {
        return await getTenantList({
          pageNum: page.currentPage,
          pageSize: page.pageSize,
        });
      },
    },
  },
  toolbarConfig: { slots: { buttons: 'toolbar-btns' } },
};

const [Grid, gridApi] = useVbenVxeGrid({ gridOptions });

function handleAdd() {
  formModalApi.setData({ mode: 'add' });
  formModalApi.open();
}

function handleEdit(row: TenantRecord) {
  formModalApi.setData({ id: row.id, mode: 'edit' });
  formModalApi.open();
}

async function handleFreeze(row: TenantRecord) {
  await freezeTenant(row.id);
  message.success('已冻结');
  gridApi.reload();
}

async function handleUnfreeze(row: TenantRecord) {
  await unfreezeTenant(row.id);
  message.success('已解冻');
  gridApi.reload();
}

async function handleDelete(id: number) {
  await deleteTenant(id);
  message.success('已注销');
  gridApi.reload();
}

async function handleExport() {
  try {
    const res = await exportTenants();
    const blob = new Blob([res as any], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = '租户列表.xlsx';
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
    <FormModal @success="gridApi.reload()" />
    <Grid>
      <template #toolbar-btns>
        <Button type="primary" @click="handleAdd">创建租户</Button>
        <Button class="ml-2" @click="handleExport">导出</Button>
      </template>
      <template #status="{ row }">
        <Tag v-if="row.status === 1" color="green">正常</Tag>
        <Tag v-else-if="row.status === 0" color="red">冻结</Tag>
        <Tag v-else-if="row.status === 2" color="blue">试用</Tag>
        <Tag v-else color="default">{{ row.status }}</Tag>
      </template>
      <template #action="{ row }">
        <Space>
          <Button size="small" type="link" @click="handleEdit(row)">
            编辑
          </Button>
          <Button
            v-if="row.status === 1 || row.status === 2"
            size="small"
            type="link"
            @click="handleFreeze(row)"
          >
            冻结
          </Button>
          <Button
            v-if="row.status === 0"
            size="small"
            type="link"
            @click="handleUnfreeze(row)"
          >
            解冻
          </Button>
          <Popconfirm
            title="确定注销该租户？此操作不可恢复"
            @confirm="handleDelete(row.id)"
          >
            <Button danger size="small" type="link">注销</Button>
          </Popconfirm>
        </Space>
      </template>
    </Grid>
  </Page>
</template>
