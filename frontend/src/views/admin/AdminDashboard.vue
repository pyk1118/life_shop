<template>
  <div class="admin-layout">
    <div class="admin-sidebar">
      <div class="sidebar-header">
        <div class="avatar-wrapper">
          <div class="avatar">{{ userStore.userInfo.account?.charAt(0)?.toUpperCase() || 'A' }}</div>
        </div>
        <div class="user-name">{{ userStore.userInfo.account }}</div>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        @select="handleMenuSelect"
        background-color="transparent"
        text-color="#a0a3bd"
        active-text-color="#ffffff"
      >
        <el-menu-item index="/admin/overview">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据概览</span>
        </el-menu-item>
        <el-menu-item index="/admin/user">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-sub-menu index="merchant-group">
          <template #title>
            <el-icon><Shop /></el-icon>
            <span>商家管理</span>
          </template>
          <el-menu-item index="/admin/merchant-audit">入驻审核</el-menu-item>
          <el-menu-item index="/admin/merchant-list">商家列表</el-menu-item>
          <el-menu-item index="/admin/shop-list">店铺列表</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/admin/category">
          <el-icon><Menu /></el-icon>
          <span>分类管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/goods">
          <el-icon><Goods /></el-icon>
          <span>商品管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/order">
          <el-icon><List /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/after-sale">
          <el-icon><Service /></el-icon>
          <span>售后管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/message">
          <el-icon><ChatDotRound /></el-icon>
          <span>留言管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/settings">
          <el-icon><Setting /></el-icon>
          <span>开发账号</span>
        </el-menu-item>
        <el-menu-item index="logout">
          <el-icon><SwitchButton /></el-icon>
          <span>退出登录</span>
        </el-menu-item>
      </el-menu>
    </div>
    <div class="admin-main">
      <div class="admin-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useUserStore } from '../../store/user';
import {
  DataAnalysis, User, Shop, Menu, Goods, List, ChatDotRound, Setting, SwitchButton, Service
} from '@element-plus/icons-vue';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
userStore.syncFromStorage();

const activeMenu = computed(() => route.path);

function handleMenuSelect(index) {
  if (index === 'logout') {
    userStore.clearAuth();
    router.push('/login');
  } else {
    router.push(index);
  }
}
</script>

<style scoped lang="less">
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: #ffffff;
}

.admin-sidebar {
  width: 220px;
  background: linear-gradient(180deg, #1d1e3a 0%, #2d2e5e 100%);
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  position: fixed;
  left: 0;
  top: 0;
  height: 100vh;
  overflow-y: auto;
}

.sidebar-header {
  padding: 24px 16px;
  text-align: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);

  .avatar-wrapper {
    margin: 0 auto 10px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .avatar {
    width: 56px;
    height: 56px;
    border-radius: 50%;
    background: linear-gradient(135deg, #165dff 0%, #722ed1 100%);
    color: #fff;
    font-size: 24px;
    font-weight: 700;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .user-name {
    color: #fff;
    font-size: 14px;
  }
}

.sidebar-menu {
  border-right: none;
  background: transparent;
  flex: 1;

  :deep(.el-menu-item) {
    color: #a0a3bd;
    height: 50px;
    line-height: 50px;
  }

  :deep(.el-menu-item.is-active) {
    background: linear-gradient(90deg, #165dff 0%, #722ed1 100%) !important;
    color: #fff !important;
  }

  :deep(.el-menu-item:hover) {
    background: rgba(22, 93, 255, 0.15) !important;
    color: #fff !important;
  }

  :deep(.el-sub-menu__title) {
    color: #a0a3bd;
    height: 50px;
    line-height: 50px;
  }

  :deep(.el-sub-menu__title:hover) {
    background: rgba(22, 93, 255, 0.15) !important;
    color: #fff !important;
  }

  :deep(.el-sub-menu .el-menu-item) {
    color: #a0a3bd;
    padding-left: 52px !important;
    min-width: auto;
  }

  :deep(.el-sub-menu .el-menu-item.is-active) {
    background: linear-gradient(90deg, #165dff 0%, #722ed1 100%) !important;
    color: #fff !important;
  }

  :deep(.el-sub-menu .el-menu-item:hover) {
    background: rgba(22, 93, 255, 0.15) !important;
    color: #fff !important;
  }

  :deep(.el-sub-menu .el-menu) {
    background: transparent;
  }
}

.admin-main {
  flex: 1;
  margin-left: 220px;
  overflow-y: auto;
}

.admin-content {
  padding: 24px;
  min-height: 100vh;
}
</style>