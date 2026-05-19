import { requestClient } from '#/api/request';

export interface PackageRecord {
  createTime?: string;
  description?: string;
  id: number;
  maxDeptCount?: number;
  maxMenuCount?: number;
  maxProcessDefinition?: number;
  maxRoleCount?: number;
  maxUserCount?: number;
  maxWechatAccount?: number;
  packageName: string;
  price?: number;
  status?: number;
}

export function getPackageList() {
  return requestClient.get('/platform/package/list');
}

export function getPackageDetail(id: number) {
  return requestClient.get<PackageRecord>(`/platform/package/${id}`);
}

export function createPackage(data: Partial<PackageRecord>) {
  return requestClient.post('/platform/package', data);
}

export function updatePackage(id: number, data: Partial<PackageRecord>) {
  return requestClient.put(`/platform/package/${id}`, data);
}

export function updatePackageStatus(id: number, status: number) {
  return requestClient.put(`/platform/package/${id}/status`, { status });
}
