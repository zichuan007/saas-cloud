<script lang="ts" setup>
import { ref, watch } from 'vue';

import { Page } from '@vben/common-ui';

import {
  Card,
  Col,
  Empty,
  Row,
  Select,
  Statistic,
  Table,
} from 'ant-design-vue';

import { getArticleRank, getFanTrend } from '#/api/wechat/dashboard';
import { useWechatAccount } from '../use-account';

defineOptions({ name: 'WechatDashboard' });

const { accountList, currentAccountId, loadAccounts } = useWechatAccount();
loadAccounts();

const fanTrend = ref<any[]>([]);
const articleRank = ref<any[]>([]);
const loading = ref(false);

async function loadData() {
  if (!currentAccountId.value) return;
  loading.value = true;
  try {
    const [trend, rank] = await Promise.all([
      getFanTrend({ accountId: currentAccountId.value }),
      getArticleRank({ accountId: currentAccountId.value }),
    ]);
    fanTrend.value = (trend as any[]) ?? [];
    articleRank.value = (rank as any[]) ?? [];
  } finally {
    loading.value = false;
  }
}

watch(currentAccountId, (val) => {
  if (val) loadData();
});

const totalNew = ref(0);
const totalUnfollow = ref(0);
const totalNet = ref(0);

watch(fanTrend, (data) => {
  totalNew.value = data.reduce((s, d) => s + (d.newCount ?? 0), 0);
  totalUnfollow.value = data.reduce((s, d) => s + (d.unfollowCount ?? 0), 0);
  totalNet.value = totalNew.value - totalUnfollow.value;
});

const articleColumns = [
  { dataIndex: 'title', title: '标题' },
  { dataIndex: 'readCount', title: '阅读数', width: 100 },
  { dataIndex: 'likeCount', title: '点赞数', width: 100 },
  { dataIndex: 'shareCount', title: '分享数', width: 100 },
];
</script>

<template>
  <Page auto-content-height>
    <div class="p-4">
      <div class="mb-4 flex items-center justify-between">
        <h3 class="text-lg font-medium">数据看板</h3>
        <Select
          v-model:value="currentAccountId"
          :options="
            accountList.map((a) => ({ label: a.accountName, value: a.id }))
          "
          placeholder="选择公众号"
          style="width: 200px"
          @change="loadData"
        />
      </div>

      <Row :gutter="16" class="mb-4">
        <Col :span="8">
          <Card>
            <Statistic
              title="新增关注"
              :value="totalNew"
              :value-style="{ color: '#3f8600' }"
            />
          </Card>
        </Col>
        <Col :span="8">
          <Card>
            <Statistic
              title="取消关注"
              :value="totalUnfollow"
              :value-style="{ color: '#cf1322' }"
            />
          </Card>
        </Col>
        <Col :span="8">
          <Card>
            <Statistic title="净增关注" :value="totalNet" />
          </Card>
        </Col>
      </Row>

      <Row :gutter="16">
        <Col :span="12">
          <Card title="粉丝趋势">
            <Empty v-if="fanTrend.length === 0" description="暂无数据" />
            <Table
              v-else
              :columns="[
                { title: '日期', dataIndex: 'date', width: 120 },
                { title: '新增', dataIndex: 'newCount', width: 80 },
                { title: '取关', dataIndex: 'unfollowCount', width: 80 },
                { title: '净增', dataIndex: 'netCount', width: 80 },
              ]"
              :data-source="fanTrend"
              :pagination="false"
              row-key="date"
              size="small"
            />
          </Card>
        </Col>
        <Col :span="12">
          <Card title="图文排行">
            <Empty v-if="articleRank.length === 0" description="暂无数据" />
            <Table
              v-else
              :columns="articleColumns"
              :data-source="articleRank"
              :pagination="false"
              row-key="title"
              size="small"
            />
          </Card>
        </Col>
      </Row>
    </div>
  </Page>
</template>
