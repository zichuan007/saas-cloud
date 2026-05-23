import type {Recordable, UserInfo} from '@vben/types';

import {ref} from 'vue';
import {useRouter} from 'vue-router';

import {LOGIN_PATH} from '@vben/constants';
import {preferences} from '@vben/preferences';
import {resetAllStores, useAccessStore, useUserStore} from '@vben/stores';

import {notification} from 'ant-design-vue';
import {defineStore} from 'pinia';

import {type AuthApi, getUserInfoApi, loginApi, logoutApi} from '#/api';
import {$t} from '#/locales';

export const useAuthStore = defineStore('auth', () => {
  const accessStore = useAccessStore();
  const userStore = useUserStore();
  const router = useRouter();

  const loginLoading = ref(false);

  async function authLogin(
    params: Recordable<any>,
    onSuccess?: () => Promise<void> | void,
  ) {
    let userInfo: null | UserInfo = null;
    try {
      loginLoading.value = true;
      const { accessToken, refreshToken } = await loginApi(
        params as AuthApi.LoginParams,
      );

      if (accessToken) {
        accessStore.setAccessToken(accessToken);
        localStorage.setItem('refreshToken', refreshToken);

        const fetchedUserInfo = await fetchUserInfo();
        userInfo = fetchedUserInfo;

        userStore.setUserInfo(userInfo);

        const permissions = (userInfo as any).permissions ?? [];
        accessStore.setAccessCodes(permissions);

        if (accessStore.loginExpired) {
          accessStore.setLoginExpired(false);
        } else {
          onSuccess
            ? await onSuccess?.()
            : await router.push(
                userInfo.homePath || preferences.app.defaultHomePath,
              );
        }

        if (userInfo?.realName) {
          notification.success({
            description: `${$t('authentication.loginSuccessDesc')}:${userInfo?.realName}`,
            duration: 3,
            message: $t('authentication.loginSuccess'),
          });
        }
      }
    } finally {
      loginLoading.value = false;
    }

    return {
      userInfo,
    };
  }

  async function logout(redirect: boolean = true) {
    try {
      await logoutApi();
    } catch {
      // 不做任何处理
    }
    localStorage.removeItem('refreshToken');
    resetAllStores();
    accessStore.setLoginExpired(false);

    await router.replace({
      path: LOGIN_PATH,
      query: redirect
        ? {
            redirect: encodeURIComponent(router.currentRoute.value.fullPath),
          }
        : {},
    });
  }

  async function fetchUserInfo() {
    const userInfo = await getUserInfoApi();
    userStore.setUserInfo(userInfo);
    return userInfo;
  }

  function $reset() {
    loginLoading.value = false;
  }

  return {
    $reset,
    authLogin,
    fetchUserInfo,
    loginLoading,
    logout,
  };
});
