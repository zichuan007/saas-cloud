import { requestClient } from '#/api/request';

export interface MaterialRecord {
  createTime?: string;
  id: number;
  mediaId?: string;
  name?: string;
  type?: string;
  url?: string;
}

export function getMaterialList(params?: Record<string, any>) {
  return requestClient.get('/wechat-oa/material/list', { params });
}

export function uploadMaterial(data: FormData) {
  return requestClient.post('/wechat-oa/material/upload', data, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
}

export function deleteMaterial(id: number) {
  return requestClient.delete(`/wechat-oa/material/${id}`);
}

export function syncMaterial(id: number) {
  return requestClient.post(`/wechat-oa/material/${id}/sync`);
}
