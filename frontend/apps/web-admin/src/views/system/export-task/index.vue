<script lang="ts" setup>
import {ref} from 'vue';

import {Page} from '@vben/common-ui';

import {Button, message, Popconfirm, Space, Table, Tag} from 'ant-design-vue';

import {
  deleteExportTask,
  downloadExportTask,
  getExportTaskList,
} from '#/api/system/export-task';

defineOptions({ name: 'SystemExportTask' });

const dataList = ref<any[]>([]);
const loading = ref(false);
const pagination = ref({ current: 1, pageSize: 10, total: 0 });

const statusMap: Record<number, { color: string; label: string }> = {
  0: { color: 'default', label: '排队中' },
  1: { color: 'processing', label: '处理中' },
  2: { color: 'success', label: '成功' },
  3: { color: 'error', label: '失败' },
};

async function loadData() {
  loading.value = true;
  try {
    const res = (await getExportTaskList({
      pageNum: pagination.value.current,
      pageSize: pagination.value.pageSize,
    })) as any;
    dataList.value = res?.records ?? [];
    pagination.value.total = res?.total ?? 0;
  } finally {
    loading.value = false;
  }
}
loadData();

async function handleDownload(id: number) {
  try {
    const res = (await downloadExportTask(id)) as any;
    if (res?.url) {
      window.open(res.url, '_blank');
    } else {
      message.warning('下载地址不可用');
    }
  } catch {
    message.error('获取下载链接失败');
  }
}

async function handleDelete(id: number) {
  await deleteExportTask(id);
  message.success('删除成功');
  await loadData();
}

function handlePageChange(page: number, pageSize: number) {
  pagination.value.current = page;
  pagination.value.pageSize = pageSize;
  loadData();
}

function formatFileSize(bytes?: number) {
  if (!bytes) return '-';
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`;
}
</script>

<template>
  <Page auto-content-height>
    <div class="p-4">
      <div class="mb-4 flex items-center justify-between">
        <h3 class="text-lg font-medium">下载中心</h3>
        <Button @click="loadData">刷新</Button>
      </div>
      <Table
        :columns="[
          { title: '任务名称', dataIndex: 'taskName', key: 'taskName', ellipsis: true },
          { title: '文件名', dataIndex: 'fileName', key: 'fileName', ellipsis: true },
          { title: '文件大小', key: 'fileSize', width: 100 },
          { title: '状态', key: 'status', width: 100 },
          { title: '下载次数', dataIndex: 'downloadCount', key: 'downloadCount', width: 90 },
          { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
          { title: '操作', key: 'action', width: 140, fixed: 'right' },
        ]"
        :data-source="dataList"
        :loading="loading"
        :pagination="{
          current: pagination.current,
          pageSize: pagination.pageSize,
          total: pagination.total,
          showSizeChanger: true,
          showTotal: (t: number) => `共 ${t} 条`,
          onChange: handlePageChange,
        }"
        row-key="id"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'fileSize'">
            {{ formatFileSize(record.fileSize) }}
          </template>
          <template v-if="column.key === 'status'">
            <Tag :color="statusMap[record.status as number]?.color ?? 'default'">
              {{ statusMap[record.status as number]?.label ?? '未知' }}
            </Tag>
          </template>
          <template v-if="column.key === 'action'">
            <Space>
              <Button
                v-if="record.status === 2"
                size="small"
                type="link"
                @click="handleDownload(record.id)"
              >
                下载
              </Button>
              <Popconfirm
                title="确定删除？"
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
