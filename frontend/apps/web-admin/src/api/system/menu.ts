import { requestClient } from '#/api/request';

export interface MenuRecord {
  children?: MenuRecord[];
  component?: string;
  icon?: string;
  id: number;
  menuName: string;
  menuType: number;
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
