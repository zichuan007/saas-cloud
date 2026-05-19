<script lang="ts" setup>
import type { VxeGridProps } from '#/adapter/vxe-table';

import { ref } from 'vue';

import { Page } from '@vben/common-ui';

import {
  Button,
  Card,
  Col,
  message,
  Popconfirm,
  Row,
  Space,
  Statistic,
} from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  getRunningInstances,
  getStatistics,
  terminateInstance,
} from '#/api/workflow/monitor';

defineOptions({ name: 'WorkflowMonitor' });

const stats = ref<Record<string, any>>({});

async function loadStats() {
  stats.value = (await getStatistics()) as Record<string, any>;
}
loadStats();

const gridOptions: VxeGridProps = {
  columns: [
    { field: 'title', minWidth: 200, title: '流程标题' },
    { field: 'processName', minWidth: 120, title: '流程名称' },
    { field: 'startUser', title: '发起人', width: 100 },
    { field: 'currentTask', minWidth: 120, title: '当前节点' },
    { field: 'currentAssignee', title: '处理人', width: 100 },
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
        return await getRunningInstances({
          pageNum: page.currentPage,
          pageSize: page.pageSize,
        });
      },
    },
  },
};

const [Grid, gridApi] = useVbenVxeGrid({ gridOptions });

async function handleTerminate(id: number) {
  await terminateInstance(id);
  message.success('已终止');
  gridApi.reload();
}
</script>

<template>
  <Page auto-content-height>
    <div class="p-4">
      <Row :gutter="16" class="mb-4">
        <Col :span="6">
          <Card>
            <Statistic title="运行中" :value="stats.running ?? 0" />
          </Card>
        </Col>
        <Col :span="6">
          <Card>
            <Statistic title="已完成" :value="stats.completed ?? 0" />
          </Card>
        </Col>
        <Col :span="6">
          <Card>
            <Statistic title="已终止" :value="stats.terminated ?? 0" />
          </Card>
        </Col>
        <Col :span="6">
          <Card>
            <Statistic title="本月发起" :value="stats.monthlyStarted ?? 0" />
          </Card>
        </Col>
      </Row>
    </div>
    <Grid>
      <template #action="{ row }">
        <Space>
          <Popconfirm
            title="确定强制终止该流程？"
            @confirm="handleTerminate(row.id)"
          >
            <Button danger size="small" type="link">终止</Button>
          </Popconfirm>
        </Space>
      </template>
    </Grid>
  </Page>
</template>
