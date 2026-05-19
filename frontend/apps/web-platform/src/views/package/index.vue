<script lang="ts" setup>
import { ref } from 'vue';

import { Page, useVbenModal } from '@vben/common-ui';

import {
  Button,
  message,
  Switch,
} from 'ant-design-vue';

import {
  getPackageList,
  type PackageRecord,
  updatePackageStatus,
} from '#/api/platform/package';

import PackageFormModal from './package-form-modal.vue';

defineOptions({ name: 'PlatformPackage' });

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: PackageFormModal,
});

const dataList = ref<PackageRecord[]>([]);
const loading = ref(false);

async function loadData() {
  loading.value = true;
  try {
    dataList.value = (await getPackageList()) as PackageRecord[];
  } finally {
    loading.value = false;
  }
}
loadData();

function handleAdd() {
  formModalApi.setData({ mode: 'add' });
  formModalApi.open();
}

function handleEdit(row: PackageRecord) {
  formModalApi.setData({ id: row.id, mode: 'edit' });
  formModalApi.open();
}

async function handleStatusChange(row: PackageRecord, checked: boolean) {
  await updatePackageStatus(row.id, checked ? 1 : 0);
  message.success(checked ? '已启用' : '已禁用');
  await loadData();
}
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="loadData" />
    <div class="p-4">
      <div class="mb-4 flex justify-between">
        <h3 class="text-lg font-medium">套餐管理</h3>
        <Button type="primary" @click="handleAdd">新建套餐</Button>
      </div>
      <a-table
        :columns="[
          { title: '套餐名称', dataIndex: 'packageName', key: 'packageName', width: 160 },
          { title: '用户上限', dataIndex: 'maxUserCount', key: 'maxUserCount', width: 100 },
          { title: '角色上限', dataIndex: 'maxRoleCount', key: 'maxRoleCount', width: 100 },
          { title: '部门上限', dataIndex: 'maxDeptCount', key: 'maxDeptCount', width: 100 },
          { title: '公众号上限', dataIndex: 'maxWechatAccount', key: 'maxWechatAccount', width: 110 },
          { title: '流程定义上限', dataIndex: 'maxProcessDefinition', key: 'maxProcessDefinition', width: 120 },
          { title: '状态', key: 'status', width: 100 },
          { title: '操作', key: 'action', width: 120, fixed: 'right' },
        ]"
        :data-source="dataList"
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
            <Button size="small" type="link" @click="handleEdit(record)">
              编辑
            </Button>
          </template>
        </template>
      </a-table>
    </div>
  </Page>
</template>
