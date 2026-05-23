<script lang="ts" setup>
import {ref} from 'vue';

import {Page, useVbenModal} from '@vben/common-ui';

import {Button, message, Popconfirm, Space, Table, Tag,} from 'ant-design-vue';

import {useRouter} from 'vue-router';

import {
  deleteDefinition,
  getDefinitionList,
  type ProcessDefinition,
  updateDefinitionStatus,
} from '#/api/workflow/definition';

import DefinitionFormModal from './definition-form-modal.vue';

defineOptions({ name: 'WorkflowDefinition' });

const router = useRouter();

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: DefinitionFormModal,
});

const dataList = ref<ProcessDefinition[]>([]);
const loading = ref(false);

async function loadData() {
  loading.value = true;
  try {
    const res = await getDefinitionList();
    dataList.value = (res as any)?.records ?? (res as ProcessDefinition[]);
  } finally {
    loading.value = false;
  }
}
loadData();

function handleAdd() {
  formModalApi.setData({ mode: 'add' });
  formModalApi.open();
}

function handleEdit(row: ProcessDefinition) {
  formModalApi.setData({ id: row.id, mode: 'edit' });
  formModalApi.open();
}

async function handleDelete(id: number) {
  await deleteDefinition(id);
  message.success('删除成功');
  await loadData();
}

async function handleDeploy(row: ProcessDefinition) {
  router.push(`/workflow/designer/${row.id}`);
}

async function handleToggleStatus(row: ProcessDefinition) {
  const newStatus = row.status === 1 ? 0 : 1;
  await updateDefinitionStatus(row.id, newStatus);
  message.success(newStatus === 1 ? '已激活' : '已挂起');
  await loadData();
}

function handleDesign(row: ProcessDefinition) {
  router.push(`/workflow/designer/${row.id}`);
}
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="loadData" />
    <div class="p-4">
      <div class="mb-4 flex justify-between">
        <h3 class="text-lg font-medium">流程定义</h3>
        <Button type="primary" @click="handleAdd">新建流程</Button>
      </div>
      <Table
        :columns="[
          { title: '流程名称', dataIndex: 'processName', key: 'processName', width: 200 },
          { title: '流程标识', dataIndex: 'processKey', key: 'processKey', width: 160 },
          { title: '版本', dataIndex: 'version', key: 'version', width: 80 },
          { title: '状态', key: 'status', width: 100 },
          { title: '部署时间', dataIndex: 'deployTime', key: 'deployTime', width: 180 },
          { title: '操作', key: 'action', width: 320, fixed: 'right' },
        ]"
        :data-source="dataList"
        :loading="loading"
        :pagination="false"
        row-key="id"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <Tag v-if="record.status === 1" color="green">激活</Tag>
            <Tag v-else-if="record.status === 0" color="orange">挂起</Tag>
            <Tag v-else color="default">未部署</Tag>
          </template>
          <template v-if="column.key === 'action'">
            <Space>
              <Button size="small" type="link" @click="handleEdit(record)">
                编辑
              </Button>
              <Button size="small" type="link" @click="handleDesign(record)">
                设计
              </Button>
              <Button
                size="small"
                type="link"
                @click="handleToggleStatus(record)"
              >
                {{ record.status === 1 ? '挂起' : '激活' }}
              </Button>
              <Popconfirm
                title="确定删除该流程定义？"
                @confirm="handleDelete(record.id)"
              >
                <Button danger size="small" type="link">删除</Button>
              </Popconfirm>
            </Space>
          </template>
        </template>
      </Table>
    </div>
  </Page>
</template>
