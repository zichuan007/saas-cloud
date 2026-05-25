<script lang="ts" setup>
import { computed, ref, watch } from 'vue';

import { Upload, message } from 'ant-design-vue';
import { PlusOutlined } from '@ant-design/icons-vue';

import { requestClient } from '#/api/request';

interface Props {
  value?: string | string[];
  accept?: string;
  multiple?: boolean;
  maxCount?: number;
  bizType?: string;
}

const props = withDefaults(defineProps<Props>(), {
  accept: undefined,
  bizType: 'default',
  maxCount: 1,
  multiple: false,
  value: undefined,
});

const emit = defineEmits<{
  'update:value': [val: string | string[] | undefined];
}>();

interface FileItem {
  name: string;
  status: string;
  uid: string;
  url: string;
}

const fileList = ref<FileItem[]>([]);

function valueToList(val: string | string[] | undefined): FileItem[] {
  if (!val) return [];
  const urls = Array.isArray(val) ? val : [val];
  return urls.map((url, idx) => ({
    name: url.split('/').pop() || `file-${idx}`,
    status: 'done',
    uid: `${idx}`,
    url,
  }));
}

watch(
  () => props.value,
  (val) => {
    fileList.value = valueToList(val);
  },
  { immediate: true },
);

const isImageMode = computed(() => props.accept?.startsWith('image'));

async function handleUpload(options: any) {
  const { file, onSuccess, onError } = options;
  const formData = new FormData();
  formData.append('file', file);
  formData.append('bizType', props.bizType);
  try {
    const res = (await requestClient.post('/platform/file/upload', formData)) as any;
    const url = res.url || res.fileUrl || '';
    onSuccess(res, file);
    emitValue(url);
  } catch (err: any) {
    message.error('上传失败');
    onError(err);
  }
}

function emitValue(newUrl: string) {
  if (props.multiple) {
    const urls = fileList.value
      .filter((f) => f.status === 'done' && f.url)
      .map((f) => f.url);
    if (newUrl) urls.push(newUrl);
    emit('update:value', urls);
  } else {
    emit('update:value', newUrl);
  }
}

function handleRemove(file: any) {
  const remaining = fileList.value
    .filter((f) => f.uid !== file.uid && f.status === 'done')
    .map((f) => f.url);
  emit('update:value', props.multiple ? remaining : undefined);
}
</script>

<template>
  <Upload
    v-model:file-list="fileList"
    :accept="accept"
    :custom-request="handleUpload"
    :list-type="isImageMode ? 'picture-card' : 'text'"
    :max-count="multiple ? maxCount : 1"
    :multiple="multiple"
    @remove="handleRemove"
  >
    <div v-if="isImageMode">
      <PlusOutlined />
      <div style="margin-top: 8px">上传</div>
    </div>
    <a-button v-else>上传文件</a-button>
  </Upload>
</template>
