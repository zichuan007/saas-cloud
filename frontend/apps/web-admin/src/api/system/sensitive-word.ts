import {requestClient} from '#/api/request';

export interface SensitiveWordRecord {
  category?: string;
  createTime?: string;
  id: number;
  status?: number;
  word: string;
}

export function getSensitiveWordList(params?: Record<string, any>) {
  return requestClient.get('/rbac/sensitive-word/list', { params });
}

export function createSensitiveWord(data: Partial<SensitiveWordRecord>) {
  return requestClient.post('/rbac/sensitive-word', data);
}

export function deleteSensitiveWord(id: number) {
  return requestClient.delete(`/rbac/sensitive-word/${id}`);
}

export function checkSensitiveText(text: string) {
  return requestClient.post('/rbac/sensitive-word/check', { text });
}

export function filterSensitiveText(text: string) {
  return requestClient.post<string>('/rbac/sensitive-word/filter', { text });
}
