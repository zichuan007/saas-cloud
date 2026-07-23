import {defineOverridesPreferences, definePreferencesExtension,} from '@vben/preferences';

/**
 * SaaS Cloud 项目配置
 */
export const overridesPreferences = defineOverridesPreferences({
  app: {
    accessMode: 'backend',
    defaultHomePath: '/system/user',
    enableRefreshToken: true,
    name: import.meta.env.VITE_APP_TITLE,
  },
});

export const preferencesExtension = definePreferencesExtension({
  fields: [],
  tabLabel: 'SaaS Cloud',
  title: 'SaaS Cloud',
});
