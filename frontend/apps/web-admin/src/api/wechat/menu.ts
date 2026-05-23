import {requestClient} from '#/api/request';

export interface WechatMenu {
  buttons: WechatMenuButton[];
}

export interface WechatMenuButton {
  _id?: number;
  key?: string;
  name: string;
  subButtons?: WechatMenuButton[];
  type?: string;
  url?: string;
}

export function getMenuList(params?: { accountId: number }) {
  return requestClient.get('/wechat-oa/menu/list', { params });
}

export function saveMenu(data: WechatMenu & { accountId: number }) {
  return requestClient.post('/wechat-oa/menu/save', data);
}

export function publishMenu(params: { accountId: number }) {
  return requestClient.post('/wechat-oa/menu/publish', params);
}
