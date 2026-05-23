import {requestClient} from '#/api/request';

export interface LoginLogRecord {
  browser?: string;
  createTime?: string;
  id: number;
  ip?: string;
  location?: string;
  loginTime?: string;
  message?: string;
  os?: string;
  status?: number;
  username?: string;
}

export function getLoginLogPage(params?: Record<string, any>) {
  return requestClient.get('/rbac/login-log/page', { params });
}

export function cleanLoginLogs(keepDays: number = 90) {
  return requestClient.delete('/rbac/login-log/clean', {
    params: { keepDays },
  });
}
