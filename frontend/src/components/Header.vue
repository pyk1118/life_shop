<template>
  <header class="app-header">
    <img :src="logoSrc" alt="Logo" class="header-logo" @error="(e) => { e.target.style.display = 'none'; e.target.nextElementSibling.style.display = 'inline-flex'; }" />
    <span class="header-logo-fallback" style="display: none;">🛒</span>
    优品汇
  </header>
</template>

<script setup>
import logoSrc from '../assets/LifeShop_logo.png';
</script>

<style scoped>
.app-header {
  padding: 16px 24px;
  color: #fff;
  background: linear-gradient(90deg, #165dff 0%, #722ed1 100%);
  display: flex;
  align-items: center;
  gap: 12px;
  font-family: "Segoe UI", "Microsoft YaHei", "PingFang SC", sans-serif;
  font-size: 22px;
}

.header-logo {
  height: 36px;
}

.header-logo-fallback {
  font-size: 36px;
}
</style>
