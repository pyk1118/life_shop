<template>
  <div class="admin-page">
    <h3 class="page-title">订单管理</h3>
    <div class="filter-bar">
      <el-input v-model="filters.orderNo" placeholder="订单编号" clearable style="width: 180px" />
      <el-input v-model="filters.userId" placeholder="用户ID" clearable style="width: 120px" />
      <el-input v-model="filters.shopId" placeholder="店铺ID" clearable style="width: 120px" />
      <el-select v-model="filters.status" placeholder="订单状态" clearable style="width: 120px">
        <el-option label="待付款" :value="0" />
        <el-option label="待发货" :value="1" />
        <el-option label="待收货" :value="2" />
        <el-option label="已完成" :value="3" />
        <el-option label="已取消" :value="4" />
        <el-option label="退款中" :value="5" />
        <el-option label="已退款" :value="6" />
      </el-select>
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button @click="resetFilters">重置</el-button>
    </div>
    <el-table :data="tableData" border stripe style="width: 100%" class="large-table">
      <el-table-column prop="orderId" label="ID" width="100" />
      <el-table-column prop="orderNo" label="订单编号" width="240" />
      <el-table-column prop="userId" label="用户ID" width="150" />
      <el-table-column prop="shopId" label="店铺ID" width="150" />
      <el-table-column prop="totalAmount" label="总金额" width="150" />
      <el-table-column prop="orderStatus" label="状态" width="150">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.orderStatus)">{{ orderStatusMap[row.orderStatus] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="receiverName" label="收货人" width="150" />
      <el-table-column label="下单时间" width="180">
        <template #default="{ row }"><span v-html="formatDateTimeHtml(row.createTime)"></span></template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="121">
        <template #default="{ row }">
          <el-button size="small" @click="viewDetail(row.orderId)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        layout="total, prev, pager, next"
        @current-change="loadData"
      />
    </div>

    <el-dialog v-model="detailVisible" title="订单详情" width="750px">
      <template v-if="detailData">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单ID">{{ detailData.orderInfo.orderId }}</el-descriptions-item>
          <el-descriptions-item label="订单编号">{{ detailData.orderInfo.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="用户ID">{{ detailData.orderInfo.userId }}</el-descriptions-item>
          <el-descriptions-item label="商家ID">{{ detailData.orderInfo.merchantId }}</el-descriptions-item>
          <el-descriptions-item label="店铺ID">{{ detailData.orderInfo.shopId }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">{{ orderStatusMap[detailData.orderInfo.orderStatus] }}</el-descriptions-item>
          <el-descriptions-item label="总金额">¥{{ detailData.orderInfo.totalAmount }}</el-descriptions-item>
          <el-descriptions-item label="支付方式">{{ detailData.orderInfo.payType || '未支付' }}</el-descriptions-item>
          <el-descriptions-item label="收货人">{{ detailData.orderInfo.receiverName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ detailData.orderInfo.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ detailData.orderInfo.receiverAddress }}</el-descriptions-item>
          <el-descriptions-item label="物流单号">{{ detailData.orderInfo.logisticsNo || '无' }}</el-descriptions-item>
          <el-descriptions-item label="支付时间">{{ detailData.orderInfo.payTime || '无' }}</el-descriptions-item>
          <el-descriptions-item label="发货时间">{{ detailData.orderInfo.deliverTime || '无' }}</el-descriptions-item>
          <el-descriptions-item label="收货时间">{{ detailData.orderInfo.receiveTime || '无' }}</el-descriptions-item>
        </el-descriptions>
        <h4 style="margin: 16px 0 8px">订单明细</h4>
        <el-table :data="detailData.orderItems" border size="small">
          <el-table-column prop="goodsName" label="商品名称" min-width="150" />
          <el-table-column prop="goodsPrice" label="单价" width="100" />
          <el-table-column prop="goodsNum" label="数量" width="80" />
          <el-table-column prop="totalPrice" label="小计" width="100" />
        </el-table>
        <template v-if="detailData.userInfo">
          <h4 style="margin: 16px 0 8px">下单用户</h4>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="用户ID">{{ detailData.userInfo.userId }}</el-descriptions-item>
            <el-descriptions-item label="账号">{{ detailData.userInfo.userAccount }}</el-descriptions-item>
            <el-descriptions-item label="姓名">{{ detailData.userInfo.realName }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ detailData.userInfo.phone }}</el-descriptions-item>
          </el-descriptions>
        </template>
        <template v-if="detailData.shopInfo">
          <h4 style="margin: 16px 0 8px">所属店铺</h4>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="店铺ID">{{ detailData.shopInfo.shopId }}</el-descriptions-item>
            <el-descriptions-item label="店铺名称">{{ detailData.shopInfo.shopName }}</el-descriptions-item>
          </el-descriptions>
        </template>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { getOrderList, getOrderDetail } from '../../api/admin';
import { formatDateTimeHtml } from '../../utils/datetime';

const orderStatusMap = { 0: '待付款', 1: '待发货', 2: '待收货', 3: '已完成', 4: '已取消', 5: '退款中', 6: '已退款' };

function statusTagType(status) {
  const map = { 0: 'warning', 1: '', 2: '', 3: 'success', 4: 'info', 5: 'danger', 6: '' };
  return map[status] || '';
}

const filters = reactive({ orderNo: '', userId: '', shopId: '', status: undefined });
const pagination = reactive({ current: 1, size: 9, total: 0 });
const tableData = ref([]);
const detailVisible = ref(false);
const detailData = ref(null);

onMounted(() => loadData());

async function loadData() {
  const res = await getOrderList({
    current: pagination.current,
    size: pagination.size,
    orderNo: filters.orderNo || undefined,
    userId: filters.userId ? Number(filters.userId) : undefined,
    shopId: filters.shopId ? Number(filters.shopId) : undefined,
    status: filters.status !== undefined && filters.status !== null ? filters.status : undefined
  });
  tableData.value = res.data.records;
  pagination.total = Number(res.data.total);
}

function resetFilters() {
  filters.orderNo = '';
  filters.userId = '';
  filters.shopId = '';
  filters.status = undefined;
  pagination.current = 1;
  loadData();
}

async function viewDetail(orderId) {
  const res = await getOrderDetail(orderId);
  detailData.value = res.data;
  detailVisible.value = true;
}
</script>

<style scoped lang="less">
.admin-page {
  .page-title {
    font-size: 20px;
    font-weight: 600;
    color: #1d2129;
    margin: 0 0 16px;
  }

  .filter-bar {
    display: flex;
    gap: 12px;
    margin-bottom: 16px;
    flex-wrap: wrap;
  }

  .pagination-wrap {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }
}

.large-table {
  :deep(.el-table__cell) {
    font-size: 15px;
    padding: 16px 12px;
    line-height: 1.6;
    white-space: normal;
    word-break: break-all;
    text-align: center;
    vertical-align: middle;
  }

  :deep(.el-table__header th) {
    font-size: 15px;
    font-weight: 600;
    padding: 16px 12px;
    line-height: 1.6;
    white-space: normal;
    word-break: break-all;
    text-align: center;
    vertical-align: middle;
  }

  :deep(.el-table__row) {
    height: auto;
  }
}
</style>