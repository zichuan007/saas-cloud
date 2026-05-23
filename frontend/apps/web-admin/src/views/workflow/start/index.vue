<script lang="ts" setup>
import {ref} from 'vue';

import {Page, useVbenModal} from '@vben/common-ui';

import {Card, Col, Empty, Row} from 'ant-design-vue';

import {getStartableList} from '#/api/workflow/process';

import StartProcessModal from './start-process-modal.vue';

defineOptions({ name: 'WorkflowStart' });

const [ProcessModal, processModalApi] = useVbenModal({
  connectedComponent: StartProcessModal,
});

const processList = ref<any[]>([]);
const loading = ref(false);

async function loadData() {
  loading.value = true;
  try {
    processList.value = (await getStartableList()) as any[];
  } finally {
    loading.value = false;
  }
}
loadData();

function handleStart(item: any) {
  processModalApi.setData({
    processKey: item.processKey,
    processName: item.processName,
  });
  processModalApi.open();
}
</script>

<template>
  <Page auto-content-height>
    <ProcessModal @success="loadData" />
    <div class="p-4">
      <h3 class="mb-4 text-lg font-medium">发起流程</h3>
      <Empty v-if="!loading && processList.length === 0" description="暂无可发起的流程" />
      <Row :gutter="[16, 16]">
        <Col v-for="item in processList" :key="item.processKey" :span="6">
          <Card hoverable @click="handleStart(item)">
            <div class="text-center">
              <div class="text-primary mb-2 text-2xl">
                <span class="i-ant-design:file-text-outlined" />
              </div>
              <div class="font-medium">{{ item.processName }}</div>
              <div class="mt-1 text-xs text-gray-400">
                {{ item.remark || '点击发起' }}
              </div>
            </div>
          </Card>
        </Col>
      </Row>
    </div>
  </Page>
</template>
