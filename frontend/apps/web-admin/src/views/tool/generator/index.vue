<script lang="ts" setup>
import type {GenerateRequest, TableInfo} from '#/api/tool';
import {connectDatabase, downloadCode, previewCode} from '#/api/tool';

import {ref} from 'vue';

import {Page} from '@vben/common-ui';

import {
  Button,
  Card,
  Form,
  FormItem,
  Input,
  InputPassword,
  message,
  Space,
  Spin,
  Steps,
  Table,
  Tag,
} from 'ant-design-vue';

import CodePreview from './code-preview.vue';

defineOptions({ name: 'ToolGenerator' });

const currentStep = ref(0);
const loading = ref(false);

// Step 1: 数据库连接
const connectForm = ref({
  dbType: 'MYSQL',
  host: 'localhost',
  port: '3306',
  dbName: 'rbac',
  username: 'root',
  password: '',
});

// Step 2: 生成配置
const configForm = ref({
  packageName: 'com.example.demo',
  author: 'generator',
  tablePrefix: '',
});

const tables = ref<TableInfo[]>([]);
const selectedRowKeys = ref<string[]>([]);

// Step 3: 预览
const previewFiles = ref<Record<string, string>>({});
const previewTableName = ref('');

const tableColumns = [
  { title: '表名', dataIndex: 'name', key: 'name' },
  { title: '注释', dataIndex: 'comment', key: 'comment' },
];

async function handleConnect() {
  if (!connectForm.value.host || !connectForm.value.username) {
    message.warning('请填写连接信息');
    return;
  }
  loading.value = true;
  try {
    const res = await connectDatabase(connectForm.value);
    tables.value = res;
    message.success(`连接成功，发现 ${res.length} 张表`);
    currentStep.value = 1;
  } catch {
    message.error('连接失败，请检查连接信息');
  } finally {
    loading.value = false;
  }
}

function buildRequest(): GenerateRequest {
  const prefix = configForm.value.tablePrefix.trim();
  return {
    ...connectForm.value,
    packageName: configForm.value.packageName,
    author: configForm.value.author || 'generator',
    tablePrefix: prefix ? [prefix] : [],
    tables: selectedRowKeys.value,
  };
}

async function handlePreview(tableName: string) {
  loading.value = true;
  previewTableName.value = tableName;
  try {
    const req = {
      ...buildRequest(),
      previewTable: tableName,
    };
    const res = await previewCode(req);
    previewFiles.value = res;
    currentStep.value = 2;
  } catch {
    message.error('预览失败');
  } finally {
    loading.value = false;
  }
}

async function handleDownload() {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请选择要生成的表');
    return;
  }
  if (!configForm.value.packageName) {
    message.warning('请填写包名');
    return;
  }
  loading.value = true;
  try {
    const res = await downloadCode(buildRequest());
    // baseRequestClient 返回完整 AxiosResponse，data 才是 blob
    const blobData = (res as any)?.data ?? res;
    const blob = new Blob([blobData], { type: 'application/zip' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = 'generated-code.zip';
    link.click();
    window.URL.revokeObjectURL(url);
    message.success('下载成功');
  } catch {
    message.error('下载失败');
  } finally {
    loading.value = false;
  }
}

function handleBack(step: number) {
  currentStep.value = step;
}
</script>

<template>
  <Page auto-content-height>
    <Spin :spinning="loading">
      <Card>
        <Steps :current="currentStep" style="margin-bottom: 24px">
          <Steps.Step title="连接数据库" />
          <Steps.Step title="选择表 & 配置" />
          <Steps.Step title="代码预览" />
        </Steps>

        <!-- Step 1: 连接数据库 -->
        <div v-show="currentStep === 0">
          <Form
            :label-col="{ span: 4 }"
            :wrapper-col="{ span: 14 }"
            style="max-width: 600px; margin: 40px auto"
          >
            <FormItem label="数据库类型">
              <select
                v-model="connectForm.dbType"
                style="width: 100%; height: 32px; border: 1px solid #d9d9d9; border-radius: 6px; padding: 0 8px"
              >
                <option value="MYSQL">MySQL</option>
                <option value="POSTGRESQL">PostgreSQL</option>
                <option value="ORACLE">Oracle</option>
              </select>
            </FormItem>
            <FormItem label="主机地址">
              <Input
                v-model:value="connectForm.host"
                placeholder="localhost"
              />
            </FormItem>
            <FormItem label="端口">
              <Input
                v-model:value="connectForm.port"
                placeholder="3306"
              />
            </FormItem>
            <FormItem label="数据库名">
              <Input
                v-model:value="connectForm.dbName"
                placeholder="rbac"
              />
            </FormItem>
            <FormItem label="用户名">
              <Input
                v-model:value="connectForm.username"
                placeholder="数据库用户名"
              />
            </FormItem>
            <FormItem label="密码">
              <InputPassword
                v-model:value="connectForm.password"
                placeholder="数据库密码"
              />
            </FormItem>
            <FormItem :wrapper-col="{ offset: 4 }">
              <Button type="primary" @click="handleConnect">
                连接并获取表
              </Button>
            </FormItem>
          </Form>
        </div>

        <!-- Step 2: 选表 & 配置 -->
        <div v-show="currentStep === 1">
          <Form
            layout="inline"
            style="margin-bottom: 16px"
          >
            <FormItem label="包名">
              <Input
                v-model:value="configForm.packageName"
                placeholder="com.example.demo"
                style="width: 260px"
              />
            </FormItem>
            <FormItem label="作者">
              <Input
                v-model:value="configForm.author"
                placeholder="generator"
                style="width: 140px"
              />
            </FormItem>
            <FormItem label="表前缀">
              <Input
                v-model:value="configForm.tablePrefix"
                placeholder="如 t_，生成时去除"
                style="width: 160px"
              />
            </FormItem>
          </Form>

          <Table
            :columns="tableColumns"
            :data-source="tables"
            :row-key="(r: any) => r.name"
            :row-selection="{
              selectedRowKeys,
              onChange: (keys: any) => (selectedRowKeys = keys),
            }"
            :pagination="{ pageSize: 20, showSizeChanger: true, showTotal: (t: number) => `共 ${t} 张表` }"
            size="small"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'name'">
                <Button
                  type="link"
                  size="small"
                  @click="handlePreview(record.name)"
                >
                  {{ record.name }}
                </Button>
              </template>
            </template>
          </Table>

          <div style="margin-top: 16px; text-align: right">
            <Space>
              <Button @click="handleBack(0)">上一步</Button>
              <Button
                type="primary"
                :disabled="selectedRowKeys.length === 0"
                @click="handleDownload"
              >
                下载代码 ({{ selectedRowKeys.length }} 张表)
              </Button>
            </Space>
          </div>
        </div>

        <!-- Step 3: 代码预览 -->
        <div v-show="currentStep === 2">
          <div style="margin-bottom: 12px">
            <Space>
              <Button @click="handleBack(1)">返回选表</Button>
              <Tag color="blue">{{ previewTableName }}</Tag>
            </Space>
          </div>
          <CodePreview :files="previewFiles" />
        </div>
      </Card>
    </Spin>
  </Page>
</template>
