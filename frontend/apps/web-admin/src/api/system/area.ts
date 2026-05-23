import {requestClient} from '#/api/request';

export interface AreaRecord {
  areaCode?: string;
  areaLevel?: number;
  areaName: string;
  children?: AreaRecord[];
  firstLetter?: string;
  id: number;
  parentCode?: string;
  pinyin?: string;
}

export function getAreaChildren(parentCode: string) {
  return requestClient.get<AreaRecord[]>(`/rbac/area/children/${parentCode}`);
}

export function searchArea(keyword: string) {
  return requestClient.get<AreaRecord[]>('/rbac/area/search', {
    params: { keyword },
  });
}

export function getAreaByCode(areaCode: string) {
  return requestClient.get<AreaRecord>(`/rbac/area/code/${areaCode}`);
}

export function getAreaPath(areaCode: string) {
  return requestClient.get<AreaRecord[]>(`/rbac/area/path/${areaCode}`);
}
