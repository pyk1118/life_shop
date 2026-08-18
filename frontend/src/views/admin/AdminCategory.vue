<template>
  <div class="admin-page">
    <h3 class="page-title">分类管理</h3>
    <div class="filter-bar">
      <el-button type="primary" @click="openDialog(null)">新增分类</el-button>
    </div>
    <el-table :data="tableData" border stripe style="width: 100%" class="large-table">
      <el-table-column prop="categoryId" label="ID" width="100" />
      <el-table-column prop="categoryName" label="分类名称" width="200" />
      <el-table-column prop="categoryDesc" label="描述" min-width="200" show-overflow-tooltip />
      <el-table-column prop="sort" label="排序" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
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

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑分类' : '新增分类'" width="450px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="分类名称" required>
          <el-input v-model="form.categoryName" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="分类描述">
          <el-input v-model="form.categoryDesc" type="textarea" :rows="2" placeholder="请输入分类描述" />
        </el-form-item>
        <el-form-item label="排序" required>
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.statusBool" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getCategoryList, saveCategory, deleteCategory } from '../../api/admin';

const pagination = reactive({ current: 1, size: 10, total: 0 });
const tableData = ref([]);
const dialogVisible = ref(false);
const editingId = ref(null);
const form = reactive({
  categoryName: '',
  categoryDesc: '',
  categoryIcon: '',
  sort: 0,
  statusBool: true
});

onMounted(() => loadData());

async function loadData() {
  const res = await getCategoryList({ current: pagination.current, size: pagination.size });
  tableData.value = res.data.records;
  pagination.total = Number(res.data.total);
}

function openDialog(row) {
  if (row) {
    editingId.value = row.categoryId;
    form.categoryName = row.categoryName;
    form.categoryDesc = row.categoryDesc;
    form.categoryIcon = row.categoryIcon;
    form.sort = row.sort;
    form.statusBool = row.status === 1;
  } else {
    editingId.value = null;
    form.categoryName = '';
    form.categoryDesc = '';
    form.categoryIcon = '';
    form.sort = 0;
    form.statusBool = true;
  }
  dialogVisible.value = true;
}

async function handleSave() {
  if (!form.categoryName) {
    ElMessage.warning('请输入分类名称');
    return;
  }
  await saveCategory({
    categoryId: editingId.value,
    categoryName: form.categoryName,
    categoryDesc: form.categoryDesc,
    categoryIcon: form.categoryIcon,
    sort: form.sort,
    status: form.statusBool ? 1 : 0
  });
  ElMessage.success('保存成功');
  dialogVisible.value = false;
  loadData();
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除分类「${row.categoryName}」吗？如果该分类下存在商品将无法删除。`, '提示', { type: 'warning' });
  await deleteCategory(row.categoryId);
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