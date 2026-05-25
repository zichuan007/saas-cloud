<script lang="ts" setup>
import type { EchartsUIType } from '@vben/plugins/echarts';
import { EchartsUI, useEcharts } from '@vben/plugins/echarts';

import { ref, watch } from 'vue';

interface PackageDistItem {
  count: number;
  packageName: string;
}

const props = defineProps<{ data: PackageDistItem[] }>();

const chartRef = ref<EchartsUIType>();
const { renderEcharts } = useEcharts(chartRef);

watch(
  () => props.data,
  (items) => {
    if (!items || items.length === 0) return;
    renderEcharts({
      legend: {
        bottom: '2%',
        left: 'center',
      },
      series: [
        {
          animationEasing: 'exponentialInOut',
          animationType: 'scale',
          avoidLabelOverlap: false,
          color: ['#5ab1ef', '#b6a2de', '#67e0e3', '#2ec7c9', '#f5994e'],
          data: items.map((i) => ({ name: i.packageName, value: i.count })),
          emphasis: {
            label: { fontSize: '12', fontWeight: 'bold', show: true },
          },
          itemStyle: { borderRadius: 10, borderWidth: 2 },
          label: { position: 'center', show: false },
          labelLine: { show: false },
          name: '套餐分布',
          radius: ['40%', '65%'],
          type: 'pie',
        },
      ],
      tooltip: { trigger: 'item' },
    });
  },
  { immediate: true },
);
</script>

<template>
  <EchartsUI ref="chartRef" />
</template>
