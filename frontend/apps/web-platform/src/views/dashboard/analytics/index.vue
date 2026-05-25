<script lang="ts" setup>
import type { AnalysisOverviewItem } from '@vben/common-ui';
import type {
  OverviewData,
  PackageDistItem,
  TopTenantItem,
  TrendItem,
} from '#/api/platform/dashboard';

import { onMounted, ref } from 'vue';

import {
  AnalysisChartCard,
  AnalysisChartsTabs,
  AnalysisOverview,
} from '@vben/common-ui';
import type { TabOption } from '@vben/types';
import {
  SvgBellIcon,
  SvgCakeIcon,
  SvgCardIcon,
  SvgDownloadIcon,
} from '@vben/icons';

import {
  getDashboardOverview,
  getPackageDistribution,
  getTenantTrend,
  getTopTenants,
} from '#/api/platform/dashboard';

import AnalyticsPackageDist from './analytics-package-dist.vue';
import AnalyticsTopTenants from './analytics-top-tenants.vue';
import AnalyticsTrends from './analytics-trends.vue';

const overviewItems = ref<AnalysisOverviewItem[]>([]);
const trendData = ref<TrendItem[]>([]);
const packageDistData = ref<PackageDistItem[]>([]);
const topTenantsData = ref<TopTenantItem[]>([]);

const chartTabs: TabOption[] = [
  { label: '租户注册趋势', value: 'trends' },
  { label: '套餐分布', value: 'package' },
];

function buildOverviewItems(data: OverviewData): AnalysisOverviewItem[] {
  return [
    {
      icon: SvgCardIcon,
      title: '本月新增',
      totalTitle: '租户总数',
      totalValue: data.totalTenants,
      value: data.newTenantsThisMonth,
    },
    {
      icon: SvgCakeIcon,
      title: '活跃租户',
      totalTitle: '租户总数',
      totalValue: data.totalTenants,
      value: data.activeTenants,
    },
    {
      icon: SvgDownloadIcon,
      title: '总收入(元)',
      totalTitle: '总收入',
      totalValue: data.totalRevenue,
      value: data.totalRevenue,
    },
    {
      icon: SvgBellIcon,
      title: '租户总数',
      totalTitle: '活跃占比(%)',
      totalValue:
        data.totalTenants > 0
          ? Math.round((data.activeTenants / data.totalTenants) * 100)
          : 0,
      value: data.totalTenants,
    },
  ];
}

onMounted(async () => {
  const [overview, trend, dist, top] = await Promise.all([
    getDashboardOverview(),
    getTenantTrend(30),
    getPackageDistribution(),
    getTopTenants(10),
  ]);
  overviewItems.value = buildOverviewItems(overview as OverviewData);
  trendData.value = (trend as TrendItem[]) || [];
  packageDistData.value = (dist as PackageDistItem[]) || [];
  topTenantsData.value = (top as TopTenantItem[]) || [];
});
</script>

<template>
  <div class="p-5">
    <AnalysisOverview :items="overviewItems" />

    <AnalysisChartsTabs :tabs="chartTabs" class="mt-5">
      <template #trends>
        <AnalyticsTrends :data="trendData" />
      </template>
      <template #package>
        <AnalyticsPackageDist :data="packageDistData" />
      </template>
    </AnalysisChartsTabs>

    <div class="mt-5 w-full">
      <AnalysisChartCard title="活跃度 TOP 租户">
        <AnalyticsTopTenants :data="topTenantsData" />
      </AnalysisChartCard>
    </div>
  </div>
</template>
