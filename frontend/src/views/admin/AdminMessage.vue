<template>
  <div class="admin-page">
    <h3 class="page-title">留言管理</h3>
    <div class="filter-bar">
      <el-input v-model="filters.userAccount" placeholder="留言用户账号" clearable style="width: 180px" />
      <el-input v-model="filters.title" placeholder="留言标题" clearable style="width: 180px" />
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button @click="resetFilters">重置</el-button>
    </div>
    <el-table :data="tableData" border stripe style="width: 100%" class="large-table">
      <el-table-column prop="messageId" label="ID" width="70" />
      <el-table-column prop="userAccount" label="用户账号" width="130" />
      <el-table-column prop="messageTitle" label="标题" min-width="180" show-overflow-tooltip />
      <el-table-column prop="messageContent" label="内容" min-width="220" show-overflow-tooltip />
      <el-table-column prop="replyContent" label="回复" width="180" show-overflow-tooltip>
        <template #default="{ row }">
          <span v-if="row.replyContent">{{ row.replyContent }}</span>
          <el-tag v-else type="warning" size="small">未回复</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="留言时间" width="180">
        <template #default="{ row }"><span v-html="formatDateTimeHtml(row.createTime)"></span></template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="180">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="openReply(row)">回复</el-button>
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

    <el-dialog v-model="replyVisible" title="回复留言" width="500px">
      <template v-if="replyingRow">
        <el-descriptions :column="1" border size="small" style="margin-bottom: 16px">
          <el-descriptions-item label="用户">{{ replyingRow.userAccount }}</el-descriptions-item>
          <el-descriptions-item label="标题">{{ replyingRow.messageTitle }}</el-descriptions-item>
          <el-descriptions-item label="内容">{{ replyingRow.messageContent }}</el-descriptions-item>
        </el-descriptions>
      </template>
      <el-input v-model="replyContent" type="textarea" :rows="4" placeholder="请输入回复内容" />
      <template #footer>
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" @click="handleReply">提交回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getMessageList, replyMessage, deleteMessage } from '../../api/admin';
import { formatDateTimeHtml } from '../../utils/datetime';

const filters = reactive({ userAccount: '', title: '' });
const pagination = reactive({ current: 1, size: 10, total: 0 });
const tableData = ref([]);
const replyVisible = ref(false);
const replyingRow = ref(null);
const replyContent = ref('');

onMounted(() => loadData());

async function loadData() {
  const res = await getMessageList({
    current: pagination.current,
    size: pagination.size,
    userAccount: filters.userAccount || undefined,
    title: filters.title || undefined
  });
  tableData.value = res.data.records;
  pagination.total = Number(res.data.total);
}

function resetFilters() {
  filters.userAccount = '';
  filters.title = '';
  pagination.current = 1;
  loadData();
}

function openReply(row) {
  replyingRow.value = row;
  replyContent.value = row.replyContent || '';
  replyVisible.value = true;
}

async function handleReply() {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容');
    return;
  }
  await replyMessage(replyingRow.value.messageId, { replyContent: replyContent.value });
  ElMessage.success('回复成功');
  replyVisible.value = false;
  loadData();
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该留言吗？', '提示', { type: 'warning' });
  await deleteMessage(row.messageId);
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
</style>