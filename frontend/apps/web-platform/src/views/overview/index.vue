<script lang="ts" setup>
import {onMounted, ref} from 'vue';

import {Page} from '@vben/common-ui';

import {Card, Col, Row, Statistic} from 'ant-design-vue';

import {getOverview, type OverviewData} from '#/api/platform/statistics';

defineOptions({ name: 'PlatformOverview' });

const data = ref<OverviewData>({
  activeTenants: 0,
  frozenTenants: 0,
  monthlyProcessInstances: 0,
  todayActiveUsers: 0,
  totalProcessInstances: 0,
  totalTenants: 0,
  totalUsers: 0,
  trialTenants: 0,
});

onMounted(async () => {
  data.value = (await getOverview()) as OverviewData;
});
</script>

<template>
  <Page auto-content-height>
    <div class="p-4">
      <h3 class="mb-4 text-lg font-medium">平台概览</h3>
      <Row :gutter="[16, 16]">
        <Col :span="6">
          <Card>
            <Statistic title="租户总数" :value="data.totalTenants" />
          </Card>
        </Col>
        <Col :span="6">
          <Card>
            <Statistic
              title="活跃租户"
              :value="data.activeTenants"
              :value-style="{ color: '#3f8600' }"
            />
          </Card>
        </Col>
        <Col :span="6">
          <Card>
            <Statistic title="试用租户" :value="data.trialTenants" />
          </Card>
        </Col>
        <Col :span="6">
          <Card>
            <Statistic
              title="已冻结"
              :value="data.frozenTenants"
              :value-style="{ color: '#cf1322' }"
            />
          </Card>
        </Col>
        <Col :span="6">
          <Card>
            <Statistic title="用户总数" :value="data.totalUsers" />
          </Card>
        </Col>
        <Col :span="6">
          <Card>
            <Statistic
              title="今日活跃"
              :value="data.todayActiveUsers"
              :value-style="{ color: '#3f8600' }"
            />
          </Card>
        </Col>
        <Col :span="6">
          <Card>
            <Statistic title="流程实例总数" :value="data.totalProcessInstances" />
          </Card>
        </Col>
        <Col :span="6">
          <Card>
            <Statistic title="本月流程发起" :value="data.monthlyProcessInstances" />
          </Card>
        </Col>
      </Row>
    </div>
  </Page>
</template>
