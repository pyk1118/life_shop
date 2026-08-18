<template>
  <div class="admin-page">
    <h3 class="page-title">商家列表</h3>
    <div class="filter-bar">
      <el-input v-model="filters.merchantAccount" placeholder="商家账号" clearable style="width: 180px" />
      <el-select v-model="filters.merchantStatus" placeholder="状态" clearable style="width: 130px">
        <el-option label="正常" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button @click="resetFilters">重置</el-button>
    </div>
    <el-table :data="tableData" border stripe style="width: 100%" class="large-table">
      <el-table-column prop="merchantId" label="ID" width="100" />
      <el-table-column prop="merchantAccount" label="商家账号" width="300" />
      <el-table-column prop="userId" label="关联用户ID" width="150" />
      <el-table-column prop="shopId" label="关联店铺ID" width="150" />
      <el-table-column prop="merchantStatus" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="row.merchantStatus === 1 ? 'success' : 'danger'">
            {{ row.merchantStatus === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="291">
        <template #default="{ row }"><span v-html="formatDateTimeHtml(row.createTime)"></span></template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="280">
        <template #default="{ row }">
          <el-button size="small" @click="viewDetail(row.merchantId)">详情</el-button>
          <el-button
            size="small"
            :type="row.merchantStatus === 1 ? 'danger' : 'success'"
            @click="handleToggleStatus(row)"
          >
            {{ row.merchantStatus === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button size="small" type="warning" @click="handleResetPwd(row.merchantId)">重置密码</el-button>
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

    <el-dialog v-model="detailVisible" title="商家详情" width="650px">
      <template v-if="detailData">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="商家ID">{{ detailData.merchantInfo.merchantId }}</el-descriptions-item>
          <el-descriptions-item label="商家账号">{{ detailData.merchantInfo.merchantAccount }}</el-descriptions-item>
          <el-descriptions-item label="商家状态">{{ detailData.merchantInfo.merchantStatus === 1 ? '正常' : '禁用' }}</el-descriptions-item>
          <el-descriptions-item label="审核状态">{{ detailData.merchantInfo.auditStatus === 1 ? '已通过' : '未通过' }}</el-descriptions-item>
        </el-descriptions>
        <template v-if="detailData.userInfo">
          <h4 style="margin: 16px 0 8px">关联用户信息</h4>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="用户ID">{{ detailData.userInfo.userId }}</el-descriptions-item>
            <el-descriptions-item label="账号">{{ detailData.userInfo.userAccount }}</el-descriptions-item>
            <el-descriptions-item label="姓名">{{ detailData.userInfo.realName }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ detailData.userInfo.phone }}</el-descriptions-item>
          </el-descriptions>
        </template>
        <template v-if="detailData.shopInfo">
          <h4 style="margin: 16px 0 8px">店铺信息</h4>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="店铺ID">{{ detailData.shopInfo.shopId }}</el-descriptions-item>
            <el-descriptions-item label="店铺名称">{{ detailData.shopInfo.shopName }}</el-descriptions-item>
            <el-descriptions-item label="店铺状态">{{ detailData.shopInfo.shopStatus === 1 ? '营业中' : '已关闭' }}</el-descriptions-item>
            <el-descriptions-item label="营业时间">{{ detailData.shopInfo.businessHours || '无' }}</el-descriptions-item>
          </el-descriptions>
        </template>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getMerchantList, getMerchantDetail, toggleMerchantStatus, resetMerchantPassword } from '../../api/admin';
import { formatDateTimeHtml } from '../../utils/datetime';

const filters = reactive({ merchantAccount: '', merchantStatus: undefined });
const pagination = reactive({ current: 1, size: 10, total: 0 });
const tableData = ref([]);
const detailVisible = ref(false);
const detailData = ref(null);

onMounted(() => loadData());

async function loadData() {
  const res = await getMerchantList({
    current: pagination.current,
    size: pagination.size,
    merchantAccount: filters.merchantAccount || undefined,
    merchantStatus: filters.merchantStatus !== undefined && filters.merchantStatus !== null ? filters.merchantStatus : undefined
  });
  tableData.value = res.data.records;
  pagination.total = Number(res.data.total);
}

function resetFilters() {
  filters.merchantAccount = '';
  filters.merchantStatus = undefined;
  pagination.current = 1;
  loadData();
}

async function viewDetail(merchantId) {
  const res = await getMerchantDetail(merchantId);
  detailData.value = res.data;
  detailVisible.value = true;
}

async function handleToggleStatus(row) {
  const action = row.merchantStatus === 1 ? '禁用' : '启用';
  await ElMessageBox.confirm(`确定${action}商家 ${row.merchantAccount} 吗？`, '提示', { type: 'warning' });
  await toggleMerchantStatus(row.merchantId);
  ElMessage.success(`已${action}该商家`);
  loadData();
}

async function handleResetPwd(merchantId) {
  await ElMessageBox.confirm('确定重置该商家密码为123456吗？', '提示', { type: 'warning' });
  await resetMerchantPassword(merchantId);
  ElMessage.success('密码已重置为123456');
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