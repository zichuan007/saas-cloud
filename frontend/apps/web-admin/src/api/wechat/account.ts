import { requestClient } from '#/api/request';

export interface WechatAccount {
  accountName: string;
  accountType?: number;
  appId?: string;
  appSecret?: string;
  createTime?: string;
  id: number;
  isVerified?: number;
  status?: number;
  token?: string;
}

export function getAccountList() {
  return requestClient.get('/wechat-oa/account/list');
}

export function getAccountDetail(id: number) {
  return requestClient.get<WechatAccount>(`/wechat-oa/account/${id}`);
}

export function createAccount(data: Partial<WechatAccount>) {
  return requestClient.post('/wechat-oa/account', data);
}

export function updateAccount(id: number, data: Partial<WechatAccount>) {
  return requestClient.put(`/wechat-oa/account/${id}`, data);
}

export function deleteAccount(id: number) {
  return requestClient.delete(`/wechat-oa/account/${id}`);
}
