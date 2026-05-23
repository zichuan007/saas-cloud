import {requestClient} from '#/api/request';

export interface FanRecord {
  city?: string;
  headImgUrl?: string;
  id: number;
  isBlacklist?: boolean;
  nickname?: string;
  openId?: string;
  province?: string;
  subscribeTime?: string;
  tagIds?: number[];
}

export function getFanList(params?: Record<string, any>) {
  return requestClient.get('/wechat-oa/fan/list', { params });
}

export function syncFans() {
  return requestClient.post('/wechat-oa/fan/sync');
}

export function setFanBlacklist(id: number, blacklist: boolean) {
  return requestClient.put(`/wechat-oa/fan/${id}/blacklist`, { blacklist });
}

export function setFanTags(id: number, tagIds: number[]) {
  return requestClient.put(`/wechat-oa/fan/${id}/tags`, { tagIds });
}
