<template>
  <div class="admin-page">
    <h3 class="page-title">入驻审核</h3>
    <div class="filter-bar">
      <el-select v-model="filters.auditStatus" placeholder="审核状态" clearable style="width: 150px">
        <el-option label="待审核" :value="0" />
        <el-option label="已通过" :value="1" />
        <el-option label="已驳回" :value="2" />
      </el-select>
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button @click="resetFilters">重置</el-button>
    </div>
    <el-table :data="tableData" border stripe style="width: 100%" class="large-table">
      <el-table-column prop="merchantId" label="ID" width="100" />
      <el-table-column prop="merchantAccount" label="商家账号" width="180" />
      <el-table-column prop="userId" label="关联用户ID" width="180" />
      <el-table-column prop="auditStatus" label="审核状态" width="150">
        <template #default="{ row }">
          <el-tag :type="auditTagType(row.auditStatus)">{{ auditLabel(row.auditStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="auditRemark" label="审核备注" min-width="150" />
      <el-table-column label="申请时间" width="180">
        <template #default="{ row }"><span v-html="formatDateTimeHtml(row.createTime)"></span></template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="250">
        <template #default="{ row }">
          <el-button size="small" @click="viewDetail(row.merchantId)">详情</el-button>
          <template v-if="row.auditStatus === 0">
            <el-button size="small" type="success" @click="handleAudit(row.merchantId, 1)">通过</el-button>
            <el-button size="small" type="danger" @click="handleAudit(row.merchantId, 2)">驳回</el-button>
          </template>
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
          <el-descriptions-item label="审核状态">{{ auditLabel(detailData.merchantInfo.auditStatus) }}</el-descriptions-item>
          <el-descriptions-item label="审核备注">{{ detailData.merchantInfo.auditRemark || '无' }}</el-descriptions-item>
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
            <el-descriptions-item label="店铺描述">{{ detailData.shopInfo.shopDesc || '无' }}</el-descriptions-item>
            <el-descriptions-item label="营业时间">{{ detailData.shopInfo.businessHours || '无' }}</el-descriptions-item>
          </el-descriptions>
        </template>
      </template>
    </el-dialog>

    <el-dialog v-model="auditDialogVisible" title="审核备注" width="400px">
      <el-input v-model="auditRemark" type="textarea" :rows="3" placeholder="请输入审核备注（可选）" />
      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAudit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { getMerchantAuditList, getMerchantDetail, auditMerchant } from '../../api/admin';
import { formatDateTimeHtml } from '../../utils/datetime';

const filters = reactive({ auditStatus: undefined });
const pagination = reactive({ current: 1, size: 10, total: 0 });
const tableData = ref([]);
const detailVisible = ref(false);
const detailData = ref(null);
const auditDialogVisible = ref(false);
const auditRemark = ref('');
const pendingAuditId = ref(null);
const pendingAuditStatus = ref(null);

onMounted(() => loadData());

function auditTagType(status) {
  return status === 0 ? 'warning' : status === 1 ? 'success' : 'danger';
}
function auditLabel(status) {
  return status === 0 ? '待审核' : status === 1 ? '已通过' : '已驳回';
}

async function loadData() {
  const res = await getMerchantAuditList({
    current: pagination.current,
    size: pagination.size,
    auditStatus: filters.auditStatus !== undefined && filters.auditStatus !== null ? filters.auditStatus : undefined
  });
  tableData.value = res.data.records;
  pagination.total = Number(res.data.total);
}

function resetFilters() {
  filters.auditStatus = undefined;
  pagination.current = 1;
  loadData();
}

async function viewDetail(merchantId) {
  const res = await getMerchantDetail(merchantId);
  detailData.value = res.data;
  detailVisible.value = true;
}

function handleAudit(merchantId, status) {
  pendingAuditId.value = merchantId;
  pendingAuditStatus.value = status;
  auditRemark.value = '';
  auditDialogVisible.value = true;
}

async function confirmAudit() {
  await auditMerchant(pendingAuditId.value, pendingAuditStatus.value, auditRemark.value);
  ElMessage.success(pendingAuditStatus.value === 1 ? '审核通过' : '审核驳回');
  auditDialogVisible.value = false;
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