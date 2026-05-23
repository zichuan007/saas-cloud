<script lang="ts" setup>
import {ref} from 'vue';

import {Page} from '@vben/common-ui';

import {
  Card,
  Col,
  Descriptions,
  DescriptionsItem,
  Progress,
  Row,
  Statistic,
} from 'ant-design-vue';

import {getTenantQuota, getTenantSelfInfo} from '#/api/system/tenant-self';

defineOptions({ name: 'SystemTenantSelf' });

const tenantInfo = ref<Record<string, any>>({});
const quota = ref<Record<string, any>>({});
const loading = ref(false);

async function loadData() {
  loading.value = true;
  try {
    const [info, q] = await Promise.all([
      getTenantSelfInfo(),
      getTenantQuota(),
    ]);
    tenantInfo.value = (info as Record<string, any>) ?? {};
    quota.value = (q as Record<string, any>) ?? {};
  } finally {
    loading.value = false;
  }
}
loadData();

function calcPercent(used?: number, total?: number) {
  if (!total || total <= 0) return 0;
  return Math.round(((used ?? 0) / total) * 100);
}
</script>

<template>
  <Page auto-content-height>
    <div class="p-4">
      <Row :gutter="16">
        <Col :span="24" class="mb-4">
          <Card :loading="loading" title="租户信息">
            <Descriptions :column="2" bordered size="small">
              <DescriptionsItem label="租户名称">
                {{ tenantInfo.tenantName ?? '-' }}
              </DescriptionsItem>
              <DescriptionsItem label="租户编码">
                {{ tenantInfo.tenantCode ?? '-' }}
              </DescriptionsItem>
              <DescriptionsItem label="套餐名称">
                {{ tenantInfo.packageName ?? '-' }}
              </DescriptionsItem>
              <DescriptionsItem label="到期时间">
                {{ tenantInfo.expireTime ?? '永久' }}
              </DescriptionsItem>
              <DescriptionsItem label="联系人">
                {{ tenantInfo.contactPerson ?? '-' }}
              </DescriptionsItem>
              <DescriptionsItem label="联系电话">
                {{ tenantInfo.contactPhone ?? '-' }}
              </DescriptionsItem>
            </Descriptions>
          </Card>
        </Col>
        <Col :span="6">
          <Card>
            <Statistic
              :value="quota.userUsed ?? 0"
              :suffix="`/ ${quota.userLimit ?? '不限'}`"
              title="用户数"
            />
            <Progress
              v-if="quota.userLimit"
              :percent="calcPercent(quota.userUsed, quota.userLimit)"
              size="small"
              class="mt-2"
            />
          </Card>
        </Col>
        <Col :span="6">
          <Card>
            <Statistic
              :value="quota.roleUsed ?? 0"
              :suffix="`/ ${quota.roleLimit ?? '不限'}`"
              title="角色数"
            />
            <Progress
              v-if="quota.roleLimit"
              :percent="calcPercent(quota.roleUsed, quota.roleLimit)"
              size="small"
              class="mt-2"
            />
          </Card>
        </Col>
        <Col :span="6">
          <Card>
            <Statistic
              :value="quota.deptUsed ?? 0"
              :suffix="`/ ${quota.deptLimit ?? '不限'}`"
              title="部门数"
            />
            <Progress
              v-if="quota.deptLimit"
              :percent="calcPercent(quota.deptUsed, quota.deptLimit)"
              size="small"
              class="mt-2"
            />
          </Card>
        </Col>
        <Col :span="6">
          <Card>
            <Statistic
              :value="quota.processUsed ?? 0"
              :suffix="`/ ${quota.processLimit ?? '不限'}`"
              title="流程数"
            />
            <Progress
              v-if="quota.processLimit"
              :percent="calcPercent(quota.processUsed, quota.processLimit)"
              size="small"
              class="mt-2"
            />
          </Card>
        </Col>
      </Row>
    </div>
  </Page>
</template>
