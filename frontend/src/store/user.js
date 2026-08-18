import { defineStore } from 'pinia';
import { clearLoginInfo, getLoginInfo, setLoginInfo } from '../utils/auth';

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: {
      id: 0,
      account: '',
      role: '',
      avatar: ''
    },
    token: ''
  }),
  actions: {
    syncFromStorage() {
      const info = getLoginInfo();
      this.token = info.token;
      this.userInfo = { id: info.id, account: info.account, role: info.role, avatar: info.avatar || '' };
    },
    setAuth(payload) {
      setLoginInfo(payload);
      this.syncFromStorage();
    },
    clearAuth() {
      clearLoginInfo();
      this.syncFromStorage();
    },
    updateAvatar(avatarUrl) {
      this.userInfo.avatar = avatarUrl;
      const info = getLoginInfo();
      setLoginInfo({ ...info, avatar: avatarUrl });
    }
  }
});
