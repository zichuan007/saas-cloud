<script lang="ts" setup>
import type { VxeGridProps } from '#/adapter/vxe-table';

import { Page, useVbenModal } from '@vben/common-ui';

import { Button, message, Popconfirm, Select, Space, Tag } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  type ArticleRecord,
  deleteArticle,
  getArticleList,
  offlineArticle,
  publishArticle,
} from '#/api/wechat/article';
import { useWechatAccount } from '../use-account';

import ArticleFormModal from './article-form-modal.vue';

defineOptions({ name: 'WechatArticle' });

const { accountList, currentAccountId, loadAccounts } = useWechatAccount();
loadAccounts();

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: ArticleFormModal,
});

const gridOptions: VxeGridProps = {
  columns: [
    { field: 'title', minWidth: 200, title: '标题' },
    { field: 'author', title: '作者', width: 100 },
    { field: 'digest', minWidth: 200, title: '摘要' },
    {
      field: 'status',
      slots: { default: 'status' },
      title: '状态',
      width: 100,
    },
    { field: 'publishTime', title: '发布时间', width: 180 },
    {
      field: 'action',
      fixed: 'right',
      slots: { default: 'action' },
      title: '操作',
      width: 280,
    },
  ],
  pagerConfig: { enabled: true },
  proxyConfig: {
    ajax: {
      query: async ({ page }) => {
        return await getArticleList({
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

function handleAdd() {
  formModalApi.setData({ accountId: currentAccountId.value, mode: 'add' });
  formModalApi.open();
}

function handleEdit(row: ArticleRecord) {
  formModalApi.setData({ id: row.id, mode: 'edit' });
  formModalApi.open();
}

async function handlePublish(id: number) {
  await publishArticle(id);
  message.success('发布成功');
  gridApi.reload();
}

async function handleOffline(id: number) {
  await offlineArticle(id);
  message.success('已下线');
  gridApi.reload();
}

async function handleDelete(id: number) {
  await deleteArticle(id);
  message.success('删除成功');
  gridApi.reload();
}
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="gridApi.reload()" />
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
          <Button type="primary" @click="handleAdd">新建图文</Button>
        </Space>
      </template>
      <template #status="{ row }">
        <Tag v-if="row.status === 1" color="green">已发布</Tag>
        <Tag v-else-if="row.status === 2" color="default">已下线</Tag>
        <Tag v-else color="blue">草稿</Tag>
      </template>
      <template #action="{ row }">
        <Space>
          <Button size="small" type="link" @click="handleEdit(row)">
            编辑
          </Button>
          <Button
            v-if="row.status !== 1"
            size="small"
            type="link"
            @click="handlePublish(row.id)"
          >
            发布
          </Button>
          <Button
            v-if="row.status === 1"
            size="small"
            type="link"
            @click="handleOffline(row.id)"
          >
            下线
          </Button>
          <Popconfirm
            title="确定删除该图文？"
            @confirm="handleDelete(row.id)"
          >
            <Button danger size="small" type="link">删除</Button>
          </Popconfirm>
        </Space>
      </template>
    </Grid>
  </Page>
</template>
