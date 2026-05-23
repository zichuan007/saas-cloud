import {requestClient} from '#/api/request';

export interface OnlineUserRecord {
  browser?: string;
  ip?: string;
  loginTime?: string;
  os?: string;
  tokenValue?: string;
  userId?: number;
  username?: string;
}

export function getOnlineUserList(params?: Record<string, any>) {
  return requestClient.get('/rbac/online-user/list', { params });
}

export function kickOnlineUser(tokenValue: string) {
  return requestClient.delete(`/rbac/online-user/${tokenValue}`);
}
