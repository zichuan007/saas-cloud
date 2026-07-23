<script setup lang="ts">
import type {
  AnalysisOverviewItem,
  WorkbenchQuickNavItem,
} from '@vben/common-ui';

import { computed } from 'vue';

import {
  AnalysisOverview,
  ParticleNetwork,
  TypeWriter,
  vFadeIn,
} from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';
import { usePreferences } from '@vben/preferences';

import { Card } from 'ant-design-vue';

import { useAuthStore } from '#/store';

defineOptions({ name: 'Dashboard' });

const authStore = useAuthStore();
const { isDark } = usePreferences();

const username = computed(
  () => authStore.userInfo?.realName || authStore.userInfo?.username || '',
);

const greeting = computed(() => {
  const hour = new Date().getHours();
  if (hour < 6) return '夜深了';
  if (hour < 12) return '早上好';
  if (hour < 14) return '中午好';
  if (hour < 18) return '下午好';
  return '晚上好';
});

const sloganTexts = computed(() => [
  `${greeting.value}，${username.value}，欢迎回来`,
  '高效管理，智慧运营',
]);

const overviewItems: AnalysisOverviewItem[] = [
  {
    icon: 'lucide:users',
    title: '用户总数',
    totalTitle: '本月新增',
    totalValue: 128,
    value: 2_860,
  },
  {
    icon: 'lucide:building-2',
    title: '租户总数',
    totalTitle: '活跃租户',
    totalValue: 45,
    value: 68,
  },
  {
    icon: 'lucide:file-check',
    title: '流程实例',
    totalTitle: '本月完成',
    totalValue: 320,
    value: 1_540,
  },
  {
    icon: 'lucide:bell',
    title: '消息通知',
    totalTitle: '本月发送',
    totalValue: 1_280,
    value: 5_620,
  },
];

const quickNavItems: WorkbenchQuickNavItem[] = [
  { color: '#1890ff', icon: 'lucide:user-plus', title: '新建用户' },
  { color: '#52c41a', icon: 'lucide:shield', title: '角色管理' },
  { color: '#722ed1', icon: 'lucide:layout-grid', title: '菜单管理' },
  { color: '#fa8c16', icon: 'lucide:building', title: '部门管理' },
  { color: '#eb2f96', icon: 'lucide:git-branch', title: '流程管理' },
  { color: '#13c2c2', icon: 'lucide:settings', title: '系统配置' },
];
</script>

<template>
  <div class="p-4">
    <!-- 欢迎横幅 -->
    <div
      class="relative mb-6 overflow-hidden rounded-xl bg-background-deep p-8 dark:bg-[#0a0a0f]"
    >
      <div class="absolute inset-0">
        <ParticleNetwork
          :interactive="true"
          :max-distance="100"
          :opacity="0.35"
          :particle-count="35"
          :speed="0.3"
        />
      </div>
      <div class="relative z-10">
        <h1 class="text-2xl font-bold text-foreground">
          <TypeWriter
            :loop="true"
            :pause-duration="4000"
            :texts="sloganTexts"
            :type-speed="80"
          />
        </h1>
        <p class="mt-2 text-muted-foreground">
          今天是 {{ new Date().toLocaleDateString('zh-CN', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' }) }}
        </p>
      </div>
    </div>

    <!-- 统计概览 -->
    <div v-fade-in="0">
      <AnalysisOverview :items="overviewItems" />
    </div>

    <!-- 快捷导航 -->
    <div v-fade-in="200" class="mt-6 grid grid-cols-1 gap-4 lg:grid-cols-2">
      <Card title="快捷操作">
        <div class="grid grid-cols-3 gap-4">
          <div
            v-for="item in quickNavItems"
            :key="item.title"
            class="group flex-col-center cursor-pointer rounded-lg border border-border p-4 transition-all duration-300 hover:shadow-lg"
          >
            <IconifyIcon
              :color="item.color"
              :icon="item.icon"
              class="size-8 transition-all duration-300 group-hover:scale-110"
            />
            <span class="mt-2 text-sm">{{ item.title }}</span>
          </div>
        </div>
      </Card>

      <Card title="系统信息">
        <div class="space-y-3">
          <div class="flex items-center justify-between rounded-lg bg-muted/50 p-3">
            <span class="text-muted-foreground">系统版本</span>
            <span class="font-medium">v1.0.0</span>
          </div>
          <div class="flex items-center justify-between rounded-lg bg-muted/50 p-3">
            <span class="text-muted-foreground">技术栈</span>
            <span class="font-medium">Spring Cloud + Vue 3</span>
          </div>
          <div class="flex items-center justify-between rounded-lg bg-muted/50 p-3">
            <span class="text-muted-foreground">框架版本</span>
            <span class="font-medium">Vben Admin v5.7</span>
          </div>
          <div class="flex items-center justify-between rounded-lg bg-muted/50 p-3">
            <span class="text-muted-foreground">当前主题</span>
            <span class="font-medium">{{ isDark ? '暗色模式' : '亮色模式' }}</span>
          </div>
        </div>
      </Card>
    </div>
  </div>
</template>
