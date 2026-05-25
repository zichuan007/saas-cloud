<script lang="ts" setup>
import type { EchartsUIType } from '@vben/plugins/echarts';
import { EchartsUI, useEcharts } from '@vben/plugins/echarts';

import { ref, watch } from 'vue';

interface TrendItem {
  count: number;
  date: string;
}

const props = defineProps<{ data: TrendItem[] }>();

const chartRef = ref<EchartsUIType>();
const { renderEcharts } = useEcharts(chartRef);

watch(
  () => props.data,
  (items) => {
    if (!items || items.length === 0) return;
    renderEcharts({
      grid: {
        bottom: 0,
        containLabel: true,
        left: '1%',
        right: '1%',
        top: '2%',
      },
      series: [
        {
          areaStyle: { opacity: 0.3 },
          data: items.map((i) => i.count),
          itemStyle: { color: '#5ab1ef' },
          name: '新增租户',
          smooth: true,
          type: 'line',
        },
      ],
      tooltip: {
        axisPointer: { lineStyle: { color: '#019680', width: 1 } },
        trigger: 'axis',
      },
      xAxis: {
        axisTick: { show: false },
        boundaryGap: false,
        data: items.map((i) => i.date.slice(5)),
        splitLine: {
          lineStyle: { type: 'solid', width: 1 },
          show: true,
        },
        type: 'category',
      },
      yAxis: [
        {
          axisTick: { show: false },
          minInterval: 1,
          splitArea: { show: true },
          splitNumber: 4,
          type: 'value',
        },
      ],
    });
  },
  { immediate: true },
);
</script>

<template>
  <EchartsUI ref="chartRef" />
</template>
