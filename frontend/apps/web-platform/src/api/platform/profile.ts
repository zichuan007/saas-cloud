import {requestClient} from '#/api/request';

export function updateProfile(data: Record<string, any>) {
  return requestClient.put('/platform/auth/profile', data);
}

export function updatePassword(oldPassword: string, newPassword: string) {
  return requestClient.put('/platform/auth/password', {
    newPassword,
    oldPassword,
  });
}
