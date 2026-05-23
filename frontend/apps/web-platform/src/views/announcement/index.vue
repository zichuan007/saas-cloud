<script lang="ts" setup>
import {ref} from 'vue';

import {Page, useVbenModal} from '@vben/common-ui';

import {Button, message, Space, Tag} from 'ant-design-vue';

import {
  type AnnouncementRecord,
  getAnnouncementList,
  offlineAnnouncement,
  publishAnnouncement,
} from '#/api/platform/announcement';

import AnnouncementFormModal from './announcement-form-modal.vue';

defineOptions({ name: 'PlatformAnnouncement' });

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: AnnouncementFormModal,
});

const dataList = ref<AnnouncementRecord[]>([]);
const loading = ref(false);

async function loadData() {
  loading.value = true;
  try {
    dataList.value = (await getAnnouncementList()) as AnnouncementRecord[];
  } finally {
    loading.value = false;
  }
}
loadData();

function handleAdd() {
  formModalApi.setData({ mode: 'add' });
  formModalApi.open();
}

function handleEdit(row: AnnouncementRecord) {
  formModalApi.setData({ id: row.id, mode: 'edit', record: row });
  formModalApi.open();
}

async function handlePublish(id: number) {
  await publishAnnouncement(id);
  message.success('发布成功');
  await loadData();
}

async function handleOffline(id: number) {
  await offlineAnnouncement(id);
  message.success('已下线');
  await loadData();
}
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="loadData" />
    <div class="p-4">
      <div class="mb-4 flex justify-between">
        <h3 class="text-lg font-medium">系统公告</h3>
        <Button type="primary" @click="handleAdd">新建公告</Button>
      </div>
      <a-table
        :columns="[
          { title: '标题', dataIndex: 'title', key: 'title', minWidth: 200 },
          { title: '状态', key: 'status', width: 100 },
          { title: '发布时间', dataIndex: 'publishTime', key: 'publishTime', width: 180 },
          { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
          { title: '操作', key: 'action', width: 220, fixed: 'right' },
        ]"
        :data-source="dataList"
        :loading="loading"
        :pagination="false"
        row-key="id"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <Tag v-if="record.status === 1" color="green">已发布</Tag>
            <Tag v-else-if="record.status === 2" color="default">已下线</Tag>
            <Tag v-else color="blue">草稿</Tag>
          </template>
          <template v-if="column.key === 'action'">
            <Space>
              <Button size="small" type="link" @click="handleEdit(record)">
                编辑
              </Button>
              <Button
                v-if="record.status !== 1"
                size="small"
                type="link"
                @click="handlePublish(record.id)"
              >
                发布
              </Button>
              <Button
                v-if="record.status === 1"
                size="small"
                type="link"
                @click="handleOffline(record.id)"
              >
                下线
              </Button>
            </Space>
          </template>
        </template>
      </a-table>
    </div>
  </Page>
</template>
