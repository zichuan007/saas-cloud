<script lang="ts" setup>
import type { VxeGridProps } from '#/adapter/vxe-table';

import { Page } from '@vben/common-ui';

import {
  Avatar,
  Button,
  message,
  Popconfirm,
  Select,
  Space,
  Tag,
} from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getFanList, setFanBlacklist, syncFans } from '#/api/wechat/fan';
import { useWechatAccount } from '../use-account';

defineOptions({ name: 'WechatFan' });

const { accountList, currentAccountId, loadAccounts } = useWechatAccount();
loadAccounts();

const gridOptions: VxeGridProps = {
  columns: [
    {
      field: 'headImgUrl',
      slots: { default: 'avatar' },
      title: '头像',
      width: 70,
    },
    { field: 'nickname', minWidth: 140, title: '昵称' },
    { field: 'openId', minWidth: 200, title: 'OpenID' },
    { field: 'province', title: '省份', width: 100 },
    { field: 'city', title: '城市', width: 100 },
    {
      field: 'isBlacklist',
      slots: { default: 'blacklist' },
      title: '黑名单',
      width: 80,
    },
    { field: 'subscribeTime', title: '关注时间', width: 180 },
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
        return await getFanList({
          accountId: currentAccountId.value,
          pageNum: page.currentPage,
          pageSize: page.pageSize,
        });
      },
    },
  },
  toolbarConfig: { slots: { buttons: 'toolbar-btns' } },
};

const [Grid, gridApi] = useVbenVxeGrid({ gridOptions });

function handleAccountChange() {
  gridApi.reload();
}

async function handleSync() {
  await syncFans();
  message.success('同步粉丝成功');
  gridApi.reload();
}

async function handleToggleBlacklist(row: any) {
  const newVal = !row.isBlacklist;
  await setFanBlacklist(row.id, newVal);
  message.success(newVal ? '已拉黑' : '已取消拉黑');
  gridApi.reload();
}
</script>

<template>
  <Page auto-content-height>
    <Grid>
      <template #toolbar-btns>
        <Space>
          <Select
            v-model:value="currentAccountId"
            :options="
              accountList.map((a) => ({ label: a.accountName, value: a.id }))
            "
            placeholder="选择公众号"
            style="width: 200px"
            @change="handleAccountChange"
          />
          <Button @click="handleSync">同步粉丝</Button>
        </Space>
      </template>
      <template #avatar="{ row }">
        <Avatar :src="row.headImgUrl" size="small" />
      </template>
      <template #blacklist="{ row }">
        <Tag v-if="row.isBlacklist" color="red">是</Tag>
        <Tag v-else color="default">否</Tag>
      </template>
      <template #action="{ row }">
        <Popconfirm
          :title="row.isBlacklist ? '确定取消拉黑？' : '确定拉黑该粉丝？'"
          @confirm="handleToggleBlacklist(row)"
        >
          <Button size="small" type="link">
            {{ row.isBlacklist ? '取消拉黑' : '拉黑' }}
          </Button>
        </Popconfirm>
      </template>
    </Grid>
  </Page>
</template>
