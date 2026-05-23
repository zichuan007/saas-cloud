<script lang="ts" setup>
import type {VxeGridProps} from '#/adapter/vxe-table';
import {useVbenVxeGrid} from '#/adapter/vxe-table';

import {Page} from '@vben/common-ui';

import {Button, message, Popconfirm, Space, Tag} from 'ant-design-vue';
import {
  deleteMessage,
  getMessageList,
  markAllRead,
  markMessageRead,
  type NotifyMessage,
} from '#/api/notify';

defineOptions({ name: 'NotifyMessage' });

const gridOptions: VxeGridProps = {
  columns: [
    {
      field: 'isRead',
      slots: { default: 'readStatus' },
      title: '状态',
      width: 80,
    },
    { field: 'title', minWidth: 200, title: '标题' },
    { field: 'content', minWidth: 300, title: '内容' },
    {
      field: 'type',
      slots: { default: 'type' },
      title: '类型',
      width: 100,
    },
    { field: 'createTime', title: '时间', width: 180 },
    {
      field: 'action',
      fixed: 'right',
      slots: { default: 'action' },
      title: '操作',
      width: 160,
    },
  ],
  pagerConfig: { enabled: true },
  proxyConfig: {
    ajax: {
      query: async ({ page }) => {
        return await getMessageList({
          pageNum: page.currentPage,
          pageSize: page.pageSize,
        });
      },
    },
  },
  toolbarConfig: { slots: { buttons: 'toolbar-btns' } },
};

const [Grid, gridApi] = useVbenVxeGrid({ gridOptions });

async function handleMarkRead(row: NotifyMessage) {
  await markMessageRead(row.id);
  message.success('已标记为已读');
  gridApi.reload();
}

async function handleMarkAllRead() {
  await markAllRead();
  message.success('全部已读');
  gridApi.reload();
}

async function handleDelete(id: number) {
  await deleteMessage(id);
  message.success('删除成功');
  gridApi.reload();
}
</script>

<template>
  <Page auto-content-height>
    <Grid>
      <template #toolbar-btns>
        <Button @click="handleMarkAllRead">全部已读</Button>
      </template>
      <template #readStatus="{ row }">
        <Tag v-if="row.isRead" color="default">已读</Tag>
        <Tag v-else color="blue">未读</Tag>
      </template>
      <template #type="{ row }">
        <Tag v-if="row.type === 'system'" color="blue">系统</Tag>
        <Tag v-else-if="row.type === 'workflow'" color="green">流程</Tag>
        <Tag v-else color="default">{{ row.type || '其他' }}</Tag>
      </template>
      <template #action="{ row }">
        <Space>
          <Button
            v-if="!row.isRead"
            size="small"
            type="link"
            @click="handleMarkRead(row)"
          >
            标为已读
          </Button>
          <Popconfirm
            title="确定删除该消息？"
            @confirm="handleDelete(row.id)"
          >
            <Button danger size="small" type="link">删除</Button>
          </Popconfirm>
        </Space>
      </template>
    </Grid>
  </Page>
</template>
