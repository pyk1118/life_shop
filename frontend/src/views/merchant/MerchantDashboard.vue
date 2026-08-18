<template>
  <div class="merchant-layout">
    <div class="merchant-sidebar">
      <div class="sidebar-header">
        <div class="avatar-wrapper" @click="showLogoUpload = true">
          <div class="avatar-inner">
            <img v-if="shopForm.shopLogo" :src="shopForm.shopLogo" class="avatar-img" />
            <span v-else>🏪</span>
          </div>
          <div class="avatar-edit-tip">点击更换</div>
        </div>
        <div class="user-name">{{ userStore.userInfo.account }}</div>
      </div>
      <el-menu :default-active="tab" @select="handleMenuSelect" class="sidebar-menu">
        <el-menu-item index="overview"><el-icon><DataAnalysis /></el-icon>数据概览</el-menu-item>
        <el-menu-item index="shop"><el-icon><Shop /></el-icon>店铺信息</el-menu-item>
        <el-menu-item index="goods"><el-icon><Goods /></el-icon>商品管理</el-menu-item>
        <el-menu-item index="order"><el-icon><List /></el-icon>订单管理</el-menu-item>
        <el-menu-item index="afterSale"><el-icon><Service /></el-icon>售后处理</el-menu-item>
        <el-menu-item index="message"><el-icon><ChatDotRound /></el-icon>留言反馈</el-menu-item>
        <el-menu-item index="password"><el-icon><Lock /></el-icon>修改密码</el-menu-item>
        <el-menu-item index="logout"><el-icon><SwitchButton /></el-icon>退出登录</el-menu-item>
      </el-menu>
    </div>
    <div class="merchant-main animate-fade-in-up">

      <div v-if="tab === 'overview'" class="animate-fade-in">
        <h3 class="page-title">数据概览</h3>
        <el-row :gutter="16" class="stat-row">
          <el-col :span="8" v-for="item in overviewCards" :key="item.label">
            <div class="stat-card">
              <div class="stat-value">{{ item.value }}</div>
              <div class="stat-label">{{ item.label }}</div>
            </div>
          </el-col>
        </el-row>
        <h3 class="page-title" style="margin-top:40px">数据图表</h3>
        <el-row :gutter="16">
          <el-col :span="12">
            <div class="chart-card">
              <span class="chart-title">商品分类统计</span>
              <div ref="categoryChartRef" class="chart-container"></div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="chart-card">
              <div class="chart-title-row">
                <span class="chart-title">销售额趋势</span>
                <el-radio-group v-model="salesChartMode" size="small" @change="updateSalesChart">
                  <el-radio-button value="monthly">按月</el-radio-button>
                  <el-radio-button value="daily">按日</el-radio-button>
                </el-radio-group>
              </div>
              <div ref="salesChartRef" class="chart-container"></div>
            </div>
          </el-col>
        </el-row>
        <h3 class="page-title" style="margin-top:40px">待办事项</h3>
        <el-row :gutter="16">
          <el-col :span="8" v-for="item in todoCards" :key="item.label">
            <div class="todo-card" @click="item.action">
              <div class="todo-value">{{ item.value }}</div>
              <div class="todo-label">{{ item.label }}</div>
            </div>
          </el-col>
        </el-row>
      </div>

      <div v-if="tab === 'shop'" class="section-card animate-fade-in">
        <h3>店铺信息</h3>
        <el-form :model="shopForm" label-width="100px" style="max-width:560px;margin:0 auto">
          <el-form-item label="店铺名称" required><el-input v-model="shopForm.shopName" /></el-form-item>
          <el-form-item label="店铺Logo">
            <div class="logo-upload-area" @click="showLogoUpload = true">
              <img v-if="shopForm.shopLogo" :src="shopForm.shopLogo" class="logo-preview" />
              <el-icon v-else class="logo-placeholder"><Plus /></el-icon>
            </div>
          </el-form-item>
          <el-form-item label="店铺描述"><el-input v-model="shopForm.shopDesc" type="textarea" :rows="3" /></el-form-item>
          <el-form-item label="营业时间" required>
            <el-select v-model="shopForm.businessHours" placeholder="选择营业时间" style="width:100%">
              <el-option v-for="h in businessHoursOptions" :key="h" :label="h" :value="h" />
            </el-select>
          </el-form-item>
          <el-form-item label="店铺状态" required>
            <el-switch v-model="shopForm.shopStatus" :active-value="1" :inactive-value="0" active-text="营业中" inactive-text="已关闭" />
          </el-form-item>
          <el-form-item><el-button type="primary" @click="saveShop">保存修改</el-button></el-form-item>
        </el-form>
      </div>

      <div v-if="tab === 'goods'" class="section-card animate-fade-in">
        <div class="card-header-row">
          <h3>商品管理</h3>
        </div>
        <div class="filter-row">
          <el-input v-model="goodsSearch" placeholder="搜索商品名称" clearable style="width:200px" />
          <el-select v-model="goodsCategoryFilter" placeholder="全部分类" clearable style="width:150px">
            <el-option v-for="c in categoryList" :key="c.categoryId" :label="c.categoryName" :value="c.categoryId" />
          </el-select>
          <el-select v-model="goodsSort" placeholder="默认排序" style="width:150px">
            <el-option label="默认排序" value="" />
            <el-option label="价格从低到高" value="priceAsc" />
            <el-option label="价格从高到低" value="priceDesc" />
            <el-option label="销量从低到高" value="salesAsc" />
            <el-option label="销量从高到低" value="salesDesc" />
          </el-select>
          <el-button type="primary" @click="filterGoods">查询</el-button>
          <el-button @click="resetGoodsFilter">重置</el-button>
          <div style="flex: 1"></div>
          <el-button type="primary" size="default" @click="openGoodsDialog(null)">新增商品</el-button>
        </div>
        <el-table :data="filteredGoods" border stripe style="width:100%" class="large-table">
          <el-table-column prop="goodsId" label="ID" width="70" align="center" />
          <el-table-column prop="goodsName" label="名称" min-width="160" align="center" show-overflow-tooltip>
            <template #default="{ row }">
              <router-link class="link-text" :to="{ path: '/goods/' + row.goodsId, query: { from: '/merchant' } }">{{ row.goodsName }}</router-link>
            </template>
          </el-table-column>
          <el-table-column prop="categoryName" label="分类" width="100" align="center" />
          <el-table-column label="售价" width="100" align="center">
            <template #default="{ row }">¥{{ row.salePrice }}</template>
          </el-table-column>
          <el-table-column prop="stock" label="库存" width="80" align="center" />
          <el-table-column prop="salesCount" label="销量" width="80" align="center" />
          <el-table-column label="状态" width="80" align="center">
            <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '上架' : '下架' }}</el-tag></template>
          </el-table-column>
          <el-table-column label="操作" width="200" align="center">
            <template #default="{ row }">
              <el-button size="small" @click="openGoodsDialog(row)">编辑</el-button>
              <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="toggleGoodsStatus(row.goodsId)">{{ row.status === 1 ? '下架' : '上架' }}</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div v-if="tab === 'order'" class="section-card animate-fade-in">
        <h3>订单管理</h3>
        <div class="filter-row">
          <el-input v-model="orderSearchNo" placeholder="订单编号" clearable style="width:200px" />
          <el-input v-model="orderSearchUserId" placeholder="用户ID" clearable style="width:120px" />
          <el-select v-model="orderStatusFilter" placeholder="全部状态" clearable style="width:150px">
            <el-option v-for="(label, key) in orderStatusMap" :key="key" :label="label" :value="Number(key)" />
          </el-select>
          <el-button type="primary" @click="filterOrders">查询</el-button>
          <el-button @click="resetOrderFilter">重置</el-button>
        </div>
        <el-table :data="filteredOrders" border stripe style="width:100%" class="large-table">
          <el-table-column prop="orderId" label="ID" width="100" align="center" />
          <el-table-column prop="orderNo" label="订单编号" min-width="250" align="center">
            <template #default="{ row }">
              <a class="link-text" @click.prevent="viewOrderDetail(row.orderId)">{{ row.orderNo }}</a>
            </template>
          </el-table-column>
          <el-table-column label="金额" width="150" align="center">
            <template #default="{ row }">¥{{ row.totalAmount }}</template>
          </el-table-column>
          <el-table-column label="状态" width="150" align="center">
            <template #default="{ row }"><el-tag :type="orderStatusType(row.orderStatus)" size="small">{{ orderStatusMap[row.orderStatus] }}</el-tag></template>
          </el-table-column>
          <el-table-column label="下单时间" width="200" align="center">
            <template #default="{ row }"><span v-html="formatDateTimeHtml(row.createTime)"></span></template>
          </el-table-column>
          <el-table-column label="操作" min-width="120" align="center">
            <template #default="{ row }">
              <el-button v-if="row.orderStatus === 1" size="small" type="primary" @click="deliverOrder(row.orderId)">发货</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div v-if="tab === 'afterSale'" class="section-card animate-fade-in">
        <h3>售后处理</h3>
        <div class="filter-row">
          <el-input v-model="afterSaleSearchOrderId" placeholder="订单编号" clearable style="width:150px" />
          <el-select v-model="afterSaleStatusFilter" placeholder="全部状态" clearable style="width:150px">
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已驳回" :value="2" />
          </el-select>
          <el-button type="primary" @click="filterAfterSales">查询</el-button>
          <el-button @click="resetAfterSaleFilter">重置</el-button>
        </div>
        <el-table :data="filteredAfterSales" border stripe style="width:100%" class="large-table">
          <el-table-column prop="afterSaleId" label="ID" width="70" align="center" />
          <el-table-column label="订单编号" width="210" align="center">
            <template #default="{ row }">
              <a class="order-link" @click.prevent="viewAfterSaleOrder(row.orderId)">{{ row.orderNo || getAfterSaleOrderNo(row.orderId) }}</a>
            </template>
          </el-table-column>
          <el-table-column label="退款金额" width="110" align="center">
            <template #default="{ row }">¥{{ row.refundAmount }}</template>
          </el-table-column>
          <el-table-column prop="refundReason" label="退款原因" min-width="150" align="center" show-overflow-tooltip />
          <el-table-column label="凭证" width="100" align="center">
            <template #default="{ row }">
              <img v-if="row.refundVoucher" :src="row.refundVoucher" style="width:50px;height:50px;object-fit:cover;cursor:pointer;border-radius:4px" @click="openPreview(row.refundVoucher)" />
              <span v-else style="color:#999">无</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }"><el-tag :type="row.auditStatus === 0 ? 'warning' : row.auditStatus === 1 ? 'success' : 'danger'" size="small">{{ row.auditStatus === 0 ? '待审核' : row.auditStatus === 1 ? '已通过' : '已驳回' }}</el-tag></template>
          </el-table-column>
          <el-table-column label="操作" width="160" align="center">
            <template #default="{ row }">
              <template v-if="row.auditStatus === 0">
                <el-button size="small" type="success" @click="openAfterSaleHandle(row.afterSaleId, 1)">同意</el-button>
                <el-button size="small" type="danger" @click="openAfterSaleHandle(row.afterSaleId, 2)">驳回</el-button>
              </template>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div v-if="tab === 'message'" class="section-card animate-fade-in">
        <h3>留言反馈</h3>
        <div class="filter-row">
          <el-input v-model="msgSearchAccount" placeholder="用户账号" clearable style="width:180px" />
          <el-input v-model="msgSearchTitle" placeholder="留言标题" clearable style="width:200px" />
          <el-button type="primary" @click="filterMessages">查询</el-button>
          <el-button @click="resetMsgFilter">重置</el-button>
        </div>
        <el-table :data="filteredMessages" border stripe style="width:100%" class="large-table">
          <el-table-column prop="messageId" label="ID" width="70" align="center" />
          <el-table-column prop="userAccount" label="用户" width="120" align="center" />
          <el-table-column prop="messageTitle" label="标题" min-width="180" show-overflow-tooltip />
          <el-table-column prop="messageContent" label="内容" min-width="220" show-overflow-tooltip />
          <el-table-column prop="replyContent" label="回复" width="180" show-overflow-tooltip>
            <template #default="{ row }">
              <span v-if="row.replyContent">{{ row.replyContent }}</span>
              <el-tag v-else type="warning" size="small">未回复</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="留言时间" width="180" align="center">
            <template #default="{ row }"><span v-html="formatDateTimeHtml(row.createTime)"></span></template>
          </el-table-column>
          <el-table-column label="操作" fixed="right" width="100" align="center">
            <template #default="{ row }">
              <el-button size="small" type="primary" @click="openMessageReply(row)">回复</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <el-dialog v-model="messageReplyVisible" title="回复留言" width="500px">
        <template v-if="messageReplyingRow">
          <el-descriptions :column="1" border size="small" style="margin-bottom: 16px">
            <el-descriptions-item label="用户">{{ messageReplyingRow.userAccount }}</el-descriptions-item>
            <el-descriptions-item label="标题">{{ messageReplyingRow.messageTitle }}</el-descriptions-item>
            <el-descriptions-item label="内容">{{ messageReplyingRow.messageContent }}</el-descriptions-item>
          </el-descriptions>
        </template>
        <el-input v-model="messageReplyContent" type="textarea" :rows="4" placeholder="请输入回复内容" />
        <template #footer>
          <el-button @click="messageReplyVisible = false">取消</el-button>
          <el-button type="primary" @click="handleMessageReply">提交回复</el-button>
        </template>
      </el-dialog>

      <el-dialog v-model="afterSaleHandleVisible" :title="afterSaleHandleStatus === 1 ? '同意退款' : '驳回退款'" width="400px">
        <el-input v-model="afterSaleHandleRemark" type="textarea" :rows="3" placeholder="请输入备注（可选）" />
        <template #footer>
          <el-button @click="afterSaleHandleVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmAfterSaleHandle">确定</el-button>
        </template>
      </el-dialog>

      <div v-if="tab === 'password'" class="section-card animate-fade-in">
        <h3>修改密码</h3>
        <el-form :model="pwdForm" label-width="100px" style="max-width:460px;margin:0 auto">
          <el-form-item label="原密码" required><el-input v-model="pwdForm.oldPassword" type="password" show-password /></el-form-item>
          <el-form-item label="新密码" required><el-input v-model="pwdForm.newPassword" type="password" show-password /></el-form-item>
          <el-form-item label="确认新密码" required><el-input v-model="pwdForm.confirmPassword" type="password" show-password /></el-form-item>
          <el-form-item><el-button type="primary" @click="changePwd">确认修改</el-button></el-form-item>
        </el-form>
      </div>
    </div>

    <el-dialog v-model="showLogoUpload" title="更换店铺Logo" width="400px" center>
      <div class="avatar-upload-content">
        <ImageUpload v-model="tempLogo" upload-url="/api/upload/logo" tip="支持JPG/PNG/WEBP格式，不超过2MB" @success="handleLogoSuccess" />
      </div>
      <template #footer>
        <div style="display:flex;justify-content:center;gap:12px">
          <el-button @click="showLogoUpload = false">取消</el-button>
          <el-button type="primary" @click="saveLogo">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="orderDetailVisible" title="订单详情" width="650px">
      <template v-if="orderDetailData">
        <el-descriptions :column="2" border size="small" style="margin-bottom: 16px">
          <el-descriptions-item label="订单编号">{{ orderDetailData.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">{{ orderStatusMap[orderDetailData.orderStatus] }}</el-descriptions-item>
          <el-descriptions-item label="总金额">¥{{ orderDetailData.totalAmount }}</el-descriptions-item>
          <el-descriptions-item label="收货人">{{ orderDetailData.receiverName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ orderDetailData.receiverPhone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ orderDetailData.receiverAddress || '-' }}</el-descriptions-item>
          <el-descriptions-item label="物流单号" :span="2">{{ orderDetailData.logisticsNo || '-' }}</el-descriptions-item>
        </el-descriptions>
        <h4 style="margin: 12px 0 8px">订单商品</h4>
        <el-table :data="orderDetailData.items" border size="small">
          <el-table-column prop="goodsName" label="商品名称" min-width="150" />
          <el-table-column label="单价" width="100">
            <template #default="{ row }">¥{{ row.goodsPrice }}</template>
          </el-table-column>
          <el-table-column prop="goodsNum" label="数量" width="80" />
          <el-table-column label="小计" width="100">
            <template #default="{ row }">¥{{ row.totalPrice }}</template>
          </el-table-column>
        </el-table>
      </template>
    </el-dialog>

    <el-dialog v-model="goodsDialogVisible" :title="editingGoods ? '编辑商品' : '新增商品'" width="550px">
      <el-form :model="goodsForm" label-width="90px">
        <el-form-item label="商品名称" required><el-input v-model="goodsForm.goodsName" /></el-form-item>
        <el-form-item label="商品分类" required>
          <el-select v-model="goodsForm.categoryId" placeholder="选择分类" style="width:100%">
            <el-option v-for="c in categoryList" :key="c.categoryId" :label="c.categoryName" :value="c.categoryId" />
          </el-select>
        </el-form-item>
        <el-form-item label="市场价"><el-input-number v-model="goodsForm.marketPrice" :precision="2" :min="0" /></el-form-item>
        <el-form-item label="销售价" required><el-input-number v-model="goodsForm.salePrice" :precision="2" :min="0" /></el-form-item>
        <el-form-item label="库存" required><el-input-number v-model="goodsForm.stock" :min="0" /></el-form-item>
        <el-form-item label="商品图片">
          <ImageUpload v-model="goodsForm.goodsImage" upload-url="/api/upload/goods" tip="支持JPG/PNG/WEBP格式" />
        </el-form-item>
        <el-form-item label="描述"><el-input v-model="goodsForm.goodsDesc" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="goodsDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveGoods">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="previewVisible" width="50%" :show-close="true" @close="previewVisible = false; previewImageUrl = ''" destroy-on-close>
      <div style="display:flex;justify-content:center;align-items:center;min-height:50vh">
        <img :src="previewImageUrl" style="max-width:100%;max-height:50vh;object-fit:contain" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, onBeforeUnmount, reactive, ref, nextTick, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRouter } from 'vue-router';
import { DataAnalysis, Shop, Goods, List, Service, Lock, SwitchButton, Plus, ChatDotRound } from '@element-plus/icons-vue';
import { getMerchantDashboard, getMerchantShopInfo, updateMerchantShop, getMerchantGoods, saveMerchantGoods, toggleMerchantGoodsStatus, getMerchantOrders, deliverMerchantOrder, getMerchantAfterSales, handleMerchantAfterSale, changeMerchantPassword, getMerchantMessages, replyMerchantMessage, getMerchantOrderDetail } from '../../api/merchant';
import request from '../../api/index';
import { useUserStore } from '../../store/user';
import ImageUpload from '../../components/ImageUpload.vue';
import * as echarts from 'echarts';

const router = useRouter();
const userStore = useUserStore();
userStore.syncFromStorage();
const tab = ref('overview');
const dashboardData = ref({});
const shopForm = reactive({ shopName: '', shopDesc: '', businessHours: '', shopStatus: 0, shopLogo: '' });
const goodsList = ref([]);
const orderList = ref([]);
const afterSaleList = ref([]);
const messageList = ref([]);
const messageReplyVisible = ref(false);
const messageReplyingRow = ref(null);
const messageReplyContent = ref('');
const categoryList = ref([]);
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' });
const orderStatusMap = { 0: '待付款', 1: '待发货', 2: '待收货', 3: '已完成', 4: '已取消', 5: '售后中', 6: '已退款' };
function orderStatusType(s) { return { 0: 'warning', 1: '', 2: '', 3: 'success', 4: 'info', 5: 'danger', 6: '' }[s] || ''; }

const businessHoursOptions = [
  '全天营业 (00:00-24:00)',
  '早班 (06:00-18:00)',
  '日班 (08:00-20:00)',
  '正常营业 (09:00-21:00)',
  '标准营业 (09:00-22:00)',
  '晚班 (10:00-23:00)',
  '夜间营业 (14:00-02:00)'
];

const overviewCards = computed(() => {
  const d = dashboardData.value;
  return [
    { label: '商品总数', value: d.goodsCount || 0 },
    { label: '订单总数', value: d.orderCount || 0 },
    { label: '总销售额', value: '¥' + (d.totalAmount || 0) }
  ];
});

const todoCards = computed(() => {
  const d = dashboardData.value;
  return [
    { label: '待发货订单', value: d.pendingDelivery || 0, action: () => { tab.value = 'order'; } },
    { label: '待处理售后', value: d.pendingAfterSale || 0, action: () => { tab.value = 'afterSale'; } },
    { label: '待处理留言', value: d.pendingMessage || 0, action: () => { tab.value = 'message'; } }
  ];
});

const goodsSearch = ref('');
const goodsCategoryFilter = ref(null);
const goodsSort = ref('');
const orderSearchNo = ref('');
const orderSearchUserId = ref('');
const orderStatusFilter = ref(null);
const msgSearchAccount = ref('');
const msgSearchTitle = ref('');
const afterSaleSearchOrderId = ref('');
const afterSaleStatusFilter = ref(null);
const afterSaleHandleVisible = ref(false);
const afterSaleHandleId = ref(null);
const afterSaleHandleStatus = ref(null);
const afterSaleHandleRemark = ref('');
const previewVisible = ref(false);
const previewImageUrl = ref('');

function openPreview(url) {
  previewImageUrl.value = url;
  previewVisible.value = true;
}
const filteredGoods = computed(() => {
  let list = [...goodsList.value];
  if (goodsSearch.value) list = list.filter(g => g.goodsName.includes(goodsSearch.value));
  if (goodsCategoryFilter.value) list = list.filter(g => g.categoryId === goodsCategoryFilter.value);
  if (goodsSort.value === 'priceAsc') list.sort((a, b) => a.salePrice - b.salePrice);
  else if (goodsSort.value === 'priceDesc') list.sort((a, b) => b.salePrice - a.salePrice);
  else if (goodsSort.value === 'salesAsc') list.sort((a, b) => (a.salesCount || 0) - (b.salesCount || 0));
  else if (goodsSort.value === 'salesDesc') list.sort((a, b) => (b.salesCount || 0) - (a.salesCount || 0));
  return list;
});

const filteredOrders = computed(() => {
  let list = [...orderList.value];
  if (orderSearchNo.value) list = list.filter(o => o.orderNo && o.orderNo.includes(orderSearchNo.value));
  if (orderSearchUserId.value) list = list.filter(o => String(o.userId) === orderSearchUserId.value);
  if (orderStatusFilter.value !== null && orderStatusFilter.value !== '') list = list.filter(o => o.orderStatus === orderStatusFilter.value);
  return list;
});

const filteredMessages = computed(() => {
  let list = [...messageList.value];
  if (msgSearchAccount.value) list = list.filter(m => m.userAccount && m.userAccount.includes(msgSearchAccount.value));
  if (msgSearchTitle.value) list = list.filter(m => m.messageTitle && m.messageTitle.includes(msgSearchTitle.value));
  return list;
});

const filteredAfterSales = computed(() => {
  let list = [...afterSaleList.value];
  if (afterSaleSearchOrderId.value) list = list.filter(a => (a.orderNo && a.orderNo.includes(afterSaleSearchOrderId.value)) || String(a.orderId) === afterSaleSearchOrderId.value);
  if (afterSaleStatusFilter.value !== null && afterSaleStatusFilter.value !== '') list = list.filter(a => a.auditStatus === afterSaleStatusFilter.value);
  return list;
});

function filterGoods() {}
function resetGoodsFilter() { goodsSearch.value = ''; goodsCategoryFilter.value = null; goodsSort.value = ''; }
function filterOrders() {}
function resetOrderFilter() { orderSearchNo.value = ''; orderSearchUserId.value = ''; orderStatusFilter.value = null; }
function filterMessages() {}
function resetMsgFilter() { msgSearchAccount.value = ''; msgSearchTitle.value = ''; }
function filterAfterSales() {}
function resetAfterSaleFilter() { afterSaleSearchOrderId.value = ''; afterSaleStatusFilter.value = null; }

function getAfterSaleOrderNo(orderId) {
  const order = orderList.value.find(o => o.orderId === orderId);
  return order ? order.orderNo : orderId;
}

async function viewAfterSaleOrder(orderId) {
  try {
    const res = await getMerchantOrderDetail(orderId);
    orderDetailData.value = res.data;
    orderDetailVisible.value = true;
  } catch (e) {
    ElMessage.error('获取订单详情失败');
  }
}

function openAfterSaleHandle(afterSaleId, status) {
  afterSaleHandleId.value = afterSaleId;
  afterSaleHandleStatus.value = status;
  afterSaleHandleRemark.value = '';
  afterSaleHandleVisible.value = true;
}

async function confirmAfterSaleHandle() {
  try {
    await handleMerchantAfterSale(afterSaleHandleId.value, afterSaleHandleStatus.value, afterSaleHandleRemark.value);
    ElMessage.success(afterSaleHandleStatus.value === 1 ? '已同意退款' : '已驳回');
    afterSaleHandleVisible.value = false;
    loadAll();
  } catch (e) {
    ElMessage.error('操作失败');
  }
}

const goodsDialogVisible = ref(false);
const editingGoods = ref(null);
const goodsForm = reactive({ goodsId: null, goodsName: '', categoryId: null, marketPrice: 0, salePrice: 0, stock: 0, goodsImage: '', goodsDesc: '' });

const showLogoUpload = ref(false);
const tempLogo = ref('');
const orderDetailVisible = ref(false);
const orderDetailData = ref(null);

const categoryChartRef = ref(null);
const salesChartRef = ref(null);
const salesChartMode = ref('monthly');
let categoryChart = null;
let salesChart = null;

function handleMenuSelect(index) {
  if (index === 'logout') {
    userStore.clearAuth();
    localStorage.removeItem('token');
    localStorage.removeItem('userInfo');
    router.push('/login');
    return;
  }
  tab.value = index;
}

function formatDateTimeHtml(dateStr) {
  if (!dateStr) return '-';
  const d = new Date(dateStr);
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  const h = String(d.getHours()).padStart(2, '0');
  const min = String(d.getMinutes()).padStart(2, '0');
  return `${y}-${m}-${day}<br/>${h}:${min}`;
}

function initCategoryChart(d) {
  if (!categoryChartRef.value) return;
  if (categoryChart) { categoryChart.dispose(); categoryChart = null; }
  categoryChart = echarts.init(categoryChartRef.value);
  const stats = d.categoryStats || [];
  categoryChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c}个 ({d}%)' },
    legend: { top: 0 },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data: stats.map(s => ({ name: s.categoryName, value: s.goodsCount })),
      label: { fontSize: 12, formatter: '{b}{c}个' },
      emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.5)' } }
    }]
  });
}

function getSalesChartOption(d) {
  const isMonthly = salesChartMode.value === 'monthly';
  const data = isMonthly ? (d.monthlySales || []) : (d.dailySales || []);
  const xData = data.map(m => isMonthly ? m.month : m.date);
  const salesData = data.map(m => m.sales || 0);
  return {
    tooltip: { trigger: 'axis', formatter(p) { let r = p[0].axisValue + '<br/>'; p.forEach(i => { r += i.marker + '¥' + i.value + '<br/>'; }); return r; } },
    grid: { left: 70, right: 30, top: 50, bottom: 40 },
    xAxis: { type: 'category', data: xData, axisLabel: { fontSize: isMonthly ? 12 : 10, rotate: isMonthly ? 0 : 45 } },
    yAxis: { type: 'value', name: '销售额(元)', axisLabel: { fontSize: 12 } },
    series: [{ type: 'bar', data: salesData, barWidth: isMonthly ? 30 : 8, itemStyle: { color: '#165dff' }, label: { show: true, position: 'top', formatter: '¥{c}', fontSize: 11 } }]
  };
}

function initSalesChart(d) {
  if (!salesChartRef.value) return;
  if (salesChart) { salesChart.dispose(); salesChart = null; }
  salesChart = echarts.init(salesChartRef.value);
  salesChart.setOption(getSalesChartOption(d));
}

function updateSalesChart() {
  if (salesChart && dashboardData.value) salesChart.setOption(getSalesChartOption(dashboardData.value), true);
}

function handleResize() { categoryChart && categoryChart.resize(); salesChart && salesChart.resize(); }

function initCharts() {
  nextTick(() => {
    if (dashboardData.value && Object.keys(dashboardData.value).length > 0) {
      initCategoryChart(dashboardData.value);
      initSalesChart(dashboardData.value);
    }
  });
}

watch(tab, (val) => {
  if (val === 'overview') {
    initCharts();
  }
});

async function loadAll() {
  try { const dash = await getMerchantDashboard(); dashboardData.value = dash.data || {}; } catch (e) { console.error('加载概览失败', e); }
  try { const shop = await getMerchantShopInfo(); Object.assign(shopForm, shop.data || {}); } catch (e) { console.error('加载店铺失败', e); }
  try {
    const [goods, cats] = await Promise.all([getMerchantGoods(), request.get('/public/categories', { headers: { 'X-Silent': 'true' } })]);
    const rawGoods = goods.data || [];
    const catMap = {};
    (cats.data || []).forEach(c => { catMap[c.categoryId] = c.categoryName; });
    rawGoods.forEach(g => { g.categoryName = catMap[g.categoryId] || '未分类'; });
    goodsList.value = rawGoods;
    categoryList.value = cats.data || [];
  } catch (e) { console.error('加载商品失败', e); }
  try { const orders = await getMerchantOrders(); orderList.value = orders.data || []; } catch (e) { console.error('加载订单失败', e); }
  try { const afterSales = await getMerchantAfterSales(); afterSaleList.value = afterSales.data || []; } catch (e) { console.error('加载售后失败', e); }
  try { const msgs = await getMerchantMessages(); messageList.value = msgs.data || []; } catch (e) { console.error('加载留言失败', e); }
  initCharts();
  window.addEventListener('resize', handleResize);
}

async function saveShop() { await updateMerchantShop(shopForm); ElMessage.success('店铺信息已保存'); }
function handleLogoSuccess(url) { tempLogo.value = url; }
async function saveLogo() {
  if (!tempLogo.value) { ElMessage.warning('请先上传Logo'); return; }
  shopForm.shopLogo = tempLogo.value;
  await updateMerchantShop({ shopLogo: tempLogo.value });
  ElMessage.success('Logo更新成功');
  showLogoUpload.value = false;
  tempLogo.value = '';
}
function openGoodsDialog(row) {
  if (row) { editingGoods.value = row; Object.assign(goodsForm, row); }
  else { editingGoods.value = null; Object.assign(goodsForm, { goodsId: null, goodsName: '', categoryId: null, marketPrice: 0, salePrice: 0, stock: 0, goodsImage: '', goodsDesc: '' }); }
  goodsDialogVisible.value = true;
}
async function saveGoods() { await saveMerchantGoods(goodsForm); goodsDialogVisible.value = false; ElMessage.success('保存成功'); loadAll(); }
async function toggleGoodsStatus(goodsId) { await toggleMerchantGoodsStatus(goodsId); loadAll(); }
async function deliverOrder(orderId) { await deliverMerchantOrder(orderId); ElMessage.success('已发货'); loadAll(); }
async function handleAfterSale(afterSaleId, status) { await handleMerchantAfterSale(afterSaleId, status); ElMessage.success(status === 1 ? '已同意退款' : '已驳回'); loadAll(); }

async function changePwd() {
  if (!pwdForm.oldPassword || !pwdForm.newPassword || !pwdForm.confirmPassword) { ElMessage.warning('请填写完整'); return; }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) { ElMessage.warning('两次密码不一致'); return; }
  await changeMerchantPassword(pwdForm);
  ElMessage.success('密码修改成功');
  Object.assign(pwdForm, { oldPassword: '', newPassword: '', confirmPassword: '' });
}

function openMessageReply(row) {
  messageReplyingRow.value = row;
  messageReplyContent.value = row.replyContent || '';
  messageReplyVisible.value = true;
}

async function handleMessageReply() {
  if (!messageReplyContent.value.trim()) { ElMessage.warning('请输入回复内容'); return; }
  await replyMerchantMessage(messageReplyingRow.value.messageId, { replyContent: messageReplyContent.value });
  ElMessage.success('回复成功');
  messageReplyVisible.value = false;
  loadAll();
}

async function viewOrderDetail(orderId) {
  try {
    const res = await getMerchantOrderDetail(orderId);
    orderDetailData.value = res.data;
    orderDetailVisible.value = true;
  } catch (e) {
    ElMessage.error('获取订单详情失败');
  }
}

onMounted(loadAll);
onBeforeUnmount(() => { window.removeEventListener('resize', handleResize); categoryChart && categoryChart.dispose(); salesChart && salesChart.dispose(); });
</script>

<style scoped lang="less">
.merchant-layout { display: flex; min-height: 100vh; background: var(--theme-bg); }

.merchant-sidebar {
  width: 220px; background: linear-gradient(180deg, #1d1e3a 0%, #2d2e5e 100%); flex-shrink: 0;
  display: flex; flex-direction: column; position: fixed; top: 0; left: 0; bottom: 0; overflow-y: auto;

  .sidebar-header {
    padding: 24px 16px 16px; text-align: center; border-bottom: 1px solid rgba(255,255,255,0.08);

    .avatar-wrapper {
      margin-bottom: 10px; cursor: pointer; position: relative;

      .avatar-inner {
        width: 64px; height: 64px; border-radius: 50%; background: linear-gradient(135deg, #165dff, #722ed1);
        display: flex; align-items: center; justify-content: center; margin: 0 auto; font-size: 28px; overflow: hidden;

        .avatar-img { width: 100%; height: 100%; object-fit: cover; }
      }

      .avatar-edit-tip {
        position: absolute; bottom: 0; left: 50%; transform: translateX(-50%);
        background: rgba(0,0,0,0.5); color: #fff; font-size: 10px; padding: 2px 8px;
        border-radius: 0 0 16px 16px; opacity: 0; transition: opacity 0.2s; white-space: nowrap;
      }

      &:hover .avatar-edit-tip { opacity: 1; }
    }

    .user-name { color: #fff; font-size: 16px; font-weight: 600; }
  }

  .sidebar-menu {
    border-right: none; flex: 1; background: transparent !important;

    :deep(.el-menu-item) {
      color: #a0a3bd; height: 48px; line-height: 48px;
      &.is-active { background: var(--theme-primary-gradient) !important; color: #fff !important; }
      &:hover { background: rgba(22,93,255,0.15) !important; }
    }
  }
}

.merchant-main { flex: 1; padding: 24px; margin-left: 220px; min-height: 100vh; }

.page-title { font-size: 20px; font-weight: 600; color: #1d2129; margin: 0 0 16px; }

.stat-row {
  .stat-card {
    background: #fff; border-radius: 12px; padding: 24px 20px; text-align: center;
    box-shadow: 0 2px 8px rgba(0,0,0,0.06); transition: all 0.2s; margin-bottom: 12px;
    &:hover { transform: translateY(-2px); box-shadow: 0 4px 16px rgba(0,0,0,0.08); }
    .stat-value { font-size: 28px; font-weight: 700; background: linear-gradient(90deg, #165dff, #722ed1); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
    .stat-label { font-size: 14px; color: #86909c; margin-top: 8px; }
  }
}

.chart-card {
  background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); margin-bottom: 12px; height: 360px;
  .chart-title-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
  .chart-title { font-size: 16px; font-weight: 600; color: #1d2129; }
  .chart-container { width: 100%; height: 300px; }
}

.todo-card {
  background: #fff; border-radius: 12px; padding: 24px 20px; text-align: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06); cursor: pointer; transition: all 0.2s; margin-bottom: 12px; border-left: 4px solid #ff7d00;
  &:hover { transform: translateY(-2px); box-shadow: 0 4px 16px rgba(0,0,0,0.08); }
  .todo-value { font-size: 28px; font-weight: 700; color: #ff7d00; }
  .todo-label { font-size: 14px; color: #86909c; margin-top: 8px; }
}

.section-card {
  background: #fff; border-radius: var(--theme-radius-lg); padding: 24px; box-shadow: var(--theme-shadow);
  h3 { font-size: 18px; margin: 0 0 20px; color: var(--theme-text); }
}

.card-header-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; h3 { margin: 0; } }

.filter-row { display: flex; gap: 12px; margin-bottom: 16px; align-items: center; }

.logo-upload-area {
  width: 120px; height: 120px; border: 1px dashed #dcdfe6; border-radius: 8px; cursor: pointer;
  display: flex; align-items: center; justify-content: center; overflow: hidden; transition: border-color 0.2s;
  &:hover { border-color: #165dff; }
  .logo-preview { width: 100%; height: 100%; object-fit: cover; }
  .logo-placeholder { font-size: 32px; color: #8c939d; }
}

.large-table {
  :deep(.el-table__header th) { height: 48px; padding: 8px 0; font-size: 14px; font-weight: 600; }
  :deep(.el-table__body td) { height: 48px; padding: 8px 0; font-size: 14px; }
  :deep(.cell) { display: flex; align-items: center; justify-content: center; }
}

.link-text {
  color: #165dff;
  cursor: pointer;
  text-decoration: none;
  transition: color 0.2s;
  &:hover {
    color: #722ed1;
    text-decoration: underline;
  }
}

.order-link {
  color: #165dff;
  cursor: pointer;
  text-decoration: none;
  transition: color 0.2s;
  &:hover {
    color: #722ed1;
    text-decoration: underline;
  }
}
</style>
