import {requestClient} from '#/api/request';

export function getCacheInfo() {
  return requestClient.get('/rbac/monitor/cache/info');
}

export function getCacheKeys(params?: Record<string, any>) {
  return requestClient.get('/rbac/monitor/cache/keys', { params });
}

export function deleteCacheKey(key: string) {
  return requestClient.delete(`/rbac/monitor/cache/key/${key}`);
}
