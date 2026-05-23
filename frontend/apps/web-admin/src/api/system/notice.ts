import {requestClient} from '#/api/request';

export interface NoticeRecord {
  content?: string;
  createTime?: string;
  id: number;
  noticeType?: number;
  publishTime?: string;
  read?: boolean;
  remark?: string;
  status?: number;
  title: string;
}

export function getNoticeList(params?: Record<string, any>) {
  return requestClient.get('/rbac/notice/list', { params });
}

export function getPublishedNotices(params?: Record<string, any>) {
  return requestClient.get('/rbac/notice/published', { params });
}

export function createNotice(data: Partial<NoticeRecord>) {
  return requestClient.post('/rbac/notice', data);
}

export function updateNotice(id: number, data: Partial<NoticeRecord>) {
  return requestClient.put(`/rbac/notice/${id}`, data);
}

export function publishNotice(id: number) {
  return requestClient.put(`/rbac/notice/${id}/publish`);
}

export function revokeNotice(id: number) {
  return requestClient.put(`/rbac/notice/${id}/revoke`);
}

export function deleteNotice(id: number) {
  return requestClient.delete(`/rbac/notice/${id}`);
}

export function markNoticeRead(id: number) {
  return requestClient.put(`/rbac/notice/${id}/read`);
}

export function getUnreadNoticeCount() {
  return requestClient.get<number>('/rbac/notice/unread-count');
}
