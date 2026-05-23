<script lang="ts" setup>
import {ref} from 'vue';

import {useVbenModal} from '@vben/common-ui';

import {Form, FormItem, Input, InputNumber, message, Switch} from 'ant-design-vue';

import {createPost, getPostDetail, updatePost} from '#/api/system/post';

defineOptions({ name: 'PostFormModal' });

const emit = defineEmits<{ success: [] }>();

const formData = ref<Record<string, any>>({});
const mode = ref<'add' | 'edit'>('add');
const formRef = ref();

const [Modal, modalApi] = useVbenModal({
  async onOpenChange(isOpen) {
    if (!isOpen) return;
    const data = modalApi.getData<{ id?: number; mode: 'add' | 'edit' }>();
    mode.value = data.mode;
    modalApi.setState({
      title: data.mode === 'add' ? '新增岗位' : '编辑岗位',
    });
    if (data.mode === 'edit' && data.id) {
      const detail = await getPostDetail(data.id);
      formData.value = { ...detail };
    } else {
      formData.value = { sortOrder: 0, status: 1 };
    }
  },
  async onConfirm() {
    try {
      await formRef.value?.validate();
    } catch {
      return false;
    }
    if (mode.value === 'add') {
      await createPost(formData.value);
      message.success('新增成功');
    } else {
      await updatePost(formData.value.id, formData.value);
      message.success('编辑成功');
    }
    emit('success');
    modalApi.close();
  },
  title: '新增岗位',
});
</script>

<template>
  <Modal>
    <Form
      ref="formRef"
      :label-col="{ span: 5 }"
      :model="formData"
      :rules="{
        postName: [
          { message: '请输入岗位名称', required: true, trigger: 'blur' },
        ],
        postCode: [
          { message: '请输入岗位编码', required: true, trigger: 'blur' },
        ],
      }"
      :wrapper-col="{ span: 18 }"
    >
      <FormItem label="岗位名称" name="postName">
        <Input
          v-model:value="formData.postName"
          placeholder="请输入岗位名称"
        />
      </FormItem>
      <FormItem label="岗位编码" name="postCode">
        <Input
          v-model:value="formData.postCode"
          :disabled="mode === 'edit'"
          placeholder="请输入岗位编码"
        />
      </FormItem>
      <FormItem label="排序">
        <InputNumber
          v-model:value="formData.sortOrder"
          :min="0"
          style="width: 100%"
        />
      </FormItem>
      <FormItem label="状态">
        <Switch
          :checked="formData.status === 1"
          checked-children="启用"
          un-checked-children="禁用"
          @change="(c: any) => (formData.status = c ? 1 : 0)"
        />
      </FormItem>
      <FormItem label="备注">
        <Input.TextArea
          v-model:value="formData.remark"
          :rows="2"
          placeholder="备注"
        />
      </FormItem>
    </Form>
  </Modal>
</template>
