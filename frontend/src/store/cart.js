import { defineStore } from 'pinia';

export const useCartStore = defineStore('cart', {
  state: () => ({
    cartCount: 0
  }),
  actions: {
    setCartCount(count) {
      this.cartCount = count;
    }
  }
});
