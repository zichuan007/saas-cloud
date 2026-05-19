import { requestClient } from '#/api/request';

export interface AnnouncementRecord {
  content?: string;
  createTime?: string;
  id: number;
  publishTime?: string;
  status?: number;
  title: string;
}

export function getAnnouncementList(params?: Record<string, any>) {
  return requestClient.get('/platform/announcement/list', { params });
}

export function createAnnouncement(data: Partial<AnnouncementRecord>) {
  return requestClient.post('/platform/announcement', data);
}

export function updateAnnouncement(
  id: number,
  data: Partial<AnnouncementRecord>,
) {
  return requestClient.put(`/platform/announcement/${id}`, data);
}

export function publishAnnouncement(id: number) {
  return requestClient.post(`/platform/announcement/${id}/publish`);
}

export function offlineAnnouncement(id: number) {
  return requestClient.put(`/platform/announcement/${id}/offline`);
}
