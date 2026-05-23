import {requestClient} from '#/api/request';

export function getSocialAuthUrl(type: string) {
  return requestClient.get<string>(`/rbac/auth/social/${type}`);
}

export function socialCallback(type: string, params: Record<string, any>) {
  return requestClient.get(`/rbac/auth/social/${type}/callback`, { params });
}

export function bindSocial(data: Record<string, any>) {
  return requestClient.post('/rbac/auth/social/bind', data);
}

export function unbindSocial(type: string) {
  return requestClient.delete(`/rbac/auth/social/unbind/${type}`);
}

export function listSocialBindings() {
  return requestClient.get('/rbac/profile/social');
}
