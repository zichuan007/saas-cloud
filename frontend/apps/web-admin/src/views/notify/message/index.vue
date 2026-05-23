<script lang="ts" setup>
import {ref} from 'vue';

import {Page} from '@vben/common-ui';

import {Button, message, Popconfirm, Space, Table, Tag} from 'ant-design-vue';

import {requestClient} from '#/api/request';

defineOptions({ name: 'NotifyMessage' });

const dataList = ref<any[]>([]);
const loading = ref(false);
const pagination = ref({ current: 1, pageSize: 10, total: 0 });

const typeMap: Record<number, { color: string; label: string }> = {
  0: { color: 'blue', label: '系统通知' },
  1: { color: 'orange', label: '审批通知' },
  2: { color: 'red', label: '催办' },
  3: { color: 'green', label: '公告' },
};

async function loadData() {
  loading.value = true;
  try {
    const res = (await requestClient.get('/notify/message/list', {
      params: {
        pageNum: pagination.value.current,
        pageSize: pagination.value.pageSize,
      },
    })) as any;
    dataList.value = res?.records ?? [];
    pagination.value.total = res?.total ?? 0;
  } finally {
    loading.value = false;
  }
}
loadData();

function handlePageChange(page: number, pageSize: number) {
  pagination.value.current = page;
  pagination.value.pageSize = pageSize;
  loadData();
}

async function handleMarkRead(id: number) {
  await requestClient.put(`/notify/message/${id}/read`);
  message.success('已标记为已读');
  await loadData();
}

async function handleMarkAllRead() {
  await requestClient.put('/notify/message/read-all');
  message.success('已全部标记为已读');
  await loadData();
}

async function handleDelete(id: number) {
  await requestClient.delete(`/notify/message/${id}`);
  message.success('删除成功');
  await loadData();
}
</script>

<template>
  <Page auto-content-height>
    <div class="p-4">
      <div class="mb-4 flex items-center justify-between">
        <h3 class="text-lg font-medium">站内消息</h3>
        <Button type="primary" @click="handleMarkAllRead">全部已读</Button>
      </div>
      <Table
        :columns="[
          { title: '标题', dataIndex: 'title', key: 'title', ellipsis: true },
          { title: '类型', key: 'type', width: 100 },
          { title: '发送人', dataIndex: 'senderName', key: 'senderName', width: 100 },
          { title: '状态', key: 'isRead', width: 80 },
          { title: '时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
          { title: '操作', key: 'action', width: 160, fixed: 'right' },
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
          <template v-if="column.key === 'type'">
            <Tag :color="typeMap[record.type]?.color ?? 'default'">
              {{ typeMap[record.type]?.label ?? '未知' }}
            </Tag>
          </template>
          <template v-if="column.key === 'isRead'">
            <Tag :color="record.isRead === 1 ? 'default' : 'green'">
              {{ record.isRead === 1 ? '已读' : '未读' }}
            </Tag>
          </template>
          <template v-if="column.key === 'action'">
            <Space>
              <Button
                v-if="record.isRead === 0"
                size="small"
                type="link"
                @click="handleMarkRead(record.id)"
              >
                标为已读
              </Button>
              <Popconfirm
                title="确定删除该消息？"
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
