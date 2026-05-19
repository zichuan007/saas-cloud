import { requestClient } from '#/api/request';

export interface AutoReplyRule {
  accountId?: number;
  createTime?: string;
  id: number;
  keyword?: string;
  matchType?: number;
  replyContent?: string;
  replyType?: number;
  ruleName: string;
  ruleType?: number;
  status?: number;
}

export function getAutoReplyList(params?: Record<string, any>) {
  return requestClient.get('/wechat-oa/auto-reply/list', { params });
}

export function createAutoReply(data: Partial<AutoReplyRule>) {
  return requestClient.post('/wechat-oa/auto-reply', data);
}

export function updateAutoReply(id: number, data: Partial<AutoReplyRule>) {
  return requestClient.put(`/wechat-oa/auto-reply/${id}`, data);
}

export function deleteAutoReply(id: number) {
  return requestClient.delete(`/wechat-oa/auto-reply/${id}`);
}

export function updateAutoReplyStatus(id: number, status: number) {
  return requestClient.put(`/wechat-oa/auto-reply/${id}/status`, null, {
    params: { status },
  });
}
