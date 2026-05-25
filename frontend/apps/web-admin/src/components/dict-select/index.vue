<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';

import { Select } from 'ant-design-vue';

import { getDictDataByType } from '#/api/system/dict';

interface Props {
  dictType: string;
  value?: number | string;
  placeholder?: string;
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: '请选择',
  value: undefined,
});

const emit = defineEmits<{
  'update:value': [val: number | string | undefined];
}>();

const options = ref<{ label: string; value: string }[]>([]);
const loading = ref(false);

const cache = new Map<string, { label: string; value: string }[]>();

async function loadOptions(dictType: string) {
  if (!dictType) return;
  if (cache.has(dictType)) {
    options.value = cache.get(dictType)!;
    return;
  }
  loading.value = true;
  try {
    const data = (await getDictDataByType(dictType)) as any[];
    const list = data.map((item) => ({
      label: item.dictLabel,
      value: item.dictValue,
    }));
    cache.set(dictType, list);
    options.value = list;
  } finally {
    loading.value = false;
  }
}

onMounted(() => loadOptions(props.dictType));
watch(() => props.dictType, loadOptions);

function handleChange(val: number | string | undefined) {
  emit('update:value', val);
}
</script>

<template>
  <Select
    :loading="loading"
    :options="options"
    :placeholder="placeholder"
    :value="value"
    allow-clear
    @change="handleChange"
  />
</template>
