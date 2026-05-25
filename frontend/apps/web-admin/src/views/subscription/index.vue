<script lang="ts" setup>
import { onMounted, ref } from 'vue';

import { Page } from '@vben/common-ui';

import { Button, Card, Col, message, Modal, Row, Tag, Typography } from 'ant-design-vue';

import { type PackageInfo, getPackageList, subscribe } from '#/api/subscription';

defineOptions({ name: 'Subscription' });

const packages = ref<PackageInfo[]>([]);
const loading = ref(false);

async function loadPackages() {
  loading.value = true;
  try {
    packages.value = (await getPackageList()) as PackageInfo[];
  } finally {
    loading.value = false;
  }
}

onMounted(loadPackages);

function handleSubscribe(pkg: PackageInfo) {
  Modal.confirm({
    title: `订购 ${pkg.packageName}`,
    content: `确认订购 ${pkg.packageName}？月费 ¥${pkg.priceMonthly ?? 0}`,
    onOk: async () => {
      try {
        // tenantId 暂用 0，实际应从用户上下文获取
        await subscribe(0, {
          packageId: pkg.id,
          orderType: 0,
          months: 1,
          payChannel: 'manual',
        });
        message.success('订单已创建');
      } catch {
        message.error('订购失败');
      }
    },
  });
}
</script>

<template>
  <Page title="套餐订购">
    <Row :gutter="16">
      <Col v-for="pkg in packages" :key="pkg.id" :lg="6" :md="8" :sm="12" :xs="24">
        <Card :loading="loading" hoverable style="margin-bottom: 16px">
          <template #title>
            <Typography.Title :level="4" style="margin: 0">
              {{ pkg.packageName }}
            </Typography.Title>
          </template>
          <p>
            <Tag color="blue">¥{{ pkg.priceMonthly ?? 0 }}/月</Tag>
            <Tag color="green">¥{{ pkg.priceYearly ?? 0 }}/年</Tag>
          </p>
          <p>用户数: {{ pkg.maxUsers === 0 ? '不限' : pkg.maxUsers }}</p>
          <p>角色数: {{ pkg.maxRoles === 0 ? '不限' : pkg.maxRoles }}</p>
          <p>部门数: {{ pkg.maxDepts === 0 ? '不限' : pkg.maxDepts }}</p>
          <p>存储: {{ pkg.maxStorageMb === 0 ? '不限' : `${pkg.maxStorageMb}MB` }}</p>
          <template #actions>
            <Button type="primary" @click="handleSubscribe(pkg)">订购</Button>
          </template>
        </Card>
      </Col>
    </Row>
  </Page>
</template>
