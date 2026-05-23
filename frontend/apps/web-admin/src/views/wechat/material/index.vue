<script lang="ts" setup>
import type {VxeGridProps} from '#/adapter/vxe-table';
import {useVbenVxeGrid} from '#/adapter/vxe-table';

import {Page} from '@vben/common-ui';

import {Button, message, Popconfirm, Select, Space, Tag, Upload,} from 'ant-design-vue';
import {
  deleteMaterial,
  getMaterialList,
  syncMaterial,
  uploadMaterial,
} from '#/api/wechat/material';
import {useWechatAccount} from '../use-account';

defineOptions({ name: 'WechatMaterial' });

const { accountList, currentAccountId, loadAccounts } = useWechatAccount();
loadAccounts();

const gridOptions: VxeGridProps = {
  columns: [
    { field: 'name', minWidth: 200, title: '素材名称' },
    {
      field: 'type',
      slots: { default: 'type' },
      title: '类型',
      width: 100,
    },
    { field: 'mediaId', minWidth: 200, title: 'MediaID' },
    { field: 'createTime', title: '创建时间', width: 180 },
    {
      field: 'action',
      fixed: 'right',
      slots: { default: 'action' },
      title: '操作',
      width: 180,
    },
  ],
  pagerConfig: { enabled: true },
  proxyConfig: {
    ajax: {
      query: async ({ page }) => {
        return await getMaterialList({
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

async function handleUpload(file: File) {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('accountId', String(currentAccountId.value));
  await uploadMaterial(formData);
  message.success('上传成功');
  gridApi.reload();
  return false;
}

async function handleSync(id: number) {
  await syncMaterial(id);
  message.success('同步成功');
}

async function handleDelete(id: number) {
  await deleteMaterial(id);
  message.success('删除成功');
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
          <Upload
            :before-upload="handleUpload"
            :show-upload-list="false"
          >
            <Button type="primary">上传素材</Button>
          </Upload>
        </Space>
      </template>
      <template #type="{ row }">
        <Tag v-if="row.type === 'image'" color="blue">图片</Tag>
        <Tag v-else-if="row.type === 'voice'" color="green">语音</Tag>
        <Tag v-else-if="row.type === 'video'" color="orange">视频</Tag>
        <Tag v-else color="default">{{ row.type }}</Tag>
      </template>
      <template #action="{ row }">
        <Space>
          <Button size="small" type="link" @click="handleSync(row.id)">
            同步
          </Button>
          <Popconfirm
            title="确定删除该素材？"
            @confirm="handleDelete(row.id)"
          >
            <Button danger size="small" type="link">删除</Button>
          </Popconfirm>
        </Space>
      </template>
    </Grid>
  </Page>
</template>
