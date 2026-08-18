<template>
  <div class="detail-page page-container animate-fade-in-up" v-if="detail.goodsId">
    <div class="detail-layout">
      <div class="detail-left">
        <div class="img-box">
          <img v-if="detail.goodsImage" :src="detail.goodsImage" :alt="detail.goodsName" />
          <div v-else class="img-placeholder">📦</div>
        </div>
      </div>
      <div class="detail-right">
        <h1 class="goods-title">{{ detail.goodsName }}</h1>
        <div class="price-box">
          <span class="sale-price">¥{{ detail.salePrice }}</span>
          <span class="market-price">市场价 ¥{{ detail.marketPrice }}</span>
        </div>
        <div class="info-row"><span class="label">库存</span><span>{{ detail.stock }} 件</span></div>
        <div class="info-row"><span class="label">销量</span><span>{{ detail.salesCount ?? 0 }} 件</span></div>
        <div class="info-row"><span class="label">分类</span><span>{{ detail.categoryName || detail.categoryId }}</span></div>
        <div class="buy-area">
          <el-input-number v-model="buyNum" :min="1" :max="Math.max(detail.stock || 1, 1)" size="large" />
          <button class="gradient-btn buy-btn" @click="addToCart">加入购物车</button>
        </div>
      </div>
    </div>
    <div class="detail-desc" v-if="detail.goodsDesc">
      <h3>商品详情</h3>
      <div class="goods-details-card">
        <p>{{ detail.goodsDesc }}</p>
      </div>
    </div>
    <div class="shop-desc" v-if="shop">
      <h3>店铺详情</h3>
      <div class="shop-info-card">
        <div class="shop-header-row">
          <div class="shop-logo-box" v-if="shop.shopLogo">
            <img :src="shop.shopLogo" :alt="shop.shopName" />
          </div>
          <div class="shop-logo-box placeholder" v-else>🏪</div>
          <div class="shop-basic">
            <span class="shop-name">{{ shop.shopName }}</span>
            <el-tag :type="shop.shopStatus === 1 ? 'success' : 'info'" size="small">{{ shop.shopStatus === 1 ? '营业中' : '休息中' }}</el-tag>
          </div>
        </div>
        <div class="shop-detail-rows">
          <div class="shop-detail-row" v-if="shop.businessHours"><span class="label">营业时间</span><span>{{ shop.businessHours }}</span></div>
          <div class="shop-detail-row" v-if="merchantInfo.address"><span class="label">店铺地址</span><span>{{ merchantInfo.address }}</span></div>
          <div class="shop-detail-row" v-if="merchantInfo.phone"><span class="label">商家电话</span><span>{{ merchantInfo.phone }}</span></div>
          <div class="shop-detail-row" v-if="merchantInfo.email"><span class="label">商家邮箱</span><span>{{ merchantInfo.email }}</span></div>
          <div class="shop-detail-row" v-if="shop.shopDesc"><span class="label">店铺描述</span><span>{{ shop.shopDesc }}</span></div>
        </div>
      </div>
    </div>
    <div class="back-row">
      <el-button @click="goBack"><&nbsp;返回</el-button>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useRoute, useRouter } from 'vue-router';
import { getGoodsDetail } from '../../api/home';
import { addCart } from '../../api/user';
import { getRole } from '../../utils/auth';

const route = useRoute();
const router = useRouter();
const detail = ref({});
const shop = ref(null);
const merchantInfo = ref({ phone: '', email: '', address: '' });
const buyNum = ref(1);

function goBack() {
  const from = route.query.from;
  if (from) {
    router.push(from);
  } else {
    router.back();
  }
}

async function loadDetail() {
  const res = await getGoodsDetail(route.params.goodsId);
  const data = res.data || {};
  detail.value = data.goods || {};
  shop.value = data.shop || null;
  merchantInfo.value = {
    phone: data.merchantPhone || '',
    email: data.merchantEmail || '',
    address: data.merchantAddress || ''
  };
}

async function addToCart() {
  if (getRole() !== 'USER') {
    ElMessage.warning('请先登录用户账号后操作');
    router.push('/login');
    return;
  }
  await addCart({ goodsId: detail.value.goodsId, goodsNum: buyNum.value });
  ElMessage.success('加入购物车成功');
}

onMounted(loadDetail);
</script>

<style scoped lang="less">
.detail-page {
  padding-top: 32px;
}

.detail-layout {
  display: flex;
  gap: 32px;
  background: #ffffff;
  border-radius: var(--theme-radius-lg);
  padding: 24px;
  box-shadow: var(--theme-shadow);
}

.detail-left {
  width: 420px;
  flex-shrink: 0;

  .img-box {
    width: 100%;
    height: 380px;
    background: #f5f7fa;
    border-radius: var(--theme-radius);
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;

    img {
      max-width: 100%;
      max-height: 100%;
      object-fit: contain;
    }

    .img-placeholder {
      font-size: 80px;
      color: #c9cdd4;
    }
  }
}

.detail-right {
  flex: 1;

  .goods-title {
    font-size: 22px;
    font-weight: 600;
    margin: 0 0 16px;
    color: var(--theme-text);
  }

  .price-box {
    background: linear-gradient(90deg, rgba(22, 93, 255, 0.06) 0%, rgba(114, 46, 209, 0.06) 100%);
    padding: 16px;
    border-radius: var(--theme-radius);
    margin-bottom: 20px;

    .sale-price {
      font-size: 28px;
      font-weight: 700;
      color: var(--theme-danger);
    }

    .market-price {
      font-size: 14px;
      color: var(--theme-text-muted);
      text-decoration: line-through;
      margin-left: 12px;
    }
  }

  .info-row {
    display: flex;
    gap: 12px;
    margin-bottom: 12px;
    font-size: 14px;
    color: var(--theme-text-secondary);

    .label {
      color: var(--theme-text-muted);
      min-width: 60px;
    }
  }

  .buy-area {
    display: flex;
    gap: 16px;
    margin-top: 24px;
    align-items: center;

    .buy-btn {
      height: 40px;
      padding: 0 32px;
      border-radius: 8px;
      font-size: 15px;
      font-weight: 600;
      cursor: pointer;
    }
  }
}

.detail-desc {
  background: #ffffff;
  border-radius: var(--theme-radius-lg);
  padding: 24px;
  margin-top: 20px;
  box-shadow: var(--theme-shadow);

  .goods-details-card {
  background: linear-gradient(90deg, rgba(22, 93, 255, 0.04) 0%, rgba(114, 46, 209, 0.04) 100%);
  border-radius: 12px;
  padding: 20px;
  }

  h3 {
    font-size: 18px;
    margin: 0 0 12px;
    color: var(--theme-text);
  }

  p {
    color: var(--theme-text-secondary);
    line-height: 1.8;
    font-size: 14px;
  }
}

.shop-desc {
  background: #ffffff;
  border-radius: var(--theme-radius-lg);
  padding: 24px;
  margin-top: 20px;
  box-shadow: var(--theme-shadow);

  h3 {
    font-size: 18px;
    margin: 0 0 16px;
    color: var(--theme-text);
  }
}

.shop-info-card {
  background: linear-gradient(90deg, rgba(22, 93, 255, 0.04) 0%, rgba(114, 46, 209, 0.04) 100%);
  border-radius: 12px;
  padding: 20px;
}

.shop-header-row {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.shop-logo-box {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  overflow: hidden;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  &.placeholder {
    font-size: 32px;
  }
}

.shop-basic {
  display: flex;
  align-items: center;
  gap: 10px;
}

.shop-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--theme-text);
}

.shop-detail-rows {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.shop-detail-row {
  display: flex;
  gap: 12px;
  font-size: 14px;
  color: var(--theme-text-secondary);

  .label {
    color: var(--theme-text-muted);
    min-width: 70px;
    flex-shrink: 0;
  }
}

.back-row {
  margin-top: 20px;
}

@media (max-width: 768px) {
  .detail-layout {
    flex-direction: column;
  }
  .detail-left {
    width: 100%;
  }
}
</style>
