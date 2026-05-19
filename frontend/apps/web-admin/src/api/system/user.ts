import { requestClient } from '#/api/request';

export interface UserRecord {
  avatar?: string;
  createTime?: string;
  deptId?: number;
  deptName?: string;
  id: number;
  phone?: string;
  realName?: string;
  roleIds?: number[];
  roles?: string[];
  status?: number;
  username: string;
}

export interface UserQuery {
  deptId?: number;
  pageNum: number;
  pageSize: number;
  realName?: string;
  status?: number;
  username?: string;
}

export function getUserList(params: UserQuery) {
  return requestClient.get('/rbac/user/list', { params });
}

export function getUserDetail(id: number) {
  return requestClient.get<UserRecord>(`/rbac/user/${id}`);
}

export function createUser(data: Partial<UserRecord> & { password: string }) {
  return requestClient.post('/rbac/user', data);
}

export function updateUser(id: number, data: Partial<UserRecord>) {
  return requestClient.put(`/rbac/user/${id}`, data);
}

export function deleteUser(id: number) {
  return requestClient.delete(`/rbac/user/${id}`);
}

export function updateUserStatus(id: number, status: number) {
  return requestClient.put(`/rbac/user/${id}/status`, { status });
}

export function resetUserPassword(id: number, password: string) {
  return requestClient.put(`/rbac/user/${id}/reset-password`, { password });
}

export function updateProfile(data: Partial<UserRecord>) {
  return requestClient.put('/rbac/user/profile', data);
}

export function updatePassword(oldPassword: string, newPassword: string) {
  return requestClient.put('/rbac/user/password', { newPassword, oldPassword });
}
