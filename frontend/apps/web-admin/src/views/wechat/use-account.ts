import { ref } from 'vue';

import { getAccountList, type WechatAccount } from '#/api/wechat/account';

const accountList = ref<WechatAccount[]>([]);
const currentAccountId = ref<number>(0);
const loaded = ref(false);

export function useWechatAccount() {
  async function loadAccounts() {
    if (loaded.value) return;
    accountList.value = (await getAccountList()) as WechatAccount[];
    if (accountList.value.length > 0 && !currentAccountId.value) {
      currentAccountId.value = accountList.value[0]!.id;
    }
    loaded.value = true;
  }

  return {
    accountList,
    currentAccountId,
    loadAccounts,
  };
}
