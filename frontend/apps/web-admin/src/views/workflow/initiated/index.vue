<script lang="ts" setup>
import type {VxeGridProps} from '#/adapter/vxe-table';
import {useVbenVxeGrid} from '#/adapter/vxe-table';

import {Page} from '@vben/common-ui';

import {Button, message, Popconfirm, Space, Tag} from 'ant-design-vue';
import {cancelProcess, getMyInitiated, type ProcessInstance,} from '#/api/workflow/process';

defineOptions({ name: 'WorkflowInitiated' });

const gridOptions: VxeGridProps = {
  columns: [
    { field: 'title', minWidth: 200, title: '流程标题' },
    { field: 'processName', minWidth: 120, title: '流程名称' },
    { field: 'currentTask', minWidth: 120, title: '当前节点' },
    { field: 'currentAssignee', title: '当前处理人', width: 120 },
    {
      field: 'status',
      slots: { default: 'status' },
      title: '状态',
      width: 100,
    },
    { field: 'createTime', title: '发起时间', width: 180 },
    {
      field: 'action',
      fixed: 'right',
      slots: { default: 'action' },
      title: '操作',
      width: 120,
    },
  ],
  pagerConfig: { enabled: true },
  proxyConfig: {
    ajax: {
      query: async ({ page }) => {
        return await getMyInitiated({
          pageNum: page.currentPage,
          pageSize: page.pageSize,
        });
      },
    },
  },
};

const [Grid, gridApi] = useVbenVxeGrid({ gridOptions });

async function handleCancel(row: ProcessInstance) {
  await cancelProcess(row.id);
  message.success('已撤回');
  gridApi.reload();
}
</script>

<template>
  <Page auto-content-height>
    <Grid>
      <template #status="{ row }">
        <Tag v-if="row.status === 'running'" color="processing">进行中</Tag>
        <Tag v-else-if="row.status === 'completed'" color="green">已完成</Tag>
        <Tag v-else-if="row.status === 'cancelled'" color="default">已撤回</Tag>
        <Tag v-else-if="row.status === 'rejected'" color="red">已驳回</Tag>
        <Tag v-else color="default">{{ row.status || '-' }}</Tag>
      </template>
      <template #action="{ row }">
        <Space>
          <Popconfirm
            v-if="row.status === 'running'"
            title="确定撤回该流程？"
            @confirm="handleCancel(row)"
          >
            <Button danger size="small" type="link">撤回</Button>
          </Popconfirm>
        </Space>
      </template>
    </Grid>
  </Page>
</template>
