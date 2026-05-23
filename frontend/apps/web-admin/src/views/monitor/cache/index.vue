<script lang="ts" setup>
import {ref} from 'vue';

import {Page} from '@vben/common-ui';

import {
  Button,
  Card,
  Col,
  Descriptions,
  DescriptionsItem,
  Input,
  message,
  Popconfirm,
  Row,
  Space,
  Table,
} from 'ant-design-vue';

import {deleteCacheKey, getCacheInfo, getCacheKeys} from '#/api/monitor/cache';

defineOptions({ name: 'MonitorCache' });

const cacheInfo = ref<Record<string, any>>({});
const keyList = ref<any[]>([]);
const infoLoading = ref(false);
const keysLoading = ref(false);
const keyPattern = ref('*');

async function loadInfo() {
  infoLoading.value = true;
  try {
    cacheInfo.value = (await getCacheInfo()) as Record<string, any>;
  } finally {
    infoLoading.value = false;
  }
}

async function loadKeys() {
  keysLoading.value = true;
  try {
    const res = (await getCacheKeys({ pattern: keyPattern.value })) as any;
    keyList.value = Array.isArray(res)
      ? res.map((k: string, i: number) => ({ index: i + 1, key: k }))
      : [];
  } finally {
    keysLoading.value = false;
  }
}

loadInfo();

async function handleDeleteKey(key: string) {
  await deleteCacheKey(key);
  message.success('删除成功');
  await loadKeys();
}
</script>

<template>
  <Page auto-content-height>
    <div class="p-4">
      <Row :gutter="16">
        <Col :span="24" class="mb-4">
          <Card :loading="infoLoading" title="Redis 信息">
            <template #extra>
              <Button size="small" @click="loadInfo">刷新</Button>
            </template>
            <Descriptions :column="3" bordered size="small">
              <DescriptionsItem label="版本">
                {{ cacheInfo.redis_version ?? '-' }}
              </DescriptionsItem>
              <DescriptionsItem label="已用内存">
                {{ cacheInfo.used_memory_human ?? '-' }}
              </DescriptionsItem>
              <DescriptionsItem label="连接数">
                {{ cacheInfo.connected_clients ?? '-' }}
              </DescriptionsItem>
              <DescriptionsItem label="运行天数">
                {{ cacheInfo.uptime_in_days ?? '-' }} 天
              </DescriptionsItem>
              <DescriptionsItem label="Key 数量">
                {{ cacheInfo.dbSize ?? '-' }}
              </DescriptionsItem>
              <DescriptionsItem label="命中率">
                {{ cacheInfo.hit_rate ?? '-' }}
              </DescriptionsItem>
            </Descriptions>
          </Card>
        </Col>
        <Col :span="24">
          <Card title="Key 管理">
            <div class="mb-3 flex items-center gap-2">
              <Input
                v-model:value="keyPattern"
                placeholder="匹配模式，如 saas:*"
                style="width: 300px"
                @press-enter="loadKeys"
              />
              <Button type="primary" @click="loadKeys">搜索</Button>
            </div>
            <Table
              :columns="[
                { title: '#', dataIndex: 'index', width: 60 },
                { title: 'Key', dataIndex: 'key', key: 'key', ellipsis: true },
                { title: '操作', key: 'action', width: 100 },
              ]"
              :data-source="keyList"
              :loading="keysLoading"
              :pagination="{ pageSize: 20, showSizeChanger: true }"
              row-key="key"
              size="small"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'action'">
                  <Popconfirm
                    title="确定删除该Key？"
                    @confirm="handleDeleteKey(record.key)"
                  >
                    <Button danger size="small" type="link">删除</Button>
                  </Popconfirm>
                </template>
              </template>
            </Table>
          </Card>
        </Col>
      </Row>
    </div>
  </Page>
</template>
