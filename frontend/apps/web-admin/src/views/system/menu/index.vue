<script lang="ts" setup>
import {ref} from 'vue';

import {Page, useVbenModal} from '@vben/common-ui';

import {Button, message, Popconfirm, Space, Table, Tag} from 'ant-design-vue';

import {deleteMenu, getMenuTree, type MenuRecord} from '#/api/system/menu';

import MenuFormModal from './menu-form-modal.vue';

defineOptions({ name: 'SystemMenu' });

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: MenuFormModal,
});

const menuTree = ref<MenuRecord[]>([]);
const loading = ref(false);

async function loadData() {
  loading.value = true;
  try {
    menuTree.value = (await getMenuTree()) as MenuRecord[];
  } finally {
    loading.value = false;
  }
}
loadData();

function handleAdd(parentId?: number) {
  formModalApi.setData({ mode: 'add', parentId: parentId ?? 0 });
  formModalApi.open();
}

function handleEdit(row: MenuRecord) {
  formModalApi.setData({ mode: 'edit', record: row });
  formModalApi.open();
}

async function handleDelete(id: number) {
  await deleteMenu(id);
  message.success('删除成功');
  await loadData();
}

function menuTypeTag(type: number) {
  switch (type) {
    case 0: {
      return { color: 'blue', text: '目录' };
    }
    case 1: {
      return { color: 'green', text: '菜单' };
    }
    case 2: {
      return { color: 'orange', text: '按钮' };
    }
    default: {
      return { color: 'default', text: '未知' };
    }
  }
}
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="loadData" />
    <div class="p-4">
      <div class="mb-4 flex items-center justify-between">
        <h3 class="text-lg font-medium">菜单管理</h3>
        <Space>
          <Button type="primary" @click="handleAdd()">新增菜单</Button>
          <Button @click="loadData">刷新</Button>
        </Space>
      </div>
      <Table
        :columns="[
          { title: '菜单名称', dataIndex: 'name', key: 'name', width: 220 },
          { title: '类型', key: 'menuType', width: 80, align: 'center' },
          { title: '图标', dataIndex: 'icon', key: 'icon', width: 100 },
          { title: '路由路径', dataIndex: 'path', key: 'path', width: 200 },
          { title: '组件路径', dataIndex: 'component', key: 'component', width: 240 },
          { title: '权限标识', dataIndex: 'permission', key: 'permission', width: 180 },
          { title: '排序', dataIndex: 'sortOrder', key: 'sortOrder', width: 70, align: 'center' },
          { title: '可见', key: 'visible', width: 70, align: 'center' },
          { title: '操作', key: 'action', width: 200, fixed: 'right' },
        ]"
        :data-source="menuTree"
        :loading="loading"
        :pagination="false"
        :scroll="{ x: 1400 }"
        children-column-name="children"
        default-expand-all-rows
        row-key="id"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'menuType'">
            <Tag :color="menuTypeTag(record.menuType).color">
              {{ menuTypeTag(record.menuType).text }}
            </Tag>
          </template>
          <template v-if="column.key === 'visible'">
            <Tag v-if="record.menuType !== 2" :color="record.visible ? 'green' : 'red'">
              {{ record.visible ? '显示' : '隐藏' }}
            </Tag>
            <span v-else>-</span>
          </template>
          <template v-if="column.key === 'action'">
            <Space>
              <Button
                v-if="record.menuType !== 2"
                size="small"
                type="link"
                @click="handleAdd(record.id)"
              >
                新增
              </Button>
              <Button size="small" type="link" @click="handleEdit(record)">
                编辑
              </Button>
              <Popconfirm
                title="确定删除？子菜单也会被一同删除"
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
