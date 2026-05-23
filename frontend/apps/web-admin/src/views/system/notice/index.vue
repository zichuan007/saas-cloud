<script lang="ts" setup>
import {ref} from 'vue';

import {Page, useVbenModal} from '@vben/common-ui';

import {
  Button,
  Input,
  message,
  Popconfirm,
  Space,
  Table,
  Tag,
} from 'ant-design-vue';

import {
  deleteNotice,
  getNoticeList,
  type NoticeRecord,
  publishNotice,
  revokeNotice,
} from '#/api/system/notice';

import NoticeFormModal from './notice-form-modal.vue';

defineOptions({ name: 'SystemNotice' });

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: NoticeFormModal,
});

const dataList = ref<NoticeRecord[]>([]);
const loading = ref(false);
const keyword = ref('');
const pagination = ref({ current: 1, pageSize: 10, total: 0 });

const typeMap: Record<number, { color: string; label: string }> = {
  1: { color: 'blue', label: '通知' },
  2: { color: 'orange', label: '公告' },
};
const statusMap: Record<number, { color: string; label: string }> = {
  0: { color: 'default', label: '草稿' },
  1: { color: 'green', label: '已发布' },
  2: { color: 'red', label: '已撤回' },
};

async function loadData() {
  loading.value = true;
  try {
    const res = (await getNoticeList({
      pageNum: pagination.value.current,
      pageSize: pagination.value.pageSize,
      title: keyword.value || undefined,
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

function handleEdit(row: NoticeRecord) {
  formModalApi.setData({ mode: 'edit', record: row });
  formModalApi.open();
}

async function handlePublish(id: number) {
  await publishNotice(id);
  message.success('发布成功');
  await loadData();
}

async function handleRevoke(id: number) {
  await revokeNotice(id);
  message.success('撤回成功');
  await loadData();
}

async function handleDelete(id: number) {
  await deleteNotice(id);
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
        <h3 class="text-lg font-medium">通知公告</h3>
        <Space>
          <Input.Search
            v-model:value="keyword"
            allow-clear
            placeholder="搜索标题"
            style="width: 200px"
            @search="handleSearch"
          />
          <Button type="primary" @click="handleAdd">新增公告</Button>
        </Space>
      </div>
      <Table
        :columns="[
          { title: '标题', dataIndex: 'title', key: 'title', ellipsis: true },
          { title: '类型', key: 'noticeType', width: 100 },
          { title: '状态', key: 'status', width: 100 },
          { title: '发布时间', dataIndex: 'publishTime', key: 'publishTime', width: 180 },
          { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
          { title: '操作', key: 'action', width: 240, fixed: 'right' },
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
          <template v-if="column.key === 'noticeType'">
            <Tag :color="typeMap[record.noticeType as number]?.color ?? 'default'">
              {{ typeMap[record.noticeType as number]?.label ?? '未知' }}
            </Tag>
          </template>
          <template v-if="column.key === 'status'">
            <Tag :color="statusMap[record.status as number]?.color ?? 'default'">
              {{ statusMap[record.status as number]?.label ?? '未知' }}
            </Tag>
          </template>
          <template v-if="column.key === 'action'">
            <Space>
              <Button
                v-if="record.status === 0"
                size="small"
                type="link"
                @click="handleEdit(record as NoticeRecord)"
              >
                编辑
              </Button>
              <Popconfirm
                v-if="record.status === 0"
                title="确定发布？"
                @confirm="handlePublish(record.id)"
              >
                <Button size="small" type="link">发布</Button>
              </Popconfirm>
              <Popconfirm
                v-if="record.status === 1"
                title="确定撤回？"
                @confirm="handleRevoke(record.id)"
              >
                <Button size="small" type="link">撤回</Button>
              </Popconfirm>
              <Popconfirm
                title="确定删除？"
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
