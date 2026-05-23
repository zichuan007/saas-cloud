import {requestClient} from '#/api/request';

export interface RoleRecord {
  createTime?: string;
  dataScope?: number;
  id: number;
  menuIds?: number[];
  remark?: string;
  roleCode: string;
  roleName: string;
  sortOrder?: number;
  status?: number;
}

export function getRoleList() {
  return requestClient.get<RoleRecord[]>('/rbac/role/list');
}

export function getRoleDetail(id: number) {
  return requestClient.get<RoleRecord>(`/rbac/role/${id}`);
}

export function createRole(data: Partial<RoleRecord>) {
  return requestClient.post('/rbac/role', data);
}

export function updateRole(id: number, data: Partial<RoleRecord>) {
  return requestClient.put(`/rbac/role/${id}`, data);
}

export function deleteRole(id: number) {
  return requestClient.delete(`/rbac/role/${id}`);
}

export function updateRoleStatus(id: number, status: number) {
  return requestClient.put(`/rbac/role/${id}/status`, { status });
}

export function assignRoleMenus(id: number, menuIds: number[]) {
  return requestClient.put(`/rbac/role/${id}/menus`, { menuIds });
}

export function setRoleDataScope(id: number, dataScope: number) {
  return requestClient.put(`/rbac/role/${id}/data-scope`, { dataScope });
}

export function exportRoles() {
  return requestClient.download('/rbac/role/export', {
    responseReturn: 'raw',
  });
}
