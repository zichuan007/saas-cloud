import { requestClient } from '#/api/request';

export interface TagRecord {
  fanCount?: number;
  id: number;
  tagName: string;
}

export function getTagList(params?: Record<string, any>) {
  return requestClient.get('/wechat-oa/tag/list', { params });
}

export function createTag(data: { accountId: number; tagName: string }) {
  return requestClient.post('/wechat-oa/tag', data);
}

export function updateTag(id: number, data: { tagName: string }) {
  return requestClient.put(`/wechat-oa/tag/${id}`, data);
}

export function deleteTag(id: number) {
  return requestClient.delete(`/wechat-oa/tag/${id}`);
}

export function syncTags(accountId: number) {
  return requestClient.post('/wechat-oa/tag/sync', null, {
    params: { accountId },
  });
}
