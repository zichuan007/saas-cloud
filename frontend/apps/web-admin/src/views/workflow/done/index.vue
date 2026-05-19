<script lang="ts" setup>
import type { VxeGridProps } from '#/adapter/vxe-table';

import { Page } from '@vben/common-ui';

import { Tag } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getDoneList } from '#/api/workflow/task';

defineOptions({ name: 'WorkflowDone' });

const gridOptions: VxeGridProps = {
  columns: [
    { field: 'title', minWidth: 200, title: '流程标题' },
    { field: 'processName', minWidth: 120, title: '流程名称' },
    { field: 'taskName', minWidth: 120, title: '处理节点' },
    {
      field: 'result',
      slots: { default: 'result' },
      title: '处理结果',
      width: 100,
    },
    { field: 'endTime', title: '处理时间', width: 180 },
  ],
  pagerConfig: { enabled: true },
  proxyConfig: {
    ajax: {
      query: async ({ page }) => {
        return await getDoneList({
          pageNum: page.currentPage,
          pageSize: page.pageSize,
        });
      },
    },
  },
};

const [Grid] = useVbenVxeGrid({ gridOptions });
</script>

<template>
  <Page auto-content-height>
    <Grid>
      <template #result="{ row }">
        <Tag v-if="row.result === 'approve'" color="green">已通过</Tag>
        <Tag v-else-if="row.result === 'reject'" color="red">已驳回</Tag>
        <Tag v-else color="default">{{ row.result || '-' }}</Tag>
      </template>
    </Grid>
  </Page>
</template>
