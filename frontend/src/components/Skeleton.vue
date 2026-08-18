<template>
  <div class="skeleton-wrapper">
    <!-- Card Skeleton -->
    <div v-if="type === 'card'" class="skeleton-card">
      <div class="skeleton-image"></div>
      <div class="skeleton-content">
        <div class="skeleton-title"></div>
        <div class="skeleton-text"></div>
        <div class="skeleton-text short"></div>
      </div>
    </div>

    <!-- Table Row Skeleton -->
    <div v-if="type === 'table-row'" class="skeleton-table-row">
      <div class="skeleton-cell"></div>
      <div class="skeleton-cell"></div>
      <div class="skeleton-cell"></div>
      <div class="skeleton-cell short"></div>
    </div>

    <!-- Text Skeleton -->
    <div v-if="type === 'text'" class="skeleton-text-line"></div>

    <!-- Avatar Skeleton -->
    <div v-if="type === 'avatar'" class="skeleton-avatar"></div>
  </div>
</template>

<script setup>
/**
 * 骨架屏加载组件
 * 用于异步数据加载时的占位显示，防止布局塌陷
 */
defineProps({
  type: {
    type: String,
    default: 'card',
    validator: (value) => ['card', 'table-row', 'text', 'avatar'].includes(value)
  }
});
</script>

<style scoped lang="less">
.skeleton-wrapper {
  width: 100%;
}

/* Card Skeleton */
.skeleton-card {
  background: #ffffff;
  border-radius: var(--theme-radius);
  overflow: hidden;
  box-shadow: var(--theme-shadow);

  .skeleton-image {
    height: 180px;
    background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
    background-size: 200% 100%;
    animation: skeleton-loading 1.5s ease-in-out infinite;
  }

  .skeleton-content {
    padding: 12px;

    .skeleton-title {
      height: 20px;
      background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
      background-size: 200% 100%;
      animation: skeleton-loading 1.5s ease-in-out infinite;
      margin-bottom: 8px;
      border-radius: 4px;
    }

    .skeleton-text {
      height: 16px;
      background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
      background-size: 200% 100%;
      animation: skeleton-loading 1.5s ease-in-out infinite;
      margin-bottom: 6px;
      border-radius: 4px;

      &.short {
        width: 60%;
      }
    }
  }
}

/* Table Row Skeleton */
.skeleton-table-row {
  display: flex;
  gap: 16px;
  padding: 12px;
  background: #ffffff;
  border-bottom: 1px solid var(--theme-border);

  .skeleton-cell {
    flex: 1;
    height: 16px;
    background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
    background-size: 200% 100%;
    animation: skeleton-loading 1.5s ease-in-out infinite;
    border-radius: 4px;

    &.short {
      flex: 0 0 80px;
    }
  }
}

/* Text Skeleton */
.skeleton-text-line {
  height: 16px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s ease-in-out infinite;
  border-radius: 4px;
  margin-bottom: 8px;
}

/* Avatar Skeleton */
.skeleton-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s ease-in-out infinite;
}

@keyframes skeleton-loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}
</style>
