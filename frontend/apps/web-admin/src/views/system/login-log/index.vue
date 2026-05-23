<script lang="ts" setup>
import {ref} from 'vue';

import {Page} from '@vben/common-ui';

import {Button, Input, message, Popconfirm, Table, Tag} from 'ant-design-vue';

import {cleanLoginLogs, getLoginLogPage} from '#/api/system/login-log';

defineOptions({ name: 'SystemLoginLog' });

const dataList = ref<any[]>([]);
const loading = ref(false);
const keyword = ref('');
const pagination = ref({ current: 1, pageSize: 10, total: 0 });

async function loadData() {
  loading.value = true;
  try {
    const res = (await getLoginLogPage({
      pageNum: pagination.value.current,
      pageSize: pagination.value.pageSize,
      username: keyword.value || undefined,
    })) as any;
    dataList.value = res?.records ?? [];
    pagination.value.total = res?.total ?? 0;
  } finally {
    loading.value = false;
  }
}
loadData();

function handlePageChange(page: number, pageSize: number) {
  pagination.value.current = page;
  pagination.value.pageSize = pageSize;
  loadData();
}

function handleSearch() {
  pagination.value.current = 1;
  loadData();
}

async function handleClean() {
  const count = await cleanLoginLogs(90);
  message.success(`已清理 ${count} 条日志`);
  await loadData();
}
</script>

<template>
  <Page auto-content-height>
    <div class="p-4">
      <div class="mb-4 flex items-center justify-between">
        <h3 class="text-lg font-medium">登录日志</h3>
        <div class="flex items-center gap-2">
          <Input.Search
            v-model:value="keyword"
            allow-clear
            placeholder="搜索用户名"
            style="width: 200px"
            @search="handleSearch"
          />
          <Popconfirm title="确定清理90天前的日志？" @confirm="handleClean">
            <Button danger>清理日志</Button>
          </Popconfirm>
        </div>
      </div>
      <Table
        :columns="[
          { title: '用户名', dataIndex: 'username', key: 'username', width: 120 },
          { title: 'IP地址', dataIndex: 'ip', key: 'ip', width: 140 },
          { title: '归属地', dataIndex: 'location', key: 'location', width: 140 },
          { title: '浏览器', dataIndex: 'browser', key: 'browser', width: 120 },
          { title: '操作系统', dataIndex: 'os', key: 'os', width: 140 },
          { title: '状态', key: 'status', width: 80 },
          { title: '提示信息', dataIndex: 'message', key: 'message', ellipsis: true },
          { title: '登录时间', dataIndex: 'loginTime', key: 'loginTime', width: 180 },
        ]"
        :data-source="dataList"
        :loading="loading"
        :pagination="{
          current: pagination.current,
          pageSize: pagination.pageSize,
          total: pagination.total,
          showSizeChanger: true,
          showTotal: (t: number) => `共 ${t} 条`,
          onChange: handlePageChange,
        }"
        row-key="id"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <Tag :color="record.status === 1 ? 'green' : 'red'">
              {{ record.status === 1 ? '成功' : '失败' }}
            </Tag>
          </template>
        </template>
      </Table>
    </div>
  </Page>
</template>
