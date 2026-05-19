<script lang="ts" setup>
import { ref } from 'vue';

import { Page } from '@vben/common-ui';

import { Input, Table, Tag } from 'ant-design-vue';

import { requestClient } from '#/api/request';

defineOptions({ name: 'SystemLog' });

const dataList = ref<any[]>([]);
const loading = ref(false);
const pagination = ref({ current: 1, pageSize: 10, total: 0 });
const keyword = ref('');

async function loadData() {
  loading.value = true;
  try {
    const res = (await requestClient.get('/rbac/operation-log/list', {
      params: {
        module: keyword.value || undefined,
        pageNum: pagination.value.current,
        pageSize: pagination.value.pageSize,
      },
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
</script>

<template>
  <Page auto-content-height>
    <div class="p-4">
      <div class="mb-4 flex items-center justify-between">
        <h3 class="text-lg font-medium">操作日志</h3>
        <Input.Search
          v-model:value="keyword"
          allow-clear
          placeholder="搜索操作模块"
          style="width: 240px"
          @search="handleSearch"
        />
      </div>
      <Table
        :columns="[
          { title: '操作用户', dataIndex: 'username', key: 'username', width: 120 },
          { title: '操作模块', dataIndex: 'module', key: 'module', width: 120 },
          { title: '操作描述', dataIndex: 'operation', key: 'operation', width: 160 },
          { title: '请求方式', key: 'requestMethod', width: 100 },
          { title: '请求URL', dataIndex: 'requestUrl', key: 'requestUrl', ellipsis: true },
          { title: '状态', key: 'responseCode', width: 80 },
          { title: '耗时(ms)', dataIndex: 'duration', key: 'duration', width: 100 },
          { title: '操作时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
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
          <template v-if="column.key === 'requestMethod'">
            <Tag
              :color="record.requestMethod === 'GET' ? 'green' : record.requestMethod === 'DELETE' ? 'red' : 'blue'"
            >
              {{ record.requestMethod }}
            </Tag>
          </template>
          <template v-if="column.key === 'responseCode'">
            <Tag :color="record.responseCode === 200 ? 'green' : 'red'">
              {{ record.responseCode }}
            </Tag>
          </template>
        </template>
      </Table>
    </div>
  </Page>
</template>
