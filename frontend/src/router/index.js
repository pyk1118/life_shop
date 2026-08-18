import { createRouter, createWebHistory } from 'vue-router';
import { getRole, getToken } from '../utils/auth';

const APP_TITLE = '优品汇';

const routes = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/home',
    name: 'home',
    component: () => import('../views/home/HomeIndex.vue'),
    meta: { public: true, title: '首页' }
  },
  {
    path: '/goods/:goodsId',
    name: 'goodsDetail',
    component: () => import('../views/home/GoodsDetail.vue'),
    meta: { public: true, title: '商品详情' }
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/login/LoginIndex.vue'),
    meta: { public: true, title: '登录' }
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('../views/login/RegisterIndex.vue'),
    meta: { public: true, title: '注册' }
  },
  {
    path: '/user',
    name: 'user',
    component: () => import('../views/user/UserCenter.vue'),
    meta: { role: 'USER', title: '个人中心' }
  },
  {
    path: '/user/message',
    name: 'userMessage',
    component: () => import('../views/user/MessagePage.vue'),
    meta: { public: true, title: '留言反馈' }
  },
  {
    path: '/merchant',
    name: 'merchant',
    component: () => import('../views/merchant/MerchantDashboard.vue'),
    meta: { role: 'MERCHANT', title: '商家后台' }
  },
  {
    path: '/admin',
    name: 'admin',
    component: () => import('../views/admin/AdminDashboard.vue'),
    meta: { role: 'ADMIN', title: '开发者后台' },
    redirect: '/admin/overview',
    children: [
      {
        path: 'overview',
        name: 'adminOverview',
        component: () => import('../views/admin/AdminOverview.vue'),
        meta: { title: '开发者后台' }
      },
      {
        path: 'user',
        name: 'adminUser',
        component: () => import('../views/admin/AdminUser.vue'),
        meta: { title: '开发者后台' }
      },
      {
        path: 'merchant-audit',
        name: 'adminMerchantAudit',
        component: () => import('../views/admin/AdminMerchantAudit.vue'),
        meta: { title: '开发者后台' }
      },
      {
        path: 'merchant-list',
        name: 'adminMerchantList',
        component: () => import('../views/admin/AdminMerchantList.vue'),
        meta: { title: '开发者后台' }
      },
      {
        path: 'shop-list',
        name: 'adminShopList',
        component: () => import('../views/admin/AdminShopList.vue'),
        meta: { title: '开发者后台' }
      },
      {
        path: 'category',
        name: 'adminCategory',
        component: () => import('../views/admin/AdminCategory.vue'),
        meta: { title: '开发者后台' }
      },
      {
        path: 'goods',
        name: 'adminGoods',
        component: () => import('../views/admin/AdminGoods.vue'),
        meta: { title: '开发者后台' }
      },
      {
        path: 'order',
        name: 'adminOrder',
        component: () => import('../views/admin/AdminOrder.vue'),
        meta: { title: '开发者后台' }
      },
      {
        path: 'after-sale',
        name: 'adminAfterSale',
        component: () => import('../views/admin/AdminAfterSale.vue'),
        meta: { title: '开发者后台' }
      },
      {
        path: 'message',
        name: 'adminMessage',
        component: () => import('../views/admin/AdminMessage.vue'),
        meta: { title: '开发者后台' }
      },
      {
        path: 'settings',
        name: 'adminSettings',
        component: () => import('../views/admin/AdminSettings.vue'),
        meta: { title: '开发者后台' }
      }
    ]
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${APP_TITLE}-${to.meta.title}` : APP_TITLE;

  if (to.meta.public) {
    next();
    return;
  }
  const token = getToken();
  if (!token) {
    next('/login');
    return;
  }
  if (to.meta.role) {
    const role = getRole();
    if (role !== to.meta.role) {
      next('/home');
      return;
    }
  }
  next();
});

export default router;
