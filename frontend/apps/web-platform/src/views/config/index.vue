<script lang="ts" setup>
import { ref } from 'vue';

import { Page } from '@vben/common-ui';

import { Button, Form, FormItem, Input, message } from 'ant-design-vue';

import { type ConfigItem, getConfigList, updateConfig } from '#/api/platform/config';

defineOptions({ name: 'PlatformConfig' });

const configs = ref<ConfigItem[]>([]);
const loading = ref(false);
const editValues = ref<Record<string, string>>({});

async function loadData() {
  loading.value = true;
  try {
    configs.value = (await getConfigList()) as ConfigItem[];
    editValues.value = {};
    for (const c of configs.value) {
      editValues.value[c.key] = c.value;
    }
  } finally {
    loading.value = false;
  }
}
loadData();

async function handleSave(key: string) {
  await updateConfig(key, editValues.value[key]!);
  message.success('保存成功');
}

const configLabels: Record<string, string> = {
  default_package_id: '默认套餐ID',
  max_login_attempts: '最大登录尝试次数',
  password_min_length: '密码最小长度',
  trial_days: '试用天数',
};
</script>

<template>
  <Page auto-content-height>
    <div class="p-4">
      <h3 class="mb-4 text-lg font-medium">全局配置</h3>
      <Form :label-col="{ span: 6 }" :wrapper-col="{ span: 12 }">
        <FormItem
          v-for="config in configs"
          :key="config.key"
          :label="configLabels[config.key] ?? config.key"
        >
          <div class="flex items-center gap-2">
            <Input
              v-model:value="editValues[config.key]"
              :placeholder="config.description"
              style="flex: 1"
            />
            <Button type="primary" size="small" @click="handleSave(config.key)">
              保存
            </Button>
          </div>
          <div v-if="config.description" class="mt-1 text-xs text-gray-400">
            {{ config.description }}
          </div>
        </FormItem>
      </Form>
    </div>
  </Page>
</template>
