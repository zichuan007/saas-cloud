<script lang="ts" setup>
import { computed } from 'vue';

import { Button, message, Tabs, TabPane } from 'ant-design-vue';

defineOptions({ name: 'CodePreview' });

const props = defineProps<{
  files: Record<string, string>;
}>();

const fileList = computed(() =>
  Object.entries(props.files).map(([name, code]) => ({
    name,
    code,
    shortName: name.includes('/') ? name.split('/').pop()! : name,
  })),
);

function handleCopy(code: string) {
  navigator.clipboard.writeText(code).then(() => {
    message.success('已复制到剪贴板');
  });
}
</script>

<template>
  <Tabs type="card" size="small">
    <TabPane
      v-for="file in fileList"
      :key="file.name"
      :tab="file.shortName"
    >
      <div style="position: relative">
        <Button
          size="small"
          style="position: absolute; right: 8px; top: 8px; z-index: 1"
          @click="handleCopy(file.code)"
        >
          复制
        </Button>
        <pre
          style="
            background: #f5f5f5;
            padding: 16px;
            border-radius: 4px;
            overflow: auto;
            max-height: 600px;
            font-size: 13px;
            line-height: 1.5;
          "
        ><code>{{ file.code }}</code></pre>
      </div>
    </TabPane>
  </Tabs>
</template>
