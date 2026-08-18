<template>
  <div class="admin-page">
    <h3 class="page-title">开发账号</h3>

    <div class="section-card">
      <h4 class="section-title">修改密码</h4>
      <el-form :model="form" label-width="100px" class="center-form">
        <el-form-item label="原密码" required>
          <el-input v-model="form.oldPassword" type="password" show-password placeholder="请输入原密码" />
        </el-form-item>
        <el-form-item label="新密码" required>
          <el-input v-model="form.newPassword" type="password" show-password placeholder="请输入新密码（6-20位）" />
        </el-form-item>
        <el-form-item label="确认新密码" required>
          <el-input v-model="form.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
        <el-form-item>
          <button type="button" class="gradient-btn apply-btn" @click="handleChangePassword">确认修改</button>
        </el-form-item>
      </el-form>
    </div>

    <div class="section-card" style="margin-top: 24px">
      <div class="section-header">
        <h4 class="section-title">开发人员列表</h4>
        <el-button v-if="isSuperAdmin" type="primary" @click="openAddDialog">新增管理员</el-button>
      </div>
      <div v-if="!isSuperAdmin" class="permission-tip">
        <el-alert title="仅超级开发者(admin)可进行授权操作" type="warning" :closable="false" show-icon />
      </div>
      <el-table :data="adminList" border stripe style="width: 100%" class="large-table">
        <el-table-column prop="adminId" label="ID" width="120" />
        <el-table-column prop="adminAccount" label="开发者账号" width="200" />
        <el-table-column prop="realName" label="真实姓名" width="200" />
        <el-table-column prop="status" label="状态" width="200">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" min_width="300">
          <template #default="{ row }"><span v-html="formatDateTimeHtml(row.createTime)"></span></template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="300">
          <template #default="{ row }">
            <template v-if="isSuperAdmin && row.adminAccount !== 'admin'">
              <el-button size="small" :type="row.status === 1 ? 'danger' : 'success'" @click="handleToggleStatus(row)">
                {{ row.status === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-button size="small" type="warning" @click="handleResetPwd(row.adminId)">重置密码</el-button>
              <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
            </template>
            <el-tag v-else-if="row.adminAccount === 'admin'" type="warning" size="small">超级开发者</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          layout="total, prev, pager, next"
          @current-change="loadAdmins"
        />
      </div>
    </div>

    <el-dialog v-model="addDialogVisible" title="新增管理员" width="450px">
      <el-form :model="addForm" label-width="100px">
        <el-form-item label="管理员账号" required>
          <el-input v-model="addForm.adminAccount" placeholder="请输入管理员账号" />
        </el-form-item>
        <el-form-item label="真实姓名" required>
          <el-input v-model="addForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="初始密码">
          <el-input v-model="addForm.adminPassword" type="password" show-password placeholder="默认123456" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddAdmin">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { changeAdminPassword, getAdminList, addAdmin, toggleAdminStatus, resetAdminPassword, deleteAdmin } from '../../api/admin';
import { formatDateTimeHtml } from '../../utils/datetime';
import { useUserStore } from '../../store/user';

const userStore = useUserStore();
userStore.syncFromStorage();

const isSuperAdmin = computed(() => userStore.userInfo.account === 'admin');

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

const adminList = ref([]);
const pagination = reactive({ current: 1, size: 10, total: 0 });
const addDialogVisible = ref(false);
const addForm = reactive({ adminAccount: '', realName: '', adminPassword: '' });

async function handleChangePassword() {
  if (!form.oldPassword || !form.newPassword || !form.confirmPassword) {
    ElMessage.warning('请填写完整信息');
    return;
  }
  if (form.newPassword !== form.confirmPassword) {
    ElMessage.warning('两次输入新密码不一致');
    return;
  }
  if (form.newPassword.length < 6 || form.newPassword.length > 20) {
    ElMessage.warning('新密码长度应为6-20位');
    return;
  }
  await changeAdminPassword(form);
  ElMessage.success('密码修改成功');
  form.oldPassword = '';
  form.newPassword = '';
  form.confirmPassword = '';
}

async function loadAdmins() {
  const res = await getAdminList({ current: pagination.current, size: pagination.size });
  adminList.value = res.data.records;
  pagination.total = Number(res.data.total);
}

function openAddDialog() {
  addForm.adminAccount = '';
  addForm.realName = '';
  addForm.adminPassword = '';
  addDialogVisible.value = true;
}

async function handleAddAdmin() {
  if (!addForm.adminAccount) {
    ElMessage.warning('请输入管理员账号');
    return;
  }
  await addAdmin(addForm);
  ElMessage.success('管理员添加成功，默认密码123456');
  addDialogVisible.value = false;
  loadAdmins();
}

async function handleToggleStatus(row) {
  const action = row.status === 1 ? '禁用' : '启用';
  await ElMessageBox.confirm(`确定${action}管理员 ${row.adminAccount} 吗？`, '提示', { type: 'warning' });
  await toggleAdminStatus(row.adminId);
  ElMessage.success(`已${action}该管理员`);
  loadAdmins();
}

async function handleResetPwd(adminId) {
  await ElMessageBox.confirm('确定重置该管理员密码为123456吗？', '提示', { type: 'warning' });
  await resetAdminPassword(adminId);
  ElMessage.success('密码已重置为123456');
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除管理员 ${row.adminAccount} 吗？此操作不可恢复。`, '提示', { type: 'warning' });
  await deleteAdmin(row.adminId);
  ElMessage.success('删除成功');
  loadAdmins();
}

onMounted(loadAdmins);
</script>

<style scoped lang="less">
.admin-page {
  .page-title {
    font-size: 20px;
    font-weight: 600;
    color: #1d2129;
    margin: 0 0 16px;
  }
}

.section-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  .section-title {
    font-size: 18px;
    font-weight: 600;
    color: #1d2129;
    margin: 0 0 16px;
  }

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    .section-title {
      margin: 0;
    }
  }
}

.center-form {
  max-width: 460px;
  margin: 0 auto;
}

.permission-tip {
  margin-bottom: 16px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
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

.gradient-btn {
  background: linear-gradient(135deg, #165dff 0%, #722ed1 100%);
  color: white;
  border: none;
  padding: 10px 28px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    opacity: 0.9;
    transform: translateY(-1px);
  }
}

.apply-btn {
  height: 40px;
  padding: 0 32px;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 600;
}
</style>