<script lang="ts" setup>
import {ref} from 'vue';

import {Page, useVbenModal} from '@vben/common-ui';

import {Button, message, Popconfirm, Space, Table, Tag} from 'ant-design-vue';

import {deleteAccount, getAccountList, type WechatAccount,} from '#/api/wechat/account';

import AccountFormModal from './account-form-modal.vue';

defineOptions({ name: 'WechatAccount' });

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: AccountFormModal,
});

const dataList = ref<WechatAccount[]>([]);
const loading = ref(false);

async function loadData() {
  loading.value = true;
  try {
    dataList.value = (await getAccountList()) as WechatAccount[];
  } finally {
    loading.value = false;
  }
}
loadData();

function handleAdd() {
  formModalApi.setData({ mode: 'add' });
  formModalApi.open();
}

function handleEdit(row: WechatAccount) {
  formModalApi.setData({ id: row.id, mode: 'edit' });
  formModalApi.open();
}

async function handleDelete(id: number) {
  await deleteAccount(id);
  message.success('解绑成功');
  await loadData();
}
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="loadData" />
    <div class="p-4">
      <div class="mb-4 flex justify-between">
        <h3 class="text-lg font-medium">公众号管理</h3>
        <Button type="primary" @click="handleAdd">绑定公众号</Button>
      </div>
      <Table
        :columns="[
          { title: '公众号名称', dataIndex: 'accountName', key: 'accountName', width: 200 },
          { title: 'AppID', dataIndex: 'appId', key: 'appId', width: 200 },
          { title: '类型', key: 'accountType', width: 100 },
          { title: '认证状态', key: 'isVerified', width: 100 },
          { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
          { title: '操作', key: 'action', width: 180, fixed: 'right' },
        ]"
        :data-source="dataList"
        :loading="loading"
        :pagination="false"
        row-key="id"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'accountType'">
            <Tag v-if="record.accountType === 1" color="blue">服务号</Tag>
            <Tag v-else color="green">订阅号</Tag>
          </template>
          <template v-if="column.key === 'isVerified'">
            <Tag v-if="record.isVerified === 1" color="green">已认证</Tag>
            <Tag v-else color="default">未认证</Tag>
          </template>
          <template v-if="column.key === 'action'">
            <Space>
              <Button size="small" type="link" @click="handleEdit(record)">
                编辑
              </Button>
              <Popconfirm
                title="确定解绑该公众号？"
                @confirm="handleDelete(record.id)"
              >
                <Button danger size="small" type="link">解绑</Button>
              </Popconfirm>
            </Space>
          </template>
        </template>
      </Table>
    </div>
  </Page>
</template>
