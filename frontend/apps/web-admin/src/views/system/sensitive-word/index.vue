<script lang="ts" setup>
import {ref} from 'vue';

import {Page} from '@vben/common-ui';

import {
  Button,
  Card,
  Col,
  Input,
  message,
  Popconfirm,
  Row,
  Space,
  Table,
  Tag,
} from 'ant-design-vue';

import {
  checkSensitiveText,
  createSensitiveWord,
  deleteSensitiveWord,
  filterSensitiveText,
  getSensitiveWordList,
} from '#/api/system/sensitive-word';

defineOptions({ name: 'SystemSensitiveWord' });

const dataList = ref<any[]>([]);
const loading = ref(false);
const pagination = ref({ current: 1, pageSize: 20, total: 0 });
const newWord = ref('');
const newCategory = ref('');
const checkText = ref('');
const checkResult = ref<any>(null);

async function loadData() {
  loading.value = true;
  try {
    const res = (await getSensitiveWordList({
      pageNum: pagination.value.current,
      pageSize: pagination.value.pageSize,
    })) as any;
    dataList.value = res?.records ?? (Array.isArray(res) ? res : []);
    pagination.value.total = res?.total ?? dataList.value.length;
  } finally {
    loading.value = false;
  }
}
loadData();

async function handleAdd() {
  if (!newWord.value.trim()) {
    message.warning('请输入敏感词');
    return;
  }
  await createSensitiveWord({
    category: newCategory.value || undefined,
    word: newWord.value.trim(),
  });
  message.success('添加成功');
  newWord.value = '';
  newCategory.value = '';
  await loadData();
}

async function handleDelete(id: number) {
  await deleteSensitiveWord(id);
  message.success('删除成功');
  await loadData();
}

async function handleCheck() {
  if (!checkText.value.trim()) return;
  checkResult.value = await checkSensitiveText(checkText.value);
}

async function handleFilter() {
  if (!checkText.value.trim()) return;
  const result = await filterSensitiveText(checkText.value);
  checkResult.value = { filtered: result };
}

function handlePageChange(page: number, pageSize: number) {
  pagination.value.current = page;
  pagination.value.pageSize = pageSize;
  loadData();
}
</script>

<template>
  <Page auto-content-height>
    <div class="p-4">
      <Row :gutter="16">
        <Col :span="16">
          <Card title="敏感词管理">
            <div class="mb-3 flex items-center gap-2">
              <Input
                v-model:value="newWord"
                placeholder="输入敏感词"
                style="width: 180px"
                @press-enter="handleAdd"
              />
              <Input
                v-model:value="newCategory"
                placeholder="分类（可选）"
                style="width: 140px"
              />
              <Button type="primary" @click="handleAdd">添加</Button>
            </div>
            <Table
              :columns="[
                { title: '敏感词', dataIndex: 'word', key: 'word' },
                { title: '分类', dataIndex: 'category', key: 'category', width: 120 },
                { title: '状态', key: 'status', width: 80 },
                { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
                { title: '操作', key: 'action', width: 80 },
              ]"
              :data-source="dataList"
              :loading="loading"
              :pagination="{
                current: pagination.current,
                pageSize: pagination.pageSize,
                total: pagination.total,
                showSizeChanger: true,
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
                  <Popconfirm
                    title="确定删除？"
                    @confirm="handleDelete(record.id)"
                  >
                    <Button danger size="small" type="link">删除</Button>
                  </Popconfirm>
                </template>
              </template>
            </Table>
          </Card>
        </Col>
        <Col :span="8">
          <Card title="在线检测">
            <Input.TextArea
              v-model:value="checkText"
              :rows="4"
              placeholder="输入待检测文本"
            />
            <Space class="mt-3">
              <Button type="primary" @click="handleCheck">检测</Button>
              <Button @click="handleFilter">过滤</Button>
            </Space>
            <div v-if="checkResult" class="mt-3 rounded bg-gray-50 p-3">
              <pre class="whitespace-pre-wrap text-sm">{{
                JSON.stringify(checkResult, null, 2)
              }}</pre>
            </div>
          </Card>
        </Col>
      </Row>
    </div>
  </Page>
</template>
