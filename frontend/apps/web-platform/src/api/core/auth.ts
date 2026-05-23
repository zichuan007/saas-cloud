import {baseRequestClient, requestClient} from '#/api/request';

export namespace AuthApi {
  export interface LoginParams {
    password: string;
    username: string;
  }

  export interface LoginResult {
    accessToken: string;
    expiresIn: number;
    refreshToken: string;
  }
}

export async function loginApi(data: AuthApi.LoginParams) {
  return requestClient.post<AuthApi.LoginResult>(
    '/platform/auth/login',
    data,
  );
}

export async function refreshTokenApi(refreshToken: string) {
  return baseRequestClient.post<AuthApi.LoginResult>(
    '/platform/auth/refresh-token',
    { refreshToken },
  );
}

export async function logoutApi() {
  return requestClient.post('/platform/auth/logout');
}

export async function getAccessCodesApi() {
  return requestClient.get<string[]>('/platform/auth/codes');
}
