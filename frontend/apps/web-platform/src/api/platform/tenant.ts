import { requestClient } from '#/api/request';

export interface TenantRecord {
  contactPerson?: string;
  createTime?: string;
  expireTime?: string;
  id: number;
  packageId?: number;
  packageName?: string;
  phone?: string;
  status?: number;
  tenantCode: string;
  tenantName: string;
  userCount?: number;
}

export interface TenantQuery {
  packageId?: number;
  pageNum: number;
  pageSize: number;
  status?: number;
  tenantName?: string;
}

export function getTenantList(params: TenantQuery) {
  return requestClient.get('/platform/tenant/list', { params });
}

export function getTenantDetail(id: number) {
  return requestClient.get<TenantRecord>(`/platform/tenant/${id}`);
}

export function createTenant(data: Partial<TenantRecord>) {
  return requestClient.post('/platform/tenant', data);
}

export function updateTenant(id: number, data: Partial<TenantRecord>) {
  return requestClient.put(`/platform/tenant/${id}`, data);
}

export function freezeTenant(id: number) {
  return requestClient.put(`/platform/tenant/${id}/freeze`);
}

export function unfreezeTenant(id: number) {
  return requestClient.put(`/platform/tenant/${id}/unfreeze`);
}

export function changeTenantPackage(id: number, packageId: number) {
  return requestClient.put(`/platform/tenant/${id}/package`, { packageId });
}

export function deleteTenant(id: number) {
  return requestClient.delete(`/platform/tenant/${id}`);
}

export function getTenantQuota(id: number) {
  return requestClient.get(`/platform/tenant/${id}/quota`);
}
