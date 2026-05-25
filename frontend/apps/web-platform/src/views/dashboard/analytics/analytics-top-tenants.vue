<script lang="ts" setup>
import { Tag } from 'ant-design-vue';

interface TopTenantItem {
  createTime: string;
  packageName: string;
  status: number;
  tenantId: number;
  tenantName: string;
}

defineProps<{ data: TopTenantItem[] }>();

const STATUS_MAP: Record<number, { color: string; text: string }> = {
  0: { color: 'blue', text: '试用' },
  1: { color: 'green', text: '正常' },
  2: { color: 'red', text: '冻结' },
  3: { color: 'default', text: '注销' },
};
</script>

<template>
  <div class="overflow-x-auto">
    <table class="w-full text-sm">
      <thead>
        <tr class="border-b text-left">
          <th class="px-3 py-2">租户名称</th>
          <th class="px-3 py-2">套餐</th>
          <th class="px-3 py-2">状态</th>
          <th class="px-3 py-2">创建时间</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in data" :key="item.tenantId" class="border-b">
          <td class="px-3 py-2">{{ item.tenantName }}</td>
          <td class="px-3 py-2">{{ item.packageName }}</td>
          <td class="px-3 py-2">
            <Tag :color="STATUS_MAP[item.status]?.color ?? 'default'">
              {{ STATUS_MAP[item.status]?.text ?? '未知' }}
            </Tag>
          </td>
          <td class="px-3 py-2">{{ item.createTime }}</td>
        </tr>
        <tr v-if="!data || data.length === 0">
          <td class="px-3 py-4 text-center text-gray-400" colspan="4">
            暂无数据
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
