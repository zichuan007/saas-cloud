<script lang="ts" setup>
import type { VxeGridProps } from '#/adapter/vxe-table';
import { useVbenVxeGrid } from '#/adapter/vxe-table';

import { Page } from '@vben/common-ui';

import { Button, message, Popconfirm, Tag } from 'ant-design-vue';

import { cancelOrder, getMyOrders } from '#/api/subscription';

defineOptions({ name: 'MyOrders' });

const ORDER_TYPE_MAP: Record<number, string> = { 0: '新购', 1: '续费', 2: '升级' };
const PAY_STATUS_MAP: Record<number, { color: string; text: string }> = {
  0: { color: 'orange', text: '待支付' },
  1: { color: 'green', text: '已支付' },
  2: { color: 'default', text: '已取消' },
};

const gridOptions: VxeGridProps = {
  columns: [
    { field: 'orderNo', title: '订单编号', minWidth: 200 },
    { field: 'packageName', title: '套餐', minWidth: 120 },
    {
      field: 'orderType',
      title: '类型',
      width: 80,
      slots: { default: 'orderType' },
    },
    { field: 'amount', title: '金额', width: 100 },
    {
      field: 'payStatus',
      title: '状态',
      width: 100,
      slots: { default: 'payStatus' },
    },
    { field: 'payTime', title: '支付时间', minWidth: 160 },
    { field: 'expireTime', title: '到期时间', minWidth: 160 },
    { field: 'createTime', title: '下单时间', minWidth: 160 },
    {
      field: 'action',
      fixed: 'right',
      title: '操作',
      width: 100,
      slots: { default: 'action' },
    },
  ],
  proxyConfig: {
    ajax: {
      query: async ({ page }) => {
        return await getMyOrders({
          pageNum: page.currentPage,
          pageSize: page.pageSize,
        });
      },
    },
  },
  pagerConfig: { enabled: true },
};

const [Grid, gridApi] = useVbenVxeGrid({ gridOptions });

async function handleCancel(id: number) {
  await cancelOrder(id);
  message.success('已取消订单');
  gridApi.reload();
}
</script>

<template>
  <Page auto-content-height title="我的订单">
    <Grid>
      <template #orderType="{ row }">
        {{ ORDER_TYPE_MAP[row.orderType] || '未知' }}
      </template>
      <template #payStatus="{ row }">
        <Tag :color="PAY_STATUS_MAP[row.payStatus]?.color ?? 'default'">
          {{ PAY_STATUS_MAP[row.payStatus]?.text ?? '未知' }}
        </Tag>
      </template>
      <template #action="{ row }">
        <Popconfirm
          v-if="row.payStatus === 0"
          title="确定取消该订单？"
          @confirm="handleCancel(row.id)"
        >
          <Button danger size="small" type="link">取消</Button>
        </Popconfirm>
      </template>
    </Grid>
  </Page>
</template>
