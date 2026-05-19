<script lang="ts" setup>
import { ref } from 'vue';

import { Page, useVbenModal } from '@vben/common-ui';

import { Button, message, Popconfirm, Space, Table, Tag } from 'ant-design-vue';

import { requestClient } from '#/api/request';

import TemplateFormModal from './template-form-modal.vue';

defineOptions({ name: 'NotifyTemplate' });

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: TemplateFormModal,
});

const dataList = ref<any[]>([]);
const loading = ref(false);

const typeMap: Record<number, { color: string; label: string }> = {
  0: { color: 'blue', label: '站内信' },
  1: { color: 'green', label: '邮件' },
  2: { color: 'orange', label: 'IM Webhook' },
};

async function loadData() {
  loading.value = true;
  try {
    dataList.value = (await requestClient.get('/notify/template/list')) as any[];
  } finally {
    loading.value = false;
  }
}
loadData();

function handleAdd() {
  formModalApi.setData({ mode: 'add' });
  formModalApi.open();
}

function handleEdit(row: any) {
  formModalApi.setData({ mode: 'edit', record: row });
  formModalApi.open();
}

async function handleDelete(id: number) {
  await requestClient.delete(`/notify/template/${id}`);
  message.success('删除成功');
  await loadData();
}
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="loadData" />
    <div class="p-4">
      <div class="mb-4 flex items-center justify-between">
        <h3 class="text-lg font-medium">通知模板</h3>
        <Button type="primary" @click="handleAdd">新建模板</Button>
      </div>
      <Table
        :columns="[
          { title: '模板编码', dataIndex: 'templateCode', key: 'templateCode', width: 160 },
          { title: '模板名称', dataIndex: 'templateName', key: 'templateName', width: 180 },
          { title: '渠道', key: 'type', width: 100 },
          { title: '标题模板', dataIndex: 'titleTemplate', key: 'titleTemplate', ellipsis: true },
          { title: '状态', key: 'status', width: 80 },
          { title: '操作', key: 'action', width: 180, fixed: 'right' },
        ]"
        :data-source="dataList"
        :loading="loading"
        :pagination="false"
        row-key="id"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'type'">
            <Tag :color="typeMap[record.type]?.color ?? 'default'">
              {{ typeMap[record.type]?.label ?? '未知' }}
            </Tag>
          </template>
          <template v-if="column.key === 'status'">
            <Tag :color="record.status === 1 ? 'green' : 'default'">
              {{ record.status === 1 ? '启用' : '禁用' }}
            </Tag>
          </template>
          <template v-if="column.key === 'action'">
            <Space>
              <Button size="small" type="link" @click="handleEdit(record)">
                编辑
              </Button>
              <Popconfirm
                title="确定删除该模板？"
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
