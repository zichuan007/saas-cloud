<script lang="ts" setup>
import {ref} from 'vue';

import {useVbenModal} from '@vben/common-ui';

import {Form, FormItem, Input, message, Switch} from 'ant-design-vue';

import {requestClient} from '#/api/request';

defineOptions({ name: 'ChannelFormModal' });

const emit = defineEmits<{ success: [] }>();

const formData = ref<Record<string, any>>({});
const formRef = ref();

const rules = {
  configJson: [
    { message: '请输入渠道配置JSON', required: true, trigger: 'blur' },
  ],
};

const [Modal, modalApi] = useVbenModal({
  onOpenChange(isOpen) {
    if (!isOpen) return;
    const data = modalApi.getData<{ record: any }>();
    modalApi.setState({
      title: `配置 - ${data.record.channelTypeDesc}`,
    });
    formData.value = { ...data.record };
  },
  async onConfirm() {
    try {
      await formRef.value?.validate();
    } catch {
      return false;
    }
    await requestClient.put(`/notify/channel/${formData.value.channelType}`, {
      enabled: formData.value.enabled,
      configJson: formData.value.configJson,
    });
    message.success('保存成功');
    emit('success');
    modalApi.close();
  },
  title: '渠道配置',
});
</script>

<template>
  <Modal>
    <Form
      ref="formRef"
      :label-col="{ span: 5 }"
      :model="formData"
      :rules="rules"
      :wrapper-col="{ span: 18 }"
    >
      <FormItem label="启用状态">
        <Switch
          :checked="formData.enabled === 1"
          checked-children="启用"
          un-checked-children="禁用"
          @change="(c: any) => (formData.enabled = c ? 1 : 0)"
        />
      </FormItem>
      <FormItem label="配置(JSON)" name="configJson">
        <Input.TextArea
          v-model:value="formData.configJson"
          :rows="6"
          placeholder="请输入渠道配置 JSON"
        />
      </FormItem>
    </Form>
  </Modal>
</template>
