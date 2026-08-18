<template>
  <div class="goods-card smooth-hover">
    <h3>{{ title }}</h3>
    <p>价格：{{ price }}</p>
  </div>
</template>

<script setup>
defineProps({
  title: { type: String, default: '示例商品' },
  price: { type: String, default: '0.00' }
});
</script>

<style scoped>
.goods-card {
  padding: 16px;
  border-radius: 8px;
  background: #fff;
}
</style>
