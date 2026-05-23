<script lang="ts" setup>
import type {VxeGridProps} from '#/adapter/vxe-table';
import {useVbenVxeGrid} from '#/adapter/vxe-table';

import {Page} from '@vben/common-ui';

import {Button, message, Modal, Space} from 'ant-design-vue';
import {approveTask, getTodoList, rejectTask, type TaskRecord,} from '#/api/workflow/task';

defineOptions({ name: 'WorkflowTodo' });

const gridOptions: VxeGridProps = {
  columns: [
    { field: 'title', minWidth: 200, title: '流程标题' },
    { field: 'processName', minWidth: 120, title: '流程名称' },
    { field: 'taskName', minWidth: 120, title: '当前节点' },
    { field: 'createTime', title: '接收时间', width: 180 },
    {
      field: 'action',
      fixed: 'right',
      slots: { default: 'action' },
      title: '操作',
      width: 200,
    },
  ],
  pagerConfig: { enabled: true },
  proxyConfig: {
    ajax: {
      query: async ({ page }) => {
        return await getTodoList({
          pageNum: page.currentPage,
          pageSize: page.pageSize,
        });
      },
    },
  },
};

const [Grid, gridApi] = useVbenVxeGrid({ gridOptions });

async function handleApprove(row: TaskRecord) {
  Modal.confirm({
    content: '确定通过该审批？',
    async onOk() {
      await approveTask(row.id, { comment: '同意' });
      message.success('审批通过');
      gridApi.reload();
    },
    title: '审批确认',
  });
}

async function handleReject(row: TaskRecord) {
  Modal.confirm({
    content: '确定驳回该审批？',
    async onOk() {
      await rejectTask(row.id, { comment: '驳回' });
      message.success('已驳回');
      gridApi.reload();
    },
    title: '驳回确认',
  });
}
</script>

<template>
  <Page auto-content-height>
    <Grid>
      <template #action="{ row }">
        <Space>
          <Button size="small" type="link" @click="handleApprove(row)">
            通过
          </Button>
          <Button danger size="small" type="link" @click="handleReject(row)">
            驳回
          </Button>
        </Space>
      </template>
    </Grid>
  </Page>
</template>
