<template>
  <div class="admin-page">
    <h3 class="page-title">用户管理</h3>
    <div class="filter-bar">
      <el-input v-model="filters.account" placeholder="用户账号" clearable style="width: 180px" />
      <el-input v-model="filters.phone" placeholder="手机号" clearable style="width: 180px" />
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button @click="resetFilters">重置</el-button>
    </div>
    <el-table :data="tableData" border stripe style="width: 100%" class="large-table">
      <el-table-column prop="userId" label="ID" width="80" />
      <el-table-column prop="userAccount" label="账号" width="140" />
      <el-table-column prop="realName" label="姓名" width="140" />
      <el-table-column prop="phone" label="手机号" width="180" />
      <el-table-column prop="email" label="邮箱" width="220" />
      <el-table-column prop="isMerchant" label="是否商家" width="140">
        <template #default="{ row }">
          <el-tag :type="row.isMerchant === 1 ? 'success' : 'info'">
            {{ row.isMerchant === 1 ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="111">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="注册时间" width="180">
        <template #default="{ row }"><span v-html="formatDateTimeHtml(row.createTime)"></span></template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="viewDetail(row.userId)">详情</el-button>
          <el-button
            size="small"
            :type="row.status === 1 ? 'danger' : 'success'"
            @click="handleToggleStatus(row)"
          >
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
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

    <el-dialog v-model="detailVisible" title="用户详情" width="700px">
      <template v-if="detailData">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户ID">{{ detailData.userInfo.userId }}</el-descriptions-item>
          <el-descriptions-item label="账号">{{ detailData.userInfo.userAccount }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ detailData.userInfo.realName }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ detailData.userInfo.phone }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ detailData.userInfo.email }}</el-descriptions-item>
          <el-descriptions-item label="地址">{{ detailData.userInfo.address }}</el-descriptions-item>
          <el-descriptions-item label="是否商家">{{ detailData.userInfo.isMerchant === 1 ? '是' : '否' }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ detailData.userInfo.status === 1 ? '正常' : '禁用' }}</el-descriptions-item>
        </el-descriptions>
        <h4 style="margin: 16px 0 8px">历史订单</h4>
        <el-table :data="detailData.orderList" border size="small" max-height="200">
          <el-table-column prop="orderNo" label="订单编号" width="180" />
          <el-table-column prop="totalAmount" label="金额" width="100" />
          <el-table-column prop="orderStatus" label="状态" width="90">
            <template #default="{ row }">{{ orderStatusMap[row.orderStatus] || '未知' }}</template>
          </el-table-column>
        </el-table>
        <h4 style="margin: 16px 0 8px">收货地址</h4>
        <el-table :data="detailData.addressList" border size="small" max-height="150">
          <el-table-column prop="receiverName" label="收货人" width="100" />
          <el-table-column prop="receiverPhone" label="电话" width="130" />
          <el-table-column prop="receiverAddress" label="地址" />
          <el-table-column prop="isDefault" label="默认" width="60">
            <template #default="{ row }">{{ row.isDefault === 1 ? '是' : '否' }}</template>
          </el-table-column>
        </el-table>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getUserList, getUserDetail, toggleUserStatus } from '../../api/admin';
import { formatDateTimeHtml } from '../../utils/datetime';

const orderStatusMap = { 0: '待付款', 1: '待发货', 2: '待收货', 3: '已完成', 4: '已取消', 5: '退款中' };

const filters = reactive({ account: '', phone: '' });
const pagination = reactive({ current: 1, size: 10, total: 0 });
const tableData = ref([]);
const detailVisible = ref(false);
const detailData = ref(null);

onMounted(() => loadData());

async function loadData() {
  const res = await getUserList({
    current: pagination.current,
    size: pagination.size,
    account: filters.account || undefined,
    phone: filters.phone || undefined
  });
  tableData.value = res.data.records;
  pagination.total = Number(res.data.total);
}

function resetFilters() {
  filters.account = '';
  filters.phone = '';
  pagination.current = 1;
  loadData();
}

async function viewDetail(userId) {
  const res = await getUserDetail(userId);
  detailData.value = res.data;
  detailVisible.value = true;
}

async function handleToggleStatus(row) {
  const action = row.status === 1 ? '禁用' : '启用';
  await ElMessageBox.confirm(`确定${action}用户 ${row.userAccount} 吗？`, '提示', { type: 'warning' });
  await toggleUserStatus(row.userId);
  ElMessage.success(`已${action}该用户`);
  loadData();
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