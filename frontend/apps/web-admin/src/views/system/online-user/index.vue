<script lang="ts" setup>
import {ref} from 'vue';

import {Page} from '@vben/common-ui';

import {Button, message, Popconfirm, Space, Table} from 'ant-design-vue';

import {getOnlineUserList, kickOnlineUser} from '#/api/system/online-user';

defineOptions({ name: 'SystemOnlineUser' });

const dataList = ref<any[]>([]);
const loading = ref(false);

async function loadData() {
  loading.value = true;
  try {
    const res = (await getOnlineUserList()) as any;
    dataList.value = Array.isArray(res) ? res : res?.records ?? [];
  } finally {
    loading.value = false;
  }
}
loadData();

async function handleKick(tokenValue: string) {
  await kickOnlineUser(tokenValue);
  message.success('已强制下线');
  await loadData();
}
</script>

<template>
  <Page auto-content-height>
    <div class="p-4">
      <div class="mb-4 flex items-center justify-between">
        <h3 class="text-lg font-medium">在线用户</h3>
        <Button @click="loadData">刷新</Button>
      </div>
      <Table
        :columns="[
          { title: '用户名', dataIndex: 'username', key: 'username', width: 120 },
          { title: 'IP地址', dataIndex: 'ip', key: 'ip', width: 140 },
          { title: '浏览器', dataIndex: 'browser', key: 'browser', width: 140 },
          { title: '操作系统', dataIndex: 'os', key: 'os', width: 160 },
          { title: '登录时间', dataIndex: 'loginTime', key: 'loginTime', width: 180 },
          { title: '操作', key: 'action', width: 100, fixed: 'right' },
        ]"
        :data-source="dataList"
        :loading="loading"
        :pagination="false"
        row-key="tokenValue"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <Popconfirm
              title="确定强制下线该用户？"
              @confirm="handleKick(record.tokenValue)"
            >
              <Button danger size="small" type="link">强制下线</Button>
            </Popconfirm>
          </template>
        </template>
      </Table>
    </div>
  </Page>
</template>
