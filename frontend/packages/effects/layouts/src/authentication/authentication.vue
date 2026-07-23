<script setup lang="ts">
import type {ToolbarType} from './types';

import {computed} from 'vue';

import {ParticleNetwork, TypeWriter} from '@vben/common-ui';
import {preferences, usePreferences} from '@vben/preferences';

import {Copyright} from '../basic/copyright';
import AuthenticationFormView from './form.vue';
import SloganIcon from './icons/slogan.vue';
import Toolbar from './toolbar.vue';

interface Props {
  appName?: string;
  logo?: string;
  logoDark?: string;
  pageTitle?: string;
  pageDescription?: string;
  sloganImage?: string;
  toolbar?: boolean;
  copyright?: boolean;
  toolbarList?: ToolbarType[];
  clickLogo?: () => void;
}

const props = withDefaults(defineProps<Props>(), {
  appName: '',
  copyright: true,
  logo: '',
  logoDark: '',
  pageDescription: '',
  pageTitle: '',
  sloganImage: '',
  toolbar: true,
  toolbarList: () => ['color', 'language', 'layout', 'theme'],
  clickLogo: () => {},
});

const { authPanelCenter, authPanelLeft, authPanelRight, isDark } =
  usePreferences();

/**
 * @zh_CN 根据主题选择合适的 logo 图标
 */
const logoSrc = computed(() => {
  // 如果是暗色主题且提供了 logoDark，则使用暗色主题的 logo
  if (isDark.value && props.logoDark) {
    return props.logoDark;
  }
  // 否则使用默认的 logo
  return props.logo;
});
</script>

<template>
  <div
    :class="[isDark ? 'dark' : '']"
    class="flex min-h-full flex-1 overflow-x-hidden select-none"
  >
    <template v-if="toolbar">
      <slot name="toolbar">
        <Toolbar :toolbar-list="toolbarList" />
      </slot>
    </template>
    <!-- 左侧认证面板 -->
    <AuthenticationFormView
      v-if="authPanelLeft"
      class="min-h-full w-2/5 flex-1"
      data-side="left"
    >
      <template v-if="copyright" #copyright>
        <slot name="copyright">
          <Copyright
            v-if="preferences.copyright.enable"
            v-bind="preferences.copyright"
          />
        </slot>
      </template>
    </AuthenticationFormView>

    <slot name="logo">
      <!-- 头部 Logo 和应用名称 -->
      <div
        v-if="logoSrc || appName"
        class="absolute top-0 left-0 z-10 flex flex-1"
        @click="clickLogo"
      >
        <div
          class="mt-4 ml-4 flex flex-1 items-center text-foreground sm:top-6 sm:left-6 lg:text-foreground"
        >
          <img
            v-if="logoSrc"
            :key="logoSrc"
            :alt="appName"
            :src="logoSrc"
            class="mr-2"
            width="42"
          />
          <p v-if="appName" class="m-0 text-xl font-medium">
            {{ appName }}
          </p>
        </div>
      </div>
    </slot>

    <!-- 系统介绍 -->
    <div v-if="!authPanelCenter" class="relative hidden w-0 flex-1 lg:block">
      <div
        class="absolute inset-0 size-full bg-background-deep dark:bg-[#070709]"
      >
        <div class="aurora-container absolute inset-0 overflow-hidden">
          <div class="aurora-blob aurora-blob-1"></div>
          <div class="aurora-blob aurora-blob-2"></div>
          <div class="aurora-blob aurora-blob-3"></div>
          <div class="aurora-blob aurora-blob-4"></div>
        </div>
        <div class="absolute inset-0 size-full">
          <ParticleNetwork
            :interactive="true"
            :max-distance="160"
            :opacity="0.35"
            :particle-count="55"
            :speed="0.5"
            line-color="rgba(99, 102, 241, 0.35)"
            particle-color="rgba(139, 92, 246, 0.6)"
          />
        </div>
        <div
          :key="authPanelLeft ? 'left' : authPanelRight ? 'right' : 'center'"
          class="mr-20 flex-col-center relative z-10 h-full"
          :class="{
            'enter-x': authPanelLeft,
            '-enter-x': authPanelRight,
          }"
        >
          <div class="slogan-glow relative">
            <template v-if="sloganImage">
              <img
                :alt="appName"
                :src="sloganImage"
                class="h-64 w-2/5 animate-float"
              />
            </template>
            <SloganIcon
              v-else
              :alt="appName"
              class="h-64 w-2/5 animate-float"
            />
          </div>
          <div class="text-1xl mt-6 font-sans text-foreground lg:text-2xl">
            <TypeWriter
              v-if="pageTitle"
              :loop="false"
              :pause-duration="3000"
              :texts="[pageTitle]"
              :type-speed="60"
            />
            <template v-else>
              {{ pageTitle }}
            </template>
          </div>
          <div class="mt-2 dark:text-muted-foreground">
            {{ pageDescription }}
          </div>
        </div>
      </div>
    </div>

    <!-- 中心认证面板 -->
    <div v-if="authPanelCenter" class="relative flex-center w-full">
      <div class="aurora-container absolute inset-0 overflow-hidden">
        <div class="aurora-blob aurora-blob-1"></div>
        <div class="aurora-blob aurora-blob-2"></div>
        <div class="aurora-blob aurora-blob-3"></div>
        <div class="aurora-blob aurora-blob-4"></div>
      </div>
      <div class="absolute inset-0 size-full">
        <ParticleNetwork
          :interactive="true"
          :max-distance="140"
          :opacity="0.3"
          :particle-count="45"
          :speed="0.4"
          line-color="rgba(99, 102, 241, 0.3)"
          particle-color="rgba(139, 92, 246, 0.5)"
        />
      </div>
      <AuthenticationFormView
        class="glass-card w-full rounded-3xl pb-20 md:w-2/3 lg:w-1/2 xl:w-[36%]"
        data-side="bottom"
      >
        <template v-if="copyright" #copyright>
          <slot name="copyright">
            <Copyright
              v-if="preferences.copyright.enable"
              v-bind="preferences.copyright"
            />
          </slot>
        </template>
      </AuthenticationFormView>
    </div>

    <!-- 右侧认证面板 -->
    <AuthenticationFormView
      v-if="authPanelRight"
      class="min-h-full w-2/5 flex-1"
      data-side="right"
    >
      <template v-if="copyright" #copyright>
        <slot name="copyright">
          <Copyright
            v-if="preferences.copyright.enable"
            v-bind="preferences.copyright"
          />
        </slot>
      </template>
    </AuthenticationFormView>
  </div>
</template>

<style scoped>
.aurora-container {
  background: hsl(var(--background-deep));
}

.aurora-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  will-change: transform;
  mix-blend-mode: screen;
}

.aurora-blob-1 {
  width: 65%;
  height: 65%;
  top: -20%;
  left: -15%;
  background: radial-gradient(
    circle,
    rgba(139, 92, 246, 0.55) 0%,
    rgba(124, 58, 237, 0.2) 40%,
    transparent 70%
  );
  animation: aurora-1 8s ease-in-out infinite alternate;
}

.aurora-blob-2 {
  width: 60%;
  height: 60%;
  top: 20%;
  right: -20%;
  background: radial-gradient(
    circle,
    rgba(59, 130, 246, 0.5) 0%,
    rgba(37, 99, 235, 0.15) 40%,
    transparent 70%
  );
  animation: aurora-2 10s ease-in-out infinite alternate;
}

.aurora-blob-3 {
  width: 55%;
  height: 55%;
  bottom: -15%;
  left: 20%;
  background: radial-gradient(
    circle,
    rgba(6, 182, 212, 0.45) 0%,
    rgba(8, 145, 178, 0.12) 40%,
    transparent 70%
  );
  animation: aurora-3 12s ease-in-out infinite alternate;
}

.aurora-blob-4 {
  width: 40%;
  height: 40%;
  top: 50%;
  left: 40%;
  background: radial-gradient(
    circle,
    rgba(236, 72, 153, 0.3) 0%,
    rgba(219, 39, 119, 0.08) 40%,
    transparent 70%
  );
  animation: aurora-4 14s ease-in-out infinite alternate;
}

@keyframes aurora-1 {
  0% {
    transform: translate(0, 0) scale(1) rotate(0deg);
  }
  50% {
    transform: translate(15%, 10%) scale(1.1) rotate(3deg);
  }
  100% {
    transform: translate(30%, 20%) scale(1.2) rotate(-2deg);
  }
}

@keyframes aurora-2 {
  0% {
    transform: translate(0, 0) scale(1) rotate(0deg);
  }
  50% {
    transform: translate(-10%, 15%) scale(1.15) rotate(-3deg);
  }
  100% {
    transform: translate(-25%, 30%) scale(1.05) rotate(2deg);
  }
}

@keyframes aurora-3 {
  0% {
    transform: translate(0, 0) scale(1);
  }
  50% {
    transform: translate(10%, -10%) scale(1.2);
  }
  100% {
    transform: translate(20%, -25%) scale(1.3);
  }
}

@keyframes aurora-4 {
  0% {
    transform: translate(0, 0) scale(1);
    opacity: 0.6;
  }
  50% {
    transform: translate(-15%, -10%) scale(1.3);
    opacity: 1;
  }
  100% {
    transform: translate(10%, 15%) scale(0.9);
    opacity: 0.4;
  }
}

.dark .aurora-blob {
  mix-blend-mode: normal;
}

.dark .aurora-blob-1 {
  background: radial-gradient(
    circle,
    rgba(139, 92, 246, 0.6) 0%,
    rgba(124, 58, 237, 0.25) 40%,
    transparent 70%
  );
}

.dark .aurora-blob-2 {
  background: radial-gradient(
    circle,
    rgba(59, 130, 246, 0.55) 0%,
    rgba(37, 99, 235, 0.2) 40%,
    transparent 70%
  );
}

.dark .aurora-blob-3 {
  background: radial-gradient(
    circle,
    rgba(6, 182, 212, 0.5) 0%,
    rgba(8, 145, 178, 0.15) 40%,
    transparent 70%
  );
}

.dark .aurora-blob-4 {
  background: radial-gradient(
    circle,
    rgba(236, 72, 153, 0.4) 0%,
    rgba(219, 39, 119, 0.12) 40%,
    transparent 70%
  );
}

:deep(.glass-card) {
  background: rgba(255, 255, 255, 0.1) !important;
  backdrop-filter: blur(28px) saturate(200%);
  -webkit-backdrop-filter: blur(28px) saturate(200%);
  border: 1px solid rgba(255, 255, 255, 0.18);
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.12),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.dark :deep(.glass-card) {
  background: rgba(255, 255, 255, 0.06) !important;
  border-color: rgba(255, 255, 255, 0.1);
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.06);
}

.slogan-glow {
  position: relative;
}

.slogan-glow::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 140%;
  height: 140%;
  transform: translate(-50%, -50%);
  border-radius: 50%;
  background: radial-gradient(
    circle,
    rgba(139, 92, 246, 0.12) 0%,
    rgba(59, 130, 246, 0.06) 30%,
    transparent 60%
  );
  animation: glow-pulse 3s ease-in-out infinite;
  pointer-events: none;
}

@keyframes glow-pulse {
  0%,
  100% {
    opacity: 0.5;
    transform: translate(-50%, -50%) scale(1);
  }
  50% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1.08);
  }
}
</style>
