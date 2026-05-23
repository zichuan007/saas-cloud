import {requestClient} from '#/api/request';

export interface DictTypeRecord {
  createTime?: string;
  dictType: string;
  dictName: string;
  id: number;
  remark?: string;
  status?: number;
}

export interface DictDataRecord {
  cssClass?: string;
  dictLabel: string;
  dictType?: string;
  dictValue: string;
  id: number;
  remark?: string;
  sortOrder?: number;
  status?: number;
}

export function getDictTypeList() {
  return requestClient.get<DictTypeRecord[]>('/rbac/dict/type/list');
}

export function createDictType(data: Partial<DictTypeRecord>) {
  return requestClient.post('/rbac/dict/type', data);
}

export function updateDictType(id: number, data: Partial<DictTypeRecord>) {
  return requestClient.put(`/rbac/dict/type/${id}`, data);
}

export function deleteDictType(id: number) {
  return requestClient.delete(`/rbac/dict/type/${id}`);
}

export function getDictDataByType(dictType: string) {
  return requestClient.get<DictDataRecord[]>(`/rbac/dict/data/type/${dictType}`);
}

export function createDictData(data: Partial<DictDataRecord>) {
  return requestClient.post('/rbac/dict/data', data);
}

export function updateDictData(id: number, data: Partial<DictDataRecord>) {
  return requestClient.put(`/rbac/dict/data/${id}`, data);
}

export function deleteDictData(id: number) {
  return requestClient.delete(`/rbac/dict/data/${id}`);
}
