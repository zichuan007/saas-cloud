<script lang="ts" setup>
import {ref} from 'vue';

import {useVbenModal} from '@vben/common-ui';

import {Form, FormItem, Input, message, Select} from 'ant-design-vue';

import {getPackageList, type PackageRecord} from '#/api/platform/package';
import {
  createTenant,
  getTenantDetail,
  type TenantRecord,
  updateTenant,
} from '#/api/platform/tenant';

defineOptions({ name: 'TenantFormModal' });

const emit = defineEmits<{ success: [] }>();

const formData = ref<Record<string, any>>({});
const mode = ref<'add' | 'edit'>('add');
const packageOptions = ref<any[]>([]);

const [Modal, modalApi] = useVbenModal({
  async onOpenChange(isOpen) {
    if (!isOpen) return;
    const data = modalApi.getData<{ id?: number; mode: 'add' | 'edit' }>();
    mode.value = data.mode;
    modalApi.setState({ title: data.mode === 'add' ? '创建租户' : '编辑租户' });

    const packages = (await getPackageList()) as PackageRecord[];
    packageOptions.value = packages.map((p) => ({
      label: p.packageName,
      value: p.id,
    }));

    if (data.mode === 'edit' && data.id) {
      const detail = await getTenantDetail(data.id);
      formData.value = { ...(detail as TenantRecord) };
    } else {
      formData.value = {};
    }
  },
  async onConfirm() {
    if (mode.value === 'add') {
      await createTenant(formData.value);
      message.success('创建成功');
    } else {
      await updateTenant(formData.value.id, formData.value);
      message.success('编辑成功');
    }
    emit('success');
    modalApi.close();
  },
  title: '创建租户',
});
</script>

<template>
  <Modal>
    <Form :label-col="{ span: 5 }" :wrapper-col="{ span: 18 }">
      <FormItem label="租户名称" required>
        <Input v-model:value="formData.tenantName" placeholder="请输入租户名称" />
      </FormItem>
      <FormItem label="联系人" required>
        <Input
          v-model:value="formData.contactPerson"
          placeholder="请输入联系人"
        />
      </FormItem>
      <FormItem label="手机号">
        <Input v-model:value="formData.phone" placeholder="请输入手机号" />
      </FormItem>
      <FormItem label="套餐">
        <Select
          v-model:value="formData.packageId"
          :options="packageOptions"
          placeholder="请选择套餐"
        />
      </FormItem>
    </Form>
  </Modal>
</template>
