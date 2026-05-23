<script lang="ts" setup>
import {ref} from 'vue';

import {Page} from '@vben/common-ui';

import {
  Button,
  Card,
  Col,
  Descriptions,
  DescriptionsItem,
  Progress,
  Row,
} from 'ant-design-vue';

import {getServerInfo} from '#/api/monitor/server';

defineOptions({ name: 'MonitorServer' });

const serverInfo = ref<Record<string, any>>({});
const loading = ref(false);

async function loadData() {
  loading.value = true;
  try {
    serverInfo.value = (await getServerInfo()) as Record<string, any>;
  } finally {
    loading.value = false;
  }
}
loadData();

function formatBytes(bytes?: number) {
  if (!bytes) return '-';
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  if (bytes < 1024 * 1024 * 1024)
    return `${(bytes / 1024 / 1024).toFixed(1)} MB`;
  return `${(bytes / 1024 / 1024 / 1024).toFixed(2)} GB`;
}
</script>

<template>
  <Page auto-content-height>
    <div class="p-4">
      <div class="mb-4 flex items-center justify-between">
        <h3 class="text-lg font-medium">服务器监控</h3>
        <Button :loading="loading" @click="loadData">刷新</Button>
      </div>
      <Row :gutter="16">
        <Col :span="8" class="mb-4">
          <Card title="CPU">
            <div class="flex flex-col items-center">
              <Progress
                :percent="serverInfo.cpu?.usagePercent ?? 0"
                :stroke-color="
                  (serverInfo.cpu?.usagePercent ?? 0) > 80
                    ? '#ff4d4f'
                    : '#1890ff'
                "
                type="circle"
              />
              <div class="mt-2 text-gray-500">
                核心数: {{ serverInfo.cpu?.cores ?? '-' }}
              </div>
            </div>
          </Card>
        </Col>
        <Col :span="8" class="mb-4">
          <Card title="内存">
            <div class="flex flex-col items-center">
              <Progress
                :percent="serverInfo.memory?.usagePercent ?? 0"
                :stroke-color="
                  (serverInfo.memory?.usagePercent ?? 0) > 80
                    ? '#ff4d4f'
                    : '#52c41a'
                "
                type="circle"
              />
              <div class="mt-2 text-gray-500">
                {{ formatBytes(serverInfo.memory?.used) }} /
                {{ formatBytes(serverInfo.memory?.total) }}
              </div>
            </div>
          </Card>
        </Col>
        <Col :span="8" class="mb-4">
          <Card title="JVM">
            <div class="flex flex-col items-center">
              <Progress
                :percent="serverInfo.jvm?.usagePercent ?? 0"
                :stroke-color="
                  (serverInfo.jvm?.usagePercent ?? 0) > 80
                    ? '#ff4d4f'
                    : '#faad14'
                "
                type="circle"
              />
              <div class="mt-2 text-gray-500">
                {{ formatBytes(serverInfo.jvm?.used) }} /
                {{ formatBytes(serverInfo.jvm?.max) }}
              </div>
            </div>
          </Card>
        </Col>
        <Col :span="24" class="mb-4">
          <Card title="磁盘信息">
            <Descriptions :column="2" bordered size="small">
              <template
                v-for="(disk, index) in serverInfo.disks ?? []"
                :key="index"
              >
                <DescriptionsItem :label="`${disk.mount} 总容量`">
                  {{ formatBytes(disk.total) }}
                </DescriptionsItem>
                <DescriptionsItem :label="`${disk.mount} 使用率`">
                  <Progress
                    :percent="disk.usagePercent ?? 0"
                    :stroke-color="
                      (disk.usagePercent ?? 0) > 90 ? '#ff4d4f' : '#1890ff'
                    "
                    size="small"
                  />
                </DescriptionsItem>
              </template>
            </Descriptions>
          </Card>
        </Col>
        <Col :span="24">
          <Card title="系统信息">
            <Descriptions :column="2" bordered size="small">
              <DescriptionsItem label="操作系统">
                {{ serverInfo.os?.name ?? '-' }}
              </DescriptionsItem>
              <DescriptionsItem label="系统架构">
                {{ serverInfo.os?.arch ?? '-' }}
              </DescriptionsItem>
              <DescriptionsItem label="JVM版本">
                {{ serverInfo.jvm?.version ?? '-' }}
              </DescriptionsItem>
              <DescriptionsItem label="JVM启动时间">
                {{ serverInfo.jvm?.startTime ?? '-' }}
              </DescriptionsItem>
            </Descriptions>
          </Card>
        </Col>
      </Row>
    </div>
  </Page>
</template>
