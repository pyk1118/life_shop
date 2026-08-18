<template>
  <div class="admin-after-sale animate-fade-in">
    <h3>售后管理（平台介入）</h3>
    <el-table :data="afterSales" border stripe style="width:100%" class="large-table" v-loading="loading">
      <el-table-column prop="afterSaleId" label="ID" width="80" align="center" />
      <el-table-column label="订单编号" width="210" align="center">
        <template #default="{ row }">
          <a class="order-link" @click.prevent="viewOrderDetail(row.orderId)">{{ row.orderNo || row.orderId }}</a>
        </template>
      </el-table-column>
      <el-table-column prop="refundAmount" label="退款金额" width="120" align="center">
        <template #default="{ row }">¥{{ row.refundAmount }}</template>
      </el-table-column>
      <el-table-column prop="refundReason" label="退款原因" min-width="150" align="center" show-overflow-tooltip />
      <el-table-column label="凭证" width="100" align="center">
        <template #default="{ row }">
          <img v-if="row.refundVoucher" :src="row.refundVoucher" style="width:50px;height:50px;object-fit:cover;cursor:pointer;border-radius:4px" @click="openPreview(row.refundVoucher)" />
          <span v-else style="color:#999">无</span>
        </template>
      </el-table-column>
      <el-table-column prop="auditStatus" label="状态" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="row.auditStatus === 0 ? 'warning' : row.auditStatus === 1 ? 'success' : 'danger'">
            {{ row.auditStatus === 0 ? '待处理' : row.auditStatus === 1 ? '已同意' : '已驳回' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="auditRemark" label="商家备注" width="150" align="center" show-overflow-tooltip />
      <el-table-column prop="adminRemark" label="开发者备注" width="150" align="center" show-overflow-tooltip />
      <el-table-column label="操作" width="200" align="center">
        <template #default="{ row }">
          <template v-if="row.auditStatus === 0">
            <el-button size="small" type="success" @click="handleAfterSale(row.afterSaleId, 1)">同意退款</el-button>
            <el-button size="small" type="danger" @click="handleAfterSale(row.afterSaleId, 2)">驳回</el-button>
          </template>
          <span v-else style="color:#999">已处理</span>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="remarkDialogVisible" title="填写备注" width="400px">
      <el-input v-model="adminRemark" type="textarea" :rows="3" placeholder="请输入备注（可选）" />
      <template #footer>
        <el-button @click="remarkDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmHandle">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="orderDetailVisible" title="订单详情" width="750px">
      <template v-if="orderDetailData">
        <el-descriptions :column="2" border size="small" style="margin-bottom: 16px">
          <el-descriptions-item label="订单编号">{{ orderDetailData.orderInfo.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">{{ orderStatusMap[orderDetailData.orderInfo.orderStatus] }}</el-descriptions-item>
          <el-descriptions-item label="总金额">¥{{ orderDetailData.orderInfo.totalAmount }}</el-descriptions-item>
          <el-descriptions-item label="收货人">{{ orderDetailData.orderInfo.receiverName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ orderDetailData.orderInfo.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ orderDetailData.orderInfo.receiverAddress }}</el-descriptions-item>
        </el-descriptions>
        <h4 style="margin: 12px 0 8px">订单商品</h4>
        <el-table :data="orderDetailData.orderItems" border size="small">
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

    <el-dialog v-model="previewVisible" width="50%" :show-close="true" @close="previewVisible = false; previewImageUrl = ''" destroy-on-close>
      <div style="display:flex;justify-content:center;align-items:center;min-height:50vh">
        <img :src="previewImageUrl" style="max-width:100%;max-height:50vh;object-fit:contain" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { getAdminAfterSaleList, handleAdminAfterSale, getOrderDetail } from '../../api/admin';

const afterSales = ref([]);
const loading = ref(false);
const remarkDialogVisible = ref(false);
const adminRemark = ref('');
const currentAfterSaleId = ref(null);
const currentAuditStatus = ref(null);
const orderDetailVisible = ref(false);
const orderDetailData = ref(null);
const previewVisible = ref(false);
const previewImageUrl = ref('');

function openPreview(url) {
  previewImageUrl.value = url;
  previewVisible.value = true;
}

const orderStatusMap = { 0: '待付款', 1: '待发货', 2: '待收货', 3: '已完成', 4: '已取消', 5: '退款中', 6: '已退款' };

async function loadAfterSales() {
  loading.value = true;
  try {
    const res = await getAdminAfterSaleList();
    afterSales.value = res.data || [];
  } catch (e) {
    console.error('加载售后列表失败', e);
  } finally {
    loading.value = false;
  }
}

function handleAfterSale(afterSaleId, auditStatus) {
  currentAfterSaleId.value = afterSaleId;
  currentAuditStatus.value = auditStatus;
  adminRemark.value = '';
  remarkDialogVisible.value = true;
}

async function confirmHandle() {
  try {
    await handleAdminAfterSale(currentAfterSaleId.value, currentAuditStatus.value, adminRemark.value);
    ElMessage.success(currentAuditStatus.value === 1 ? '已同意退款' : '已驳回');
    remarkDialogVisible.value = false;
    loadAfterSales();
  } catch (e) {
    ElMessage.error('操作失败');
  }
}

async function viewOrderDetail(orderId) {
  try {
    const res = await getOrderDetail(orderId);
    orderDetailData.value = res.data;
    orderDetailVisible.value = true;
  } catch (e) {
    ElMessage.error('获取订单详情失败');
  }
}

onMounted(loadAfterSales);
</script>

<style scoped>
.admin-after-sale {
  padding: 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}
.admin-after-sale h3 {
  font-size: 20px;
  margin: 0 0 20px;
  font-weight: 600;
}
.large-table :deep(.el-table__cell) {
  font-size: 14px;
  padding: 12px 8px;
  text-align: center;
  vertical-align: middle;
}
.large-table :deep(.el-table__header th) {
  text-align: center;
  vertical-align: middle;
}
.order-link {
  color: #165dff;
  cursor: pointer;
  text-decoration: none;
  transition: color 0.2s;
}
.order-link:hover {
  color: #722ed1;
  text-decoration: underline;
}
.animate-fade-in {
  animation: fade-in 0.3s ease-out;
}
@keyframes fade-in {
  from { opacity: 0; }
  to { opacity: 1; }
}
</style>
