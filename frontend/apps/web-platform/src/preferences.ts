import {defineOverridesPreferences, definePreferencesExtension,} from '@vben/preferences';

/**
 * AI Cloud Plus 项目配置
 */
export const overridesPreferences = defineOverridesPreferences({
  app: {
    accessMode: 'backend',
    defaultHomePath: '/dashboard',
    enableRefreshToken: true,
    name: import.meta.env.VITE_APP_TITLE,
  },
});

export const preferencesExtension = definePreferencesExtension({
  fields: [],
  tabLabel: 'AI Cloud Plus 运营平台',
  title: 'AI Cloud Plus 运营平台',
});
