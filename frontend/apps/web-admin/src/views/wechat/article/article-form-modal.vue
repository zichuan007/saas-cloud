<script lang="ts" setup>
import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { Form, FormItem, Input, message } from 'ant-design-vue';

import {
  type ArticleRecord,
  createArticle,
  getArticleDetail,
  updateArticle,
} from '#/api/wechat/article';

defineOptions({ name: 'ArticleFormModal' });

const emit = defineEmits<{ success: [] }>();

const formData = ref<Record<string, any>>({});
const mode = ref<'add' | 'edit'>('add');
const formRef = ref();

const rules = {
  title: [{ message: '请输入标题', required: true, trigger: 'blur' }],
  content: [{ message: '请输入正文内容', required: true, trigger: 'blur' }],
};

const [Modal, modalApi] = useVbenModal({
  async onOpenChange(isOpen) {
    if (!isOpen) return;
    const data =
      modalApi.getData<{ accountId?: number; id?: number; mode: 'add' | 'edit' }>();
    mode.value = data.mode;
    modalApi.setState({ title: data.mode === 'add' ? '新建图文' : '编辑图文' });
    if (data.mode === 'edit' && data.id) {
      const detail = await getArticleDetail(data.id);
      formData.value = { ...(detail as ArticleRecord) };
    } else {
      formData.value = { accountId: data.accountId };
    }
  },
  async onConfirm() {
    try {
      await formRef.value?.validate();
    } catch {
      return false;
    }
    if (mode.value === 'add') {
      await createArticle(formData.value);
      message.success('新增成功');
    } else {
      await updateArticle(formData.value.id, formData.value);
      message.success('编辑成功');
    }
    emit('success');
    modalApi.close();
  },
  title: '新建图文',
});
</script>

<template>
  <Modal>
    <Form
      ref="formRef"
      :label-col="{ span: 4 }"
      :model="formData"
      :rules="rules"
      :wrapper-col="{ span: 19 }"
    >
      <FormItem label="标题" name="title">
        <Input v-model:value="formData.title" placeholder="请输入标题" />
      </FormItem>
      <FormItem label="作者">
        <Input v-model:value="formData.author" placeholder="请输入作者" />
      </FormItem>
      <FormItem label="摘要">
        <Input.TextArea
          v-model:value="formData.digest"
          placeholder="请输入摘要"
          :rows="2"
        />
      </FormItem>
      <FormItem label="正文" name="content">
        <Input.TextArea
          v-model:value="formData.content"
          placeholder="请输入正文内容"
          :rows="8"
        />
      </FormItem>
      <FormItem label="封面URL">
        <Input
          v-model:value="formData.coverUrl"
          placeholder="请输入封面图片URL"
        />
      </FormItem>
    </Form>
  </Modal>
</template>
