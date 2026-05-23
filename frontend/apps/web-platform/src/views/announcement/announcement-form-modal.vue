<script lang="ts" setup>
import {ref} from 'vue';

import {useVbenModal} from '@vben/common-ui';

import {Form, FormItem, Input, message} from 'ant-design-vue';

import {
  type AnnouncementRecord,
  createAnnouncement,
  updateAnnouncement,
} from '#/api/platform/announcement';

defineOptions({ name: 'AnnouncementFormModal' });

const emit = defineEmits<{ success: [] }>();

const formData = ref<Record<string, any>>({});
const mode = ref<'add' | 'edit'>('add');

const [Modal, modalApi] = useVbenModal({
  onOpenChange(isOpen) {
    if (!isOpen) return;
    const data =
      modalApi.getData<{
        id?: number;
        mode: 'add' | 'edit';
        record?: AnnouncementRecord;
      }>();
    mode.value = data.mode;
    modalApi.setState({ title: data.mode === 'add' ? '新建公告' : '编辑公告' });
    if (data.mode === 'edit' && data.record) {
      formData.value = { ...data.record };
    } else {
      formData.value = {};
    }
  },
  async onConfirm() {
    if (mode.value === 'add') {
      await createAnnouncement(formData.value);
      message.success('新增成功');
    } else {
      await updateAnnouncement(formData.value.id, formData.value);
      message.success('编辑成功');
    }
    emit('success');
    modalApi.close();
  },
  title: '新建公告',
});
</script>

<template>
  <Modal>
    <Form :label-col="{ span: 4 }" :wrapper-col="{ span: 19 }">
      <FormItem label="标题" required>
        <Input v-model:value="formData.title" placeholder="请输入公告标题" />
      </FormItem>
      <FormItem label="内容" required>
        <Input.TextArea
          v-model:value="formData.content"
          placeholder="请输入公告内容"
          :rows="8"
        />
      </FormItem>
    </Form>
  </Modal>
</template>
