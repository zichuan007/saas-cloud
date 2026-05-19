<script lang="ts" setup>
import { ref } from 'vue';

import { Page, useVbenModal } from '@vben/common-ui';

import { Button, message, Popconfirm, Space, Table } from 'ant-design-vue';

import { deleteDept, type DeptRecord, getDeptTree } from '#/api/system/dept';

import DeptFormModal from './dept-form-modal.vue';

defineOptions({ name: 'SystemDept' });

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: DeptFormModal,
});

const deptTree = ref<DeptRecord[]>([]);
const loading = ref(false);

async function loadDeptTree() {
  loading.value = true;
  try {
    deptTree.value = (await getDeptTree()) as DeptRecord[];
  } finally {
    loading.value = false;
  }
}
loadDeptTree();

function handleAdd(parentId?: number) {
  formModalApi.setData({ mode: 'add', parentId: parentId ?? 0 });
  formModalApi.open();
}

function handleEdit(row: DeptRecord) {
  formModalApi.setData({ id: row.id, mode: 'edit' });
  formModalApi.open();
}

async function handleDelete(id: number) {
  await deleteDept(id);
  message.success('删除成功');
  await loadDeptTree();
}
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="loadDeptTree" />
    <div class="p-4">
      <div class="mb-4 flex justify-between">
        <h3 class="text-lg font-medium">部门管理</h3>
        <Button type="primary" @click="handleAdd()">新增部门</Button>
      </div>
      <Table
        :columns="[
          { title: '部门名称', dataIndex: 'deptName', key: 'deptName', width: 250 },
          { title: '负责人', dataIndex: 'leaderUserName', key: 'leader', width: 120 },
          { title: '排序', dataIndex: 'sortOrder', key: 'sortOrder', width: 80 },
          { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
          { title: '操作', key: 'action', width: 200, fixed: 'right' },
        ]"
        :data-source="deptTree"
        :loading="loading"
        :pagination="false"
        children-column-name="children"
        default-expand-all-rows
        row-key="id"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <Space>
              <Button
                size="small"
                type="link"
                @click="handleAdd(record.id)"
              >
                新增子部门
              </Button>
              <Button size="small" type="link" @click="handleEdit(record)">
                编辑
              </Button>
              <Popconfirm
                title="确定删除？有子部门或用户时无法删除"
                @confirm="handleDelete(record.id)"
              >
                <Button danger size="small" type="link">删除</Button>
              </Popconfirm>
            </Space>
          </template>
        </template>
      </Table>
    </div>
  </Page>
</template>
