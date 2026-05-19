import { requestClient } from '#/api/request';

export interface ArticleRecord {
  author?: string;
  content?: string;
  coverUrl?: string;
  createTime?: string;
  digest?: string;
  id: number;
  publishTime?: string;
  status?: number;
  title: string;
}

export function getArticleList(params?: Record<string, any>) {
  return requestClient.get('/wechat-oa/article/list', { params });
}

export function getArticleDetail(id: number) {
  return requestClient.get<ArticleRecord>(`/wechat-oa/article/${id}`);
}

export function createArticle(data: Partial<ArticleRecord>) {
  return requestClient.post('/wechat-oa/article', data);
}

export function updateArticle(id: number, data: Partial<ArticleRecord>) {
  return requestClient.put(`/wechat-oa/article/${id}`, data);
}

export function deleteArticle(id: number) {
  return requestClient.delete(`/wechat-oa/article/${id}`);
}

export function previewArticle(id: number) {
  return requestClient.post(`/wechat-oa/article/${id}/preview`);
}

export function publishArticle(id: number) {
  return requestClient.post(`/wechat-oa/article/${id}/publish`);
}

export function offlineArticle(id: number) {
  return requestClient.put(`/wechat-oa/article/${id}/offline`);
}
