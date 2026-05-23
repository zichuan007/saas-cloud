import {requestClient} from '#/api/request';

export interface NotifyMessage {
  content?: string;
  createTime?: string;
  id: number;
  isRead?: boolean;
  title?: string;
  type?: string;
}

export interface NotifyChannel {
  channelType: string;
  config?: Record<string, any>;
  enabled?: boolean;
  name?: string;
}

export function getMessageList(params?: Record<string, any>) {
  return requestClient.get('/notify/message/list', { params });
}

export function getUnreadCount() {
  return requestClient.get<number>('/notify/message/unread-count');
}

export function markMessageRead(id: number) {
  return requestClient.put(`/notify/message/${id}/read`);
}

export function markAllRead() {
  return requestClient.put('/notify/message/read-all');
}

export function deleteMessage(id: number) {
  return requestClient.delete(`/notify/message/${id}`);
}

export function getChannelList() {
  return requestClient.get('/notify/channel/list');
}

export function updateChannel(
  channelType: string,
  data: Partial<NotifyChannel>,
) {
  return requestClient.put(`/notify/channel/${channelType}`, data);
}

export function testChannel(channelType: string) {
  return requestClient.post(`/notify/channel/${channelType}/test`);
}
