<script lang="ts" setup>
import {ref} from 'vue';

import {Page, useVbenModal} from '@vben/common-ui';

import {Button, Input, message, Popconfirm, Space, Switch, Table, Tag} from 'ant-design-vue';

import {deletePost, getPostList, type PostRecord} from '#/api/system/post';

import PostFormModal from './post-form-modal.vue';

defineOptions({ name: 'SystemPost' });

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: PostFormModal,
});

const dataList = ref<PostRecord[]>([]);
const loading = ref(false);
const keyword = ref('');
const pagination = ref({ current: 1, pageSize: 10, total: 0 });

async function loadData() {
  loading.value = true;
  try {
    const res = (await getPostList({
      pageNum: pagination.value.current,
      pageSize: pagination.value.pageSize,
      postName: keyword.value || undefined,
    })) as any;
    dataList.value = res?.records ?? [];
    pagination.value.total = res?.total ?? 0;
  } finally {
    loading.value = false;
  }
}
loadData();

function handleAdd() {
  formModalApi.setData({ mode: 'add' });
  formModalApi.open();
}

function handleEdit(row: PostRecord) {
  formModalApi.setData({ id: row.id, mode: 'edit' });
  formModalApi.open();
}

async function handleDelete(id: number) {
  await deletePost(id);
  message.success('删除成功');
  await loadData();
}

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
    <FormModal @success="loadData" />
    <div class="p-4">
      <div class="mb-4 flex items-center justify-between">
        <h3 class="text-lg font-medium">岗位管理</h3>
        <Space>
          <Input.Search
            v-model:value="keyword"
            allow-clear
            placeholder="搜索岗位名称"
            style="width: 200px"
            @search="handleSearch"
          />
          <Button type="primary" @click="handleAdd">新增岗位</Button>
        </Space>
      </div>
      <Table
        :columns="[
          { title: '岗位编码', dataIndex: 'postCode', key: 'postCode', width: 150 },
          { title: '岗位名称', dataIndex: 'postName', key: 'postName' },
          { title: '排序', dataIndex: 'sortOrder', key: 'sortOrder', width: 80 },
          { title: '状态', key: 'status', width: 100 },
          { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
          { title: '操作', key: 'action', width: 160, fixed: 'right' },
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
              {{ record.status === 1 ? '启用' : '禁用' }}
            </Tag>
          </template>
          <template v-if="column.key === 'action'">
            <Space>
              <Button size="small" type="link" @click="handleEdit(record as PostRecord)">
                编辑
              </Button>
              <Popconfirm
                title="确定删除该岗位？"
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
