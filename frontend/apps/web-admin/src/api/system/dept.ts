import { requestClient } from '#/api/request';

export interface DeptRecord {
  ancestors?: string;
  children?: DeptRecord[];
  createTime?: string;
  deptName: string;
  id: number;
  leaderUserId?: number;
  leaderUserName?: string;
  parentId: number;
  sortOrder?: number;
  status?: number;
}

export function getDeptTree() {
  return requestClient.get<DeptRecord[]>('/rbac/dept/tree');
}

export function getDeptDetail(id: number) {
  return requestClient.get<DeptRecord>(`/rbac/dept/${id}`);
}

export function createDept(data: Partial<DeptRecord>) {
  return requestClient.post('/rbac/dept', data);
}

export function updateDept(id: number, data: Partial<DeptRecord>) {
  return requestClient.put(`/rbac/dept/${id}`, data);
}

export function deleteDept(id: number) {
  return requestClient.delete(`/rbac/dept/${id}`);
}
