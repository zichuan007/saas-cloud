import type {UserInfo} from '@vben/types';

import {requestClient} from '#/api/request';

/**
 * 获取当前用户信息
 */
export async function getUserInfoApi() {
  return requestClient.get<UserInfo>('/platform/auth/user-info');
}
