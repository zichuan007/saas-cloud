<script lang="ts" setup>
import {ref, watch} from 'vue';

import {Page, useVbenModal} from '@vben/common-ui';

import {Button, message, Popconfirm, Select, Space, Table,} from 'ant-design-vue';

import {deleteTag, getTagList, syncTags, type TagRecord,} from '#/api/wechat/tag';

import {useWechatAccount} from '../use-account';
import TagFormModal from './tag-form-modal.vue';

defineOptions({ name: 'WechatTag' });

const { accountList, currentAccountId, loadAccounts } = useWechatAccount();
loadAccounts();

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: TagFormModal,
});

const dataList = ref<TagRecord[]>([]);
const loading = ref(false);

async function loadData() {
  if (!currentAccountId.value) return;
  loading.value = true;
  try {
    dataList.value = (await getTagList({
      accountId: currentAccountId.value,
    })) as TagRecord[];
  } finally {
    loading.value = false;
  }
}

watch(currentAccountId, (val) => {
  if (val) loadData();
});

function handleAdd() {
  formModalApi.setData({ accountId: currentAccountId.value, mode: 'add' });
  formModalApi.open();
}

function handleEdit(row: TagRecord) {
  formModalApi.setData({
    accountId: currentAccountId.value,
    id: row.id,
    mode: 'edit',
    tagName: row.tagName,
  });
  formModalApi.open();
}

async function handleDelete(id: number) {
  await deleteTag(id);
  message.success('删除成功');
  await loadData();
}

async function handleSync() {
  if (!currentAccountId.value) {
    message.warning('请先选择公众号');
    return;
  }
  await syncTags(currentAccountId.value);
  message.success('同步成功');
  await loadData();
}
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="loadData" />
    <div class="p-4">
      <div class="mb-4 flex items-center justify-between">
        <h3 class="text-lg font-medium">标签管理</h3>
        <Space>
          <Select
            v-model:value="currentAccountId"
            :options="
              accountList.map((a) => ({ label: a.accountName, value: a.id }))
            "
            placeholder="选择公众号"
            style="width: 200px"
          />
          <Button @click="handleSync">同步标签</Button>
          <Button type="primary" @click="handleAdd">新建标签</Button>
        </Space>
      </div>
      <Table
        :columns="[
          { title: '标签名称', dataIndex: 'tagName', key: 'tagName', width: 200 },
          { title: '粉丝数', dataIndex: 'fanCount', key: 'fanCount', width: 100 },
          { title: '操作', key: 'action', width: 180 },
        ]"
        :data-source="dataList"
        :loading="loading"
        :pagination="false"
        row-key="id"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <Space>
              <Button size="small" type="link" @click="handleEdit(record)">
                编辑
              </Button>
              <Popconfirm
                title="确定删除该标签？"
                @confirm="handleDelete(record.id)"
              >
                <Button danger size="small" type="link">删除</Button>
              </Popconfirm>
            </Space>
          </template>
        </template>
      </Table>
    </div>
  </Page>
</template>
