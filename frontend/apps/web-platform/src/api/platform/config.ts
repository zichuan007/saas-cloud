import { requestClient } from '#/api/request';

export interface ConfigItem {
  description?: string;
  key: string;
  value: string;
}

export function getConfigList() {
  return requestClient.get<ConfigItem[]>('/platform/config/list');
}

export function updateConfig(key: string, value: string) {
  return requestClient.put(`/platform/config/${key}`, { value });
}
