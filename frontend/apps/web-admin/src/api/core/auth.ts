import { baseRequestClient, requestClient } from '#/api/request';

export namespace AuthApi {
  export interface LoginParams {
    password: string;
    tenantCode: string;
    username: string;
  }

  export interface LoginResult {
    accessToken: string;
    expiresIn: number;
    refreshToken: string;
  }

  export interface RegisterParams {
    contactPerson: string;
    password: string;
    phone: string;
    tenantName: string;
    verifyCode: string;
  }

  export interface RegisterResult {
    accessToken: string;
    expiresIn: number;
    refreshToken: string;
    tenantCode: string;
    tenantId: number;
    userId: number;
  }
}

/**
 * 租户登录
 */
export async function loginApi(data: AuthApi.LoginParams) {
  return requestClient.post<AuthApi.LoginResult>('/rbac/auth/login', data);
}

/**
 * 刷新 Token
 */
export async function refreshTokenApi(refreshToken: string) {
  return baseRequestClient.post<AuthApi.LoginResult>(
    '/rbac/auth/refresh',
    { refreshToken },
  );
}

/**
 * 登出
 */
export async function logoutApi() {
  return requestClient.post('/rbac/auth/logout');
}

/**
 * 租户注册
 */
export async function registerApi(data: AuthApi.RegisterParams) {
  return requestClient.post<AuthApi.RegisterResult>(
    '/rbac/auth/register',
    data,
  );
}

/**
 * 获取权限码
 */
export async function getAccessCodesApi() {
  return requestClient.get<string[]>('/rbac/auth/codes');
}

/**
 * 发送验证码
 */
export async function sendCaptchaApi(phone: string, type: string) {
  return requestClient.post('/rbac/auth/captcha', { phone, type });
}
