import { defineStore } from 'pinia';

export const useAppStore = defineStore('app', {
  state: () => ({
    loading: false,
    theme: 'default'
  }),
  actions: {
    setLoading(loading) {
      this.loading = loading;
    },
    setTheme(theme) {
      this.theme = theme;
    }
  }
});
