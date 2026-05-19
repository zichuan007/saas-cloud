<script lang="ts" setup>
import { ref } from 'vue';

import { Page, useVbenModal } from '@vben/common-ui';

import { Button, message, Space, Switch, Table, Tag } from 'ant-design-vue';

import { requestClient } from '#/api/request';

import ChannelFormModal from './channel-form-modal.vue';

defineOptions({ name: 'NotifyChannel' });

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: ChannelFormModal,
});

const dataList = ref<any[]>([]);
const loading = ref(false);

async function loadData() {
  loading.value = true;
  try {
    dataList.value = (await requestClient.get('/notify/channel/list')) as any[];
  } finally {
    loading.value = false;
  }
}
loadData();

function handleEdit(row: any) {
  formModalApi.setData({ record: row });
  formModalApi.open();
}

async function handleToggle(row: any, checked: boolean) {
  await requestClient.put(`/notify/channel/${row.channelType}`, {
    enabled: checked ? 1 : 0,
    configJson: row.configJson,
  });
  message.success(checked ? '已启用' : '已禁用');
  await loadData();
}
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="loadData" />
    <div class="p-4">
      <div class="mb-4">
        <h3 class="text-lg font-medium">渠道配置</h3>
      </div>
      <Table
        :columns="[
          { title: '渠道类型', key: 'channelTypeDesc', width: 160 },
          { title: '渠道描述', dataIndex: 'channelTypeDesc', key: 'desc' },
          { title: '状态', key: 'enabled', width: 100 },
          { title: '操作', key: 'action', width: 160, fixed: 'right' },
        ]"
        :data-source="dataList"
        :loading="loading"
        :pagination="false"
        row-key="channelType"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'channelTypeDesc'">
            <Tag color="blue">{{ record.channelTypeDesc }}</Tag>
          </template>
          <template v-if="column.key === 'enabled'">
            <Switch
              :checked="record.enabled === 1"
              checked-children="启用"
              un-checked-children="禁用"
              @change="(c: any) => handleToggle(record, !!c)"
            />
          </template>
          <template v-if="column.key === 'action'">
            <Space>
              <Button size="small" type="link" @click="handleEdit(record)">
                配置
              </Button>
            </Space>
          </template>
        </template>
      </Table>
    </div>
  </Page>
</template>
