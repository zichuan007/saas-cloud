import {
  defineOverridesPreferences,
  definePreferencesExtension,
} from '@vben/preferences';

/**
 * AI Cloud Plus 项目配置
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
  tabLabel: 'AI Cloud Plus',
  title: 'AI Cloud Plus',
});
