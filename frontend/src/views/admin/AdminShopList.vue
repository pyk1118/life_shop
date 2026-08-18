<template>
  <div class="admin-page">
    <h3 class="page-title">店铺列表</h3>
    <div class="filter-bar">
      <el-input v-model="filters.shopName" placeholder="店铺名称" clearable style="width: 180px" />
      <el-select v-model="filters.shopStatus" placeholder="状态" clearable style="width: 130px">
        <el-option label="营业中" :value="1" />
        <el-option label="已关闭" :value="0" />
      </el-select>
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button @click="resetFilters">重置</el-button>
    </div>
    <el-table :data="tableData" border stripe style="width: 100%" class="large-table">
      <el-table-column prop="shopId" label="ID" width="80" />
      <el-table-column prop="shopName" label="店铺名称" width="160" />
      <el-table-column prop="merchantId" label="商家ID" width="100" />
      <el-table-column prop="shopDesc" label="描述" width="431" show-overflow-tooltip />
      <el-table-column prop="businessHours" label="营业时间" width="160" />
      <el-table-column prop="shopStatus" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="row.shopStatus === 1 ? 'success' : 'danger'">
            {{ row.shopStatus === 1 ? '营业中' : '已关闭' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="180">
        <template #default="{ row }"><span v-html="formatDateTimeHtml(row.createTime)"></span></template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="160">
        <template #default="{ row }">
          <el-button
            size="small"
            :type="row.shopStatus === 1 ? 'danger' : 'success'"
            @click="handleToggleStatus(row)"
          >
            {{ row.shopStatus === 1 ? '关闭店铺' : '恢复营业' }}
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getShopList, toggleShopStatus } from '../../api/admin';
import { formatDateTimeHtml } from '../../utils/datetime';

const filters = reactive({ shopName: '', shopStatus: undefined });
const pagination = reactive({ current: 1, size: 10, total: 0 });
const tableData = ref([]);

onMounted(() => loadData());

async function loadData() {
  const res = await getShopList({
    current: pagination.current,
    size: pagination.size,
    shopName: filters.shopName || undefined,
    shopStatus: filters.shopStatus !== undefined && filters.shopStatus !== null ? filters.shopStatus : undefined
  });
  tableData.value = res.data.records;
  pagination.total = Number(res.data.total);
}

function resetFilters() {
  filters.shopName = '';
  filters.shopStatus = undefined;
  pagination.current = 1;
  loadData();
}

async function handleToggleStatus(row) {
  const action = row.shopStatus === 1 ? '关闭' : '恢复';
  await ElMessageBox.confirm(`确定${action}店铺 ${row.shopName} 吗？`, '提示', { type: 'warning' });
  await toggleShopStatus(row.shopId);
  ElMessage.success(`已${action}该店铺`);
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