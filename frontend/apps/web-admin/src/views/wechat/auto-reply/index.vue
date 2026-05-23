<script lang="ts" setup>
import {ref, watch} from 'vue';

import {Page, useVbenModal} from '@vben/common-ui';

import {Button, message, Popconfirm, Select, Space, Switch, Table, Tag,} from 'ant-design-vue';

import {
  type AutoReplyRule,
  deleteAutoReply,
  getAutoReplyList,
  updateAutoReplyStatus,
} from '#/api/wechat/auto-reply';

import {useWechatAccount} from '../use-account';
import AutoReplyFormModal from './auto-reply-form-modal.vue';

defineOptions({ name: 'WechatAutoReply' });

const { accountList, currentAccountId, loadAccounts } = useWechatAccount();
loadAccounts();

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: AutoReplyFormModal,
});

const dataList = ref<AutoReplyRule[]>([]);
const loading = ref(false);

async function loadData() {
  if (!currentAccountId.value) return;
  loading.value = true;
  try {
    dataList.value = (await getAutoReplyList({
      accountId: currentAccountId.value,
    })) as AutoReplyRule[];
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

function handleEdit(row: AutoReplyRule) {
  formModalApi.setData({
    accountId: currentAccountId.value,
    id: row.id,
    mode: 'edit',
    record: row,
  });
  formModalApi.open();
}

async function handleDelete(id: number) {
  await deleteAutoReply(id);
  message.success('删除成功');
  await loadData();
}

async function handleStatusChange(row: AutoReplyRule, checked: boolean) {
  await updateAutoReplyStatus(row.id, checked ? 1 : 0);
  message.success(checked ? '已启用' : '已禁用');
  await loadData();
}
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="loadData" />
    <div class="p-4">
      <div class="mb-4 flex items-center justify-between">
        <h3 class="text-lg font-medium">自动回复</h3>
        <Space>
          <Select
            v-model:value="currentAccountId"
            :options="
              accountList.map((a) => ({ label: a.accountName, value: a.id }))
            "
            placeholder="选择公众号"
            style="width: 200px"
          />
          <Button type="primary" @click="handleAdd">新建规则</Button>
        </Space>
      </div>
      <Table
        :columns="[
          { title: '规则名称', dataIndex: 'ruleName', key: 'ruleName', width: 160 },
          { title: '类型', key: 'ruleType', width: 100 },
          { title: '关键词', dataIndex: 'keyword', key: 'keyword', width: 160 },
          { title: '回复内容', dataIndex: 'replyContent', key: 'replyContent', ellipsis: true },
          { title: '状态', key: 'status', width: 100 },
          { title: '操作', key: 'action', width: 180, fixed: 'right' },
        ]"
        :data-source="dataList"
        :loading="loading"
        :pagination="false"
        row-key="id"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'ruleType'">
            <Tag v-if="record.ruleType === 1" color="blue">关键词</Tag>
            <Tag v-else-if="record.ruleType === 0" color="green">关注</Tag>
            <Tag v-else color="default">默认</Tag>
          </template>
          <template v-if="column.key === 'status'">
            <Switch
              :checked="record.status === 1"
              checked-children="启用"
              un-checked-children="禁用"
              @change="(c: any) => handleStatusChange(record, !!c)"
            />
          </template>
          <template v-if="column.key === 'action'">
            <Space>
              <Button size="small" type="link" @click="handleEdit(record)">
                编辑
              </Button>
              <Popconfirm
                title="确定删除该规则？"
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
