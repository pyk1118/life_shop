<template>
  <div class="home-page">
    <header class="home-header">
      <div class="header-inner">
        <div class="logo gradient-text" @click="$router.push('/home')">
          <img :src="logoSrc" alt="Logo" class="logo-img" @error="(e) => { e.target.style.display = 'none'; e.target.nextElementSibling.style.display = 'inline'; }" />
          <span class="logo-fallback" style="display: none;">🛒</span>
          优品汇
        </div>
        <div class="search-bar">
          <el-input 
            v-model="query.keyword" 
            placeholder="搜索商品..." 
            clearable 
            @keyup.enter="loadGoods"
            class="search-input"
          >
            <template #append>
              <el-button @click="loadGoods">搜索</el-button>
            </template>
          </el-input>
        </div>
        <div class="category-bar">
          <el-select 
            v-model="query.categoryId" 
            placeholder="全部分类" 
            clearable 
            style="width: 140px" 
            @change="loadGoods"
            class="category-select"
          >
            <el-option v-for="c in categories" :key="c.categoryId" :label="c.categoryName" :value="c.categoryId" />
          </el-select>
        </div>
        <div class="user-center">
          <div v-if="!userStore.token" class="nav-buttons">
            <el-button class="gradient-btn" @click="$router.push('/login')">登录 / 注册</el-button>
          </div>
          <div v-else-if="isUser" class="nav-buttons">
            <a class="nav-link" @click="$router.push('/user')">个人中心</a>
            <div class="user-avatar" @click="$router.push('/user')">
              <img v-if="userStore.userInfo.avatar" :src="userStore.userInfo.avatar" alt="用户头像" />
              <span v-else>{{ userStore.userInfo.account?.charAt(0)?.toUpperCase() || 'U' }}</span>
            </div>
          </div>
          <div v-else-if="isMerchant" class="nav-buttons">
            <a class="nav-link" @click="$router.push('/merchant')">商家后台</a>
            <div class="user-avatar" @click="$router.push('/merchant')">
              <span>{{ userStore.userInfo.account?.charAt(0)?.toUpperCase() || 'M' }}</span>
            </div>
          </div>
          <div v-else-if="isAdmin" class="nav-buttons">
            <a class="nav-link" @click="$router.push('/admin')">开发者后台</a>
            <div class="user-avatar" @click="$router.push('/admin')">
              <span>{{ userStore.userInfo.account?.charAt(0)?.toUpperCase() || 'A' }}</span>
            </div>
          </div>
        </div>
      </div>
    </header>

    <section class="banner animate-fade-in-up">
      <div class="banner-inner">
        <h1>生活好物&emsp;一键到家</h1>
        <p>生活用品&emsp;品质保障&emsp;极速配送</p>
      </div>
    </section>

    <main class="home-main page-container">
      <div class="filter-bar">
        <div class="category-tags">
          <span
            v-for="c in categories" :key="c.categoryId"
            class="cat-tag smooth-hover"
            :class="{ active: query.categoryId === c.categoryId }"
            @click="toggleCategory(c.categoryId)"
          >{{ c.categoryName }}</span>
        </div>
        <div class="sort-bar">
          <el-radio-group v-model="query.sort" size="small" @change="loadGoods">
            <el-radio-button value="">默认</el-radio-button>
            <el-radio-button value="salePriceAsc">价格↑</el-radio-button>
            <el-radio-button value="salePriceDesc">价格↓</el-radio-button>
            <el-radio-button value="salesCountAsc">销量↑</el-radio-button>
            <el-radio-button value="salesCountDesc">销量↓</el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <transition name="list-fade">
        <el-row v-if="listVisible" :gutter="16" class="goods-list-wrapper">
          <el-col v-for="item in goods" :key="item.goodsId" :xs="12" :sm="8" :md="6" class="goods-col">
            <div class="goods-card card-hover" @click="$router.push(`/goods/${item.goodsId}`)">
              <div class="goods-img">
                <img v-if="item.goodsImage" :src="item.goodsImage" :alt="item.goodsName" />
                <div v-else class="img-placeholder">📦</div>
              </div>
              <div class="goods-info">
                <h4 class="goods-name">{{ item.goodsName }}</h4>
                <div class="goods-price">
                  <span class="sale-price">¥{{ item.salePrice }}</span>
                  <span class="market-price">¥{{ item.marketPrice }}</span>
                </div>
                <div class="goods-stock">库存：{{ item.stock }} | 销量：{{ item.salesCount ?? 0 }}</div>
              </div>
              <div class="goods-actions" @click.stop>
                <el-button type="primary" size="small" @click.stop="handleAddCart(item.goodsId)">+加入购物车</el-button>
              </div>
            </div>
          </el-col>
        </el-row>
      </transition>
      <el-row v-if="!listVisible" :gutter="16" class="goods-list-wrapper">
        <el-col v-for="i in 8" :key="'skeleton-' + i" :xs="12" :sm="8" :md="6" class="goods-col">
          <Skeleton type="card" />
        </el-col>
      </el-row>

      <el-empty v-if="!loading && !goods.length" description="暂无商品" />
    </main>

    <footer class="home-footer">
      <p>Copyright 2026 LifeShop 生活用品在线购物系统 —— 优品汇 | 《开发测试综合实训》作品</p>  
    </footer>

    <!-- Floating message button -->
    <div class="floating-message-btn" @click="$router.push('/user/message')">
      <el-icon><ChatDotRound /></el-icon>
      <span>留言</span>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import { ChatDotRound } from '@element-plus/icons-vue';
import { getCategoryList, getGoodsPage } from '../../api/home';
import { addCart } from '../../api/user';
import { getRole } from '../../utils/auth';
import { useUserStore } from '../../store/user';
import Skeleton from '../../components/Skeleton.vue';
import logoSrc from '../../assets/LifeShop_logo.png';

const router = useRouter();
const userStore = useUserStore();
userStore.syncFromStorage();
const categories = ref([]);
const goods = ref([]);
const listVisible = ref(true);
const query = reactive({ keyword: '', categoryId: null, sort: '' });
const isUser = computed(() => getRole() === 'USER');
const isMerchant = computed(() => getRole() === 'MERCHANT');
const isAdmin = computed(() => getRole() === 'ADMIN');

async function loadGoods() {
  listVisible.value = false;
  await new Promise(r => setTimeout(r, 150));
  const res = await getGoodsPage({ current: 1, size: 200, ...query });
  goods.value = res.data.records || [];
  listVisible.value = true;
}

async function loadCategories() {
  const res = await getCategoryList();
  categories.value = res.data || [];
}

function toggleCategory(id) {
  query.categoryId = query.categoryId === id ? null : id;
  loadGoods();
}

async function handleAddCart(goodsId) {
  if (!isUser.value) {
    ElMessage.warning('请先登录用户账号后操作');
    router.push('/login');
    return;
  }
  try {
    await addCart({ goodsId, goodsNum: 1 });
    ElMessage.success('加入购物车成功');
  } catch (e) {
    ElMessage.error('加入购物车失败');
  }
}

onMounted(async () => {
  userStore.syncFromStorage();
  await Promise.all([loadCategories(), loadGoods()]);
});
</script>

<style scoped lang="less">
.home-page {
  min-height: 100vh;
  background: #ffffff;
  position: relative;
}

.home-header {
  background: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 12px 20px;
  display: flex;
  align-items: center;
  justify-content: space-around;
  gap: 20px;
}

.logo {
  font-size: 30px;
  font-weight: 700;
  cursor: pointer;
  white-space: nowrap;
  background: linear-gradient(135deg, #165dff 0%, #722ed1 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 6px;
  font-family: "Segoe UI", "Microsoft YaHei", "PingFang SC", sans-serif;

  .logo-img {
    height: 38px;
    vertical-align: middle;
  }

  .logo-fallback {
    font-size: 30px;
  }
}

.search-bar {
  flex: 1;
  max-width: 500px;
}

.search-input {
  width: 100%;
}

.category-bar {
  flex-shrink: 0;
}

.user-center {
  flex-shrink: 0;
}

.category-select {
  :deep(.el-input__wrapper) {
    border-radius: 20px;
    box-shadow: 0 0 0 1px #dcdfe6 inset;
    transition: all 0.3s ease;
    &:hover {
      box-shadow: 0 0 0 1px #165dff inset;
    }
    &.is-focus {
      box-shadow: 0 0 0 1px #165dff inset;
    }
  }
}

.user-center {
  display: flex;
  align-items: center;
  gap: 12px;
}

.nav-buttons {
  display: flex;
  align-items: center;
  gap: 12px;
}

.nav-link {
  color: #333;
  cursor: pointer;
  transition: color 0.2s;
  text-decoration: none;
  font-size: 14px;
  &:hover {
    color: #165dff;
  }
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #165dff 0%, #722ed1 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  overflow: hidden;
  margin-left: 12px;
  border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s ease;

  &:hover {
    transform: scale(1.1);
  }

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.banner {
  background: linear-gradient(135deg, #165dff 0%, #722ed1 50%, #5b2c9e 100%);
  color: #fff;
  padding: 60px 20px;
  text-align: center;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    width: 600px;
    height: 600px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.08);
    top: -200px;
    right: -100px;
  }

  &::after {
    content: '';
    position: absolute;
    width: 400px;
    height: 400px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.05);
    bottom: -150px;
    left: -50px;
  }
}

.banner-inner {
  position: relative;
  z-index: 1;
}

.banner-inner h1 {
  font-size: 36px;
  margin-bottom: 12px;
  font-weight: 700;
}

.banner-inner p {
  font-size: 16px;
  opacity: 0.9;
}

.home-main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 20px;
  position: relative;
  z-index: 1;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 24px;
  margin-bottom: 20px;
  padding: 12px 16px;
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  flex-wrap: wrap;
  gap: 12px;
}

.category-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.cat-tag {
  padding: 6px 16px;
  background: #f5f7fa;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  color: #666;
  border: 1px solid #e4e7ed;

  &.active {
    background: linear-gradient(135deg, #165dff 0%, #722ed1 100%);
    color: #fff;
    border-color: #165dff;
  }

  &:hover:not(.active) {
    background: #e4e7ed;
  }
}

.sort-bar {
  flex-shrink: 0;

  :deep(.el-radio-group) {
    display: flex;
    gap: 8px;
  }

  :deep(.el-radio-button) {
    padding: 6px 16px;
    background: #f5f7fa;
    border-radius: 20px;
    font-size: 14px;
    border: 1px solid #e4e7ed;
    color: #666;
    transition: all 0.2s;
    box-shadow: none;
    cursor: pointer;
    outline: none;
    -webkit-text-stroke: none;
    display: flex;
    align-items: center;
    justify-content: center;
    height: 32px;

    &.is-active {
      background: linear-gradient(135deg, #165dff 0%, #722ed1 100%);
      color: #fff;
      border-color: #165dff;
      text-shadow: none !important;
      -webkit-text-stroke: 0 !important;
      outline: none !important;

      .el-radio-button__inner {
        background: transparent !important;
        color: #fff !important;
        border: none !important;
        text-shadow: none !important;
        box-shadow: none !important;
        outline: none !important;
        -webkit-text-stroke: 0 !important;
        -webkit-text-fill-color: #fff !important;
        display: flex;
        align-items: center;
        justify-content: center;
        height: 100%;
        line-height: 1;
      }
    }

    &:hover:not(.is-active) {
      background: #e4e7ed;
    }

    &:active:not(.is-active) {
      background: #dcdfe6;
    }
  }

  :deep(.el-radio-button__inner) {
    border: none;
    background: transparent !important;
    padding: 0;
    font-size: 14px;
    box-shadow: none;
    color: inherit;
    outline: none;
    -webkit-text-stroke: none;
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
    line-height: 1;
  }

  :deep(.el-radio-button__orig-radio:checked+.el-radio-button__inner) {
    background: transparent !important;
    box-shadow: none;
    outline: none;
    -webkit-text-stroke: none;
  }

  :deep(.el-radio-button__inner:hover) {
    background: transparent !important;
  }

  :deep(.el-radio-button__inner:active) {
    background: transparent !important;
  }

  :deep(.el-radio-button__text) {
    text-shadow: none !important;
    -webkit-text-stroke: 0 !important;
    -webkit-text-fill-color: inherit !important;
    text-align: center !important;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.goods-list-wrapper {
  margin-bottom: 24px;
}

.goods-col {
  margin-bottom: 16px;
}

.goods-card {
  background: #ffffff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  cursor: pointer;
  height: 100%;
  display: flex;
  flex-direction: column;
  border: 1px solid #e4e7ed;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
  }
}

.goods-img {
  width: 100%;
  height: 180px;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.img-placeholder {
  font-size: 48px;
  color: #dcdfe6;
}

.goods-info {
  padding: 12px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.goods-name {
  margin: 0 0 8px;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.goods-price {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 4px;
}

.sale-price {
  font-size: 18px;
  font-weight: 600;
  color: #f56c6c;
}

.market-price {
  font-size: 12px;
  color: #909399;
  text-decoration: line-through;
}

.goods-stock {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.goods-actions {
  padding: 0 12px 12px;
}

.gradient-btn {
  background: linear-gradient(135deg, #165dff 0%, #722ed1 100%);
  color: white;
  border: none;
  padding: 8px 20px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    opacity: 0.9;
  }
}

.home-footer {
  background: #ffffff;
  text-align: center;
  padding: 20px;
  color: #909399;
  font-size: 13px;
  border-top: 1px solid #e4e7ed;
  margin-top: 40px;
}

.floating-message-btn {
  position: fixed;
  bottom: 80px;
  right: 30px;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #165dff 0%, #722ed1 100%);
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(22, 93, 255, 0.4);
  transition: all 0.3s ease;
  z-index: 1000;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 6px 16px rgba(22, 93, 255, 0.5);
  }

  .el-icon {
    font-size: 20px;
    margin-bottom: 2px;
  }

  span {
    font-size: 10px;
    font-weight: 600;
  }
}

@keyframes fade-in-up {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-fade-in-up {
  animation: fade-in-up 0.6s ease-out;
}

.smooth-hover {
  transition: all 0.2s ease;
}

.card-hover {
  transition: all 0.3s ease;
}

.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.list-fade-enter-active,
.list-fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}
.list-fade-enter-from,
.list-fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>