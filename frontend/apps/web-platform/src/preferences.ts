import {defineOverridesPreferences, definePreferencesExtension,} from '@vben/preferences';

/**
 * SaaS Cloud 运营平台项目配置
 */
export const overridesPreferences = defineOverridesPreferences({
  app: {
    accessMode: 'backend',
    defaultHomePath: '/tenant',
    enableRefreshToken: true,
    name: import.meta.env.VITE_APP_TITLE,
  },
});

export const preferencesExtension = definePreferencesExtension({
  fields: [],
  tabLabel: 'SaaS Cloud 运营平台',
  title: 'SaaS Cloud 运营平台',
});
