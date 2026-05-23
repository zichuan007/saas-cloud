import {requestClient} from '#/api/request';

export function getTenantSelfInfo() {
  return requestClient.get('/rbac/tenant-self/info');
}

export function getTenantQuota() {
  return requestClient.get('/rbac/tenant-self/quota');
}
