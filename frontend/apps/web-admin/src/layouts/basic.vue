<script lang="ts" setup>
import type {NotificationItem} from '@vben/layouts';
import {BasicLayout, LockScreen, Notification, UserDropdown,} from '@vben/layouts';

import {computed, onMounted, ref, watch} from 'vue';
import {useRouter} from 'vue-router';

import {AuthenticationLoginExpiredModal} from '@vben/common-ui';
import {useWatermark} from '@vben/hooks';
import {preferences, usePreferences} from '@vben/preferences';
import {useAccessStore, useUserStore} from '@vben/stores';

import {$t} from '#/locales';
import {useAuthStore} from '#/store';
import LoginForm from '#/views/_core/authentication/login.vue';
import {deleteMessage, getMessageList, markAllRead, markMessageRead,} from '#/api/notify';

const notifications = ref<NotificationItem[]>([]);

async function loadNotifications() {
  try {
    const data = (await getMessageList({ pageNum: 1, pageSize: 10 })) as any;
    const records = data?.records ?? data ?? [];
    notifications.value = records.map((item: any) => ({
      id: item.id,
      date: item.createTime ?? '',
      isRead: item.isRead ?? false,
      message: item.content ?? '',
      title: item.title ?? '',
    }));
  } catch {
    notifications.value = [];
  }
}

onMounted(() => {
  loadNotifications();
});

const router = useRouter();
const userStore = useUserStore();
const authStore = useAuthStore();
const accessStore = useAccessStore();
const { destroyWatermark, updateWatermark } = useWatermark();
const { isDark } = usePreferences();
const showDot = computed(() =>
  notifications.value.some((item) => !item.isRead),
);

const menus = computed(() => [
  {
    handler: () => {
      router.push({ name: 'Profile' });
    },
    icon: 'lucide:user',
    text: $t('page.auth.profile'),
  },
]);

const avatar = computed(() => {
  return userStore.userInfo?.avatar ?? preferences.app.defaultAvatar;
});

async function handleLogout() {
  await authStore.logout(false);
}

function handleNoticeClear() {
  notifications.value = [];
}

async function markRead(id: number | string) {
  await markMessageRead(id as number);
  const item = notifications.value.find((item) => item.id === id);
  if (item) {
    item.isRead = true;
  }
}

async function remove(id: number | string) {
  await deleteMessage(id as number);
  notifications.value = notifications.value.filter((item) => item.id !== id);
}

async function handleMakeAll() {
  await markAllRead();
  notifications.value.forEach((item) => (item.isRead = true));
}

function viewAll() {
  router.push('/notify');
}

function handleClick(item: NotificationItem) {
  if (item.id && !item.isRead) {
    markRead(item.id);
  }
}

watch(
  () => ({
    enable: preferences.app.watermark,
    content: preferences.app.watermarkContent,
    isDark: isDark.value,
  }),
  async ({ enable, content, isDark: isDarkValue }) => {
    if (enable) {
      const watermarkColor = isDarkValue
        ? 'rgba(255, 255, 255, 0.12)'
        : 'rgba(0, 0, 0, 0.12)';

      await updateWatermark({
        advancedStyle: {
          colorStops: [
            {
              color: watermarkColor,
              offset: 0,
            },
            {
              color: watermarkColor,
              offset: 1,
            },
          ],
          type: 'linear',
        },
        content:
          content ||
          `${userStore.userInfo?.username} - ${userStore.userInfo?.realName}`,
      });
    } else {
      destroyWatermark();
    }
  },
  {
    immediate: true,
  },
);
</script>

<template>
  <BasicLayout @clear-preferences-and-logout="handleLogout">
    <template #user-dropdown>
      <UserDropdown
        :avatar
        :menus
        :text="userStore.userInfo?.realName"
        :description="userStore.userInfo?.username ?? ''"
        @logout="handleLogout"
      />
    </template>
    <template #notification>
      <Notification
        :dot="showDot"
        :notifications="notifications"
        @clear="handleNoticeClear"
        @read="(item) => item.id && markRead(item.id)"
        @remove="(item) => item.id && remove(item.id)"
        @make-all="handleMakeAll"
        @on-click="handleClick"
        @view-all="viewAll"
      />
    </template>
    <template #extra>
      <AuthenticationLoginExpiredModal
        v-model:open="accessStore.loginExpired"
        :avatar
      >
        <LoginForm />
      </AuthenticationLoginExpiredModal>
    </template>
    <template #lock-screen>
      <LockScreen :avatar @to-login="handleLogout" />
    </template>
  </BasicLayout>
</template>
