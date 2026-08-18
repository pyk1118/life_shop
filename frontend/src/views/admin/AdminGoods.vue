<template>
  <div class="admin-page">
    <h3 class="page-title">商品管理</h3>
    <div class="filter-bar">
      <el-input v-model="filters.keyword" placeholder="商品名称" clearable style="width: 180px" />
      <el-select v-model="filters.categoryId" placeholder="商品分类" clearable style="width: 150px">
        <el-option v-for="c in categoryOptions" :key="c.categoryId" :label="c.categoryName" :value="c.categoryId" />
      </el-select>
      <el-select v-model="filters.status" placeholder="上下架状态" clearable style="width: 130px">
        <el-option label="上架" :value="1" />
        <el-option label="下架" :value="0" />
      </el-select>
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button @click="resetFilters">重置</el-button>
    </div>
    <el-table :data="tableData" border stripe style="width: 100%" class="large-table">
      <el-table-column prop="goodsId" label="ID" width="80" />
      <el-table-column label="商品名称" width="255" show-overflow-tooltip>
        <template #default="{ row }">
          <router-link class="goods-link" :to="{ path: '/goods/' + row.goodsId, query: { from: '/admin/goods' } }">{{ row.goodsName }}</router-link>
        </template>
      </el-table-column>
      <el-table-column prop="categoryId" label="分类ID" width="120" />
      <el-table-column prop="shopId" label="店铺ID" width="120" />
      <el-table-column prop="salePrice" label="销售价" width="120" />
      <el-table-column prop="stock" label="库存" width="120" />
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="200">
        <template #default="{ row }"><span v-html="formatDateTimeHtml(row.createTime)"></span></template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="240">
        <template #default="{ row }">
          <el-button size="small" @click="viewDetail(row.goodsId)">详情</el-button>
          <el-button
            size="small"
            :type="row.status === 1 ? 'warning' : 'success'"
            @click="handleToggleStatus(row)"
          >
            {{ row.status === 1 ? '下架' : '上架' }}
          </el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="detailVisible" title="商品详情" width="600px">
      <template v-if="detailData">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="商品ID">{{ detailData.goodsId }}</el-descriptions-item>
          <el-descriptions-item label="商品名称">{{ detailData.goodsName }}</el-descriptions-item>
          <el-descriptions-item label="分类ID">{{ detailData.categoryId }}</el-descriptions-item>
          <el-descriptions-item label="商家ID">{{ detailData.merchantId }}</el-descriptions-item>
          <el-descriptions-item label="店铺ID">{{ detailData.shopId }}</el-descriptions-item>
          <el-descriptions-item label="市场价">¥{{ detailData.marketPrice }}</el-descriptions-item>
          <el-descriptions-item label="销售价">¥{{ detailData.salePrice }}</el-descriptions-item>
          <el-descriptions-item label="库存">{{ detailData.stock }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ detailData.status === 1 ? '上架' : '下架' }}</el-descriptions-item>
          <el-descriptions-item label="商品描述" :span="2">{{ detailData.goodsDesc || '无' }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getGoodsList, getGoodsDetail, toggleGoodsStatus, deleteGoods, getCategoryAll } from '../../api/admin';
import { formatDateTimeHtml } from '../../utils/datetime';

const filters = reactive({ keyword: '', categoryId: undefined, status: undefined });
const pagination = reactive({ current: 1, size: 9, total: 0 });
const tableData = ref([]);
const categoryOptions = ref([]);
const detailVisible = ref(false);
const detailData = ref(null);

onMounted(async () => {
  const catRes = await getCategoryAll();
  categoryOptions.value = catRes.data;
  loadData();
});

async function loadData() {
  const res = await getGoodsList({
    current: pagination.current,
    size: pagination.size,
    keyword: filters.keyword || undefined,
    categoryId: filters.categoryId || undefined,
    status: filters.status !== undefined && filters.status !== null ? filters.status : undefined
  });
  tableData.value = res.data.records;
  pagination.total = Number(res.data.total);
}

function resetFilters() {
  filters.keyword = '';
  filters.categoryId = undefined;
  filters.status = undefined;
  pagination.current = 1;
  loadData();
}

async function viewDetail(goodsId) {
  const res = await getGoodsDetail(goodsId);
  detailData.value = res.data;
  detailVisible.value = true;
}

async function handleToggleStatus(row) {
  const action = row.status === 1 ? '下架' : '上架';
  await ElMessageBox.confirm(`确定${action}商品「${row.goodsName}」吗？`, '提示', { type: 'warning' });
  await toggleGoodsStatus(row.goodsId);
  ElMessage.success(`已${action}该商品`);
  loadData();
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除商品「${row.goodsName}」吗？此操作不可恢复。`, '提示', { type: 'warning' });
  await deleteGoods(row.goodsId);
  ElMessage.success('删除成功');
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

.goods-link {
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