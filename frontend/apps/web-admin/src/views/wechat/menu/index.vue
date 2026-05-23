<script lang="ts" setup>
import {ref} from 'vue';

import {Page} from '@vben/common-ui';

import {
  Button,
  Card,
  Col,
  Empty,
  Form,
  FormItem,
  Input,
  message,
  Row,
  Select,
  Space,
} from 'ant-design-vue';

import {getMenuList, publishMenu, saveMenu, type WechatMenuButton,} from '#/api/wechat/menu';
import {useWechatAccount} from '../use-account';

defineOptions({ name: 'WechatMenu' });

const { accountList, currentAccountId, loadAccounts } = useWechatAccount();
loadAccounts();

const menuButtons = ref<WechatMenuButton[]>([]);
const selectedButton = ref<WechatMenuButton | null>(null);
const loading = ref(false);

function flatToTree(list: any[]): WechatMenuButton[] {
  const roots: WechatMenuButton[] = [];
  const childMap: Record<number, WechatMenuButton[]> = {};
  for (const item of list) {
    const btn: WechatMenuButton = {
      name: item.menuName ?? '',
      type: item.menuType ?? 'click',
      key: item.menuKey,
      url: item.menuUrl,
      _id: item.id,
    };
    if (!item.parentId || item.parentId === 0) {
      btn.subButtons = [];
      roots.push(btn);
    } else {
      if (!childMap[item.parentId]) childMap[item.parentId] = [];
      childMap[item.parentId]!.push(btn);
    }
  }
  for (const root of roots) {
    root.subButtons = childMap[(root as any)._id] ?? [];
  }
  return roots;
}

async function loadMenu() {
  if (!currentAccountId.value) return;
  loading.value = true;
  try {
    const data = (await getMenuList({
      accountId: currentAccountId.value,
    })) as any[];
    menuButtons.value = flatToTree(data ?? []);
  } finally {
    loading.value = false;
  }
}

function handleAccountChange() {
  selectedButton.value = null;
  loadMenu();
}

function handleSelectButton(btn: WechatMenuButton) {
  selectedButton.value = btn;
}

function handleAddButton() {
  if (menuButtons.value.length >= 3) {
    message.warning('一级菜单最多3个');
    return;
  }
  const btn: WechatMenuButton = { name: '新菜单', subButtons: [], type: 'click' };
  menuButtons.value.push(btn);
  selectedButton.value = btn;
}

function handleAddSubButton(parent: WechatMenuButton) {
  if (!parent.subButtons) parent.subButtons = [];
  if (parent.subButtons.length >= 5) {
    message.warning('子菜单最多5个');
    return;
  }
  const sub: WechatMenuButton = { name: '子菜单', type: 'view' };
  parent.subButtons.push(sub);
  selectedButton.value = sub;
}

function handleDeleteButton(index: number) {
  menuButtons.value.splice(index, 1);
  selectedButton.value = null;
}

async function handleSave() {
  await saveMenu({
    accountId: currentAccountId.value,
    buttons: menuButtons.value,
  });
  message.success('保存成功');
}

async function handlePublish() {
  await publishMenu({ accountId: currentAccountId.value });
  message.success('发布成功');
}
</script>

<template>
  <Page auto-content-height>
    <div class="p-4">
      <div class="mb-4 flex items-center justify-between">
        <h3 class="text-lg font-medium">菜单编辑</h3>
        <Space>
          <Select
            v-model:value="currentAccountId"
            :options="
              accountList.map((a) => ({ label: a.accountName, value: a.id }))
            "
            placeholder="选择公众号"
            style="width: 200px"
            @change="handleAccountChange"
          />
          <Button @click="loadMenu">刷新</Button>
          <Button type="primary" @click="handleSave">保存</Button>
          <Button @click="handlePublish">发布到微信</Button>
        </Space>
      </div>

      <Row :gutter="16">
        <Col :span="10">
          <Card title="菜单结构">
            <Empty v-if="menuButtons.length === 0" description="暂无菜单" />
            <div v-for="(btn, idx) in menuButtons" :key="idx" class="mb-3">
              <div class="flex items-center justify-between">
                <Button
                  :type="selectedButton === btn ? 'primary' : 'default'"
                  block
                  @click="handleSelectButton(btn)"
                >
                  {{ btn.name }}
                </Button>
                <Button
                  class="ml-2"
                  danger
                  size="small"
                  type="text"
                  @click="handleDeleteButton(idx)"
                >
                  删
                </Button>
              </div>
              <div v-if="btn.subButtons?.length" class="ml-6 mt-1">
                <Button
                  v-for="sub in btn.subButtons"
                  :key="sub.name"
                  :type="selectedButton === sub ? 'primary' : 'default'"
                  block
                  class="mb-1"
                  size="small"
                  @click="handleSelectButton(sub)"
                >
                  {{ sub.name }}
                </Button>
              </div>
              <Button
                class="ml-6 mt-1"
                size="small"
                type="dashed"
                @click="handleAddSubButton(btn)"
              >
                + 子菜单
              </Button>
            </div>
            <Button class="mt-2" block type="dashed" @click="handleAddButton">
              + 一级菜单
            </Button>
          </Card>
        </Col>
        <Col :span="14">
          <Card title="菜单配置">
            <Empty v-if="!selectedButton" description="请选择左侧菜单" />
            <Form
              v-else
              :label-col="{ span: 4 }"
              :wrapper-col="{ span: 18 }"
            >
              <FormItem label="名称">
                <Input v-model:value="selectedButton.name" />
              </FormItem>
              <FormItem label="类型">
                <Select
                  v-model:value="selectedButton.type"
                  :options="[
                    { label: '跳转网页', value: 'view' },
                    { label: '点击事件', value: 'click' },
                    { label: '小程序', value: 'miniprogram' },
                  ]"
                />
              </FormItem>
              <FormItem v-if="selectedButton.type === 'view'" label="链接">
                <Input
                  v-model:value="selectedButton.url"
                  placeholder="请输入网页链接"
                />
              </FormItem>
              <FormItem v-if="selectedButton.type === 'click'" label="Key">
                <Input
                  v-model:value="selectedButton.key"
                  placeholder="请输入事件Key"
                />
              </FormItem>
            </Form>
          </Card>
        </Col>
      </Row>
    </div>
  </Page>
</template>
