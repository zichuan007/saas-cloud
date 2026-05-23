<script lang="ts" setup>
import {ref} from 'vue';

import {Page, useVbenModal} from '@vben/common-ui';

import {
  Button,
  Card,
  Col,
  Form,
  FormItem,
  Input,
  message,
  Popconfirm,
  Row,
  Space,
  Table,
} from 'ant-design-vue';

import {
  createDictType,
  deleteDictData,
  deleteDictType,
  type DictDataRecord,
  type DictTypeRecord,
  getDictDataByType,
  getDictTypeList,
  updateDictType,
} from '#/api/system/dict';

import DictDataModal from './dict-data-modal.vue';

defineOptions({ name: 'SystemDict' });

const [DataFormModal, dataFormModalApi] = useVbenModal({
  connectedComponent: DictDataModal,
});

const typeList = ref<DictTypeRecord[]>([]);
const dataList = ref<DictDataRecord[]>([]);
const typeLoading = ref(false);
const dataLoading = ref(false);
const selectedType = ref<DictTypeRecord | null>(null);
const editingType = ref<Record<string, any> | null>(null);

async function loadTypes() {
  typeLoading.value = true;
  try {
    typeList.value = (await getDictTypeList()) as DictTypeRecord[];
  } finally {
    typeLoading.value = false;
  }
}
loadTypes();

async function handleSelectType(record: DictTypeRecord) {
  selectedType.value = record;
  dataLoading.value = true;
  try {
    dataList.value = (await getDictDataByType(
      record.dictType,
    )) as DictDataRecord[];
  } finally {
    dataLoading.value = false;
  }
}

function handleAddType() {
  editingType.value = { dictName: '', dictType: '', remark: '' };
}

function handleEditType(record: DictTypeRecord) {
  editingType.value = { ...record };
}

async function handleSaveType() {
  if (!editingType.value) return;
  if (editingType.value.id) {
    await updateDictType(editingType.value.id, editingType.value);
    message.success('更新成功');
  } else {
    await createDictType(editingType.value);
    message.success('创建成功');
  }
  editingType.value = null;
  await loadTypes();
}

async function handleDeleteType(id: number) {
  await deleteDictType(id);
  message.success('删除成功');
  if (selectedType.value?.id === id) {
    selectedType.value = null;
    dataList.value = [];
  }
  await loadTypes();
}

function handleAddData() {
  if (!selectedType.value) {
    message.warning('请先选择字典类型');
    return;
  }
  dataFormModalApi.setData({
    dictType: selectedType.value.dictType,
    mode: 'add',
  });
  dataFormModalApi.open();
}

function handleEditData(record: DictDataRecord) {
  dataFormModalApi.setData({ mode: 'edit', record });
  dataFormModalApi.open();
}

async function handleDeleteData(id: number) {
  await deleteDictData(id);
  message.success('删除成功');
  if (selectedType.value) {
    await handleSelectType(selectedType.value);
  }
}

async function handleDataSuccess() {
  if (selectedType.value) {
    await handleSelectType(selectedType.value);
  }
}
</script>

<template>
  <Page auto-content-height>
    <DataFormModal @success="handleDataSuccess" />
    <Row :gutter="16" class="p-4">
      <Col :span="10">
        <Card title="字典类型">
          <template #extra>
            <Button size="small" type="primary" @click="handleAddType">
              新增
            </Button>
          </template>
          <div v-if="editingType" class="mb-4 rounded border p-3">
            <Form :label-col="{ span: 6 }" :model="editingType">
              <FormItem label="类型名称">
                <Input
                  v-model:value="editingType.dictName"
                  placeholder="类型名称"
                />
              </FormItem>
              <FormItem label="类型编码">
                <Input
                  v-model:value="editingType.dictType"
                  :disabled="!!editingType.id"
                  placeholder="类型编码"
                />
              </FormItem>
              <FormItem label="备注">
                <Input v-model:value="editingType.remark" placeholder="备注" />
              </FormItem>
              <div class="text-right">
                <Button class="mr-2" @click="editingType = null">取消</Button>
                <Button type="primary" @click="handleSaveType">保存</Button>
              </div>
            </Form>
          </div>
          <Table
            :columns="[
              { title: '类型名称', dataIndex: 'dictName', key: 'dictName' },
              {
                title: '类型编码',
                dataIndex: 'dictType',
                key: 'dictType',
                ellipsis: true,
              },
              { title: '操作', key: 'action', width: 150 },
            ]"
            :data-source="typeList"
            :loading="typeLoading"
            :pagination="false"
            :row-class-name="
              (record: any) =>
                selectedType?.id === record.id ? 'ant-table-row-selected' : ''
            "
            row-key="id"
            size="small"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'dictName'">
                <a @click="handleSelectType(record as DictTypeRecord)">
                  {{ record.dictName }}
                </a>
              </template>
              <template v-if="column.key === 'action'">
                <Space>
                  <Button
                    size="small"
                    type="link"
                    @click="handleEditType(record as DictTypeRecord)"
                  >
                    编辑
                  </Button>
                  <Popconfirm
                    title="确定删除？"
                    @confirm="handleDeleteType(record.id)"
                  >
                    <Button danger size="small" type="link">删除</Button>
                  </Popconfirm>
                </Space>
              </template>
            </template>
          </Table>
        </Card>
      </Col>
      <Col :span="14">
        <Card :title="selectedType ? `字典数据 - ${selectedType.dictName}` : '字典数据'">
          <template #extra>
            <Button size="small" type="primary" @click="handleAddData">
              新增
            </Button>
          </template>
          <Table
            :columns="[
              { title: '字典标签', dataIndex: 'dictLabel', key: 'dictLabel' },
              { title: '字典值', dataIndex: 'dictValue', key: 'dictValue' },
              {
                title: '排序',
                dataIndex: 'sortOrder',
                key: 'sortOrder',
                width: 80,
              },
              { title: '操作', key: 'action', width: 150 },
            ]"
            :data-source="dataList"
            :loading="dataLoading"
            :pagination="false"
            row-key="id"
            size="small"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'action'">
                <Space>
                  <Button
                    size="small"
                    type="link"
                    @click="handleEditData(record as DictDataRecord)"
                  >
                    编辑
                  </Button>
                  <Popconfirm
                    title="确定删除？"
                    @confirm="handleDeleteData(record.id)"
                  >
                    <Button danger size="small" type="link">删除</Button>
                  </Popconfirm>
                </Space>
              </template>
            </template>
          </Table>
        </Card>
      </Col>
    </Row>
  </Page>
</template>
