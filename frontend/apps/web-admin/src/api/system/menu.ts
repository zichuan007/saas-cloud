import {requestClient} from '#/api/request';

export interface MenuRecord {
  children?: MenuRecord[];
  component?: string;
  icon?: string;
  id: number;
  isCached?: number;
  isExternal?: number;
  menuName?: string;
  menuType: number;
  module?: string;
  name?: string;
  parentId: number;
  path?: string;
  permission?: string;
  sortOrder?: number;
  status?: number;
  visible?: number;
}

export function getMenuTree() {
  return requestClient.get<MenuRecord[]>('/rbac/menu/tree');
}

export function createMenu(data: Partial<MenuRecord>) {
  return requestClient.post('/rbac/menu', data);
}

export function updateMenu(id: number, data: Partial<MenuRecord>) {
  return requestClient.put(`/rbac/menu/${id}`, data);
}

export function deleteMenu(id: number) {
  return requestClient.delete(`/rbac/menu/${id}`);
}
