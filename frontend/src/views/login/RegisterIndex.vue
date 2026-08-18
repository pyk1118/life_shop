<template>
  <div class="register-page">
    <div class="register-card animate-fade-in-up">
      <div class="card-header">
        <h2 class="gradient-text">用户注册</h2>
        <p class="sub-title">创建账号，开启购物之旅</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0" size="large">
        <el-form-item prop="userAccount">
          <el-input v-model="form.userAccount" placeholder="请输入登录账号" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="userPassword">
          <el-input v-model="form.userPassword" type="password" show-password placeholder="请输入登录密码（6-20位）" prefix-icon="Lock" />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" show-password placeholder="请再次输入密码" prefix-icon="Lock" />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" prefix-icon="Phone" />
        </el-form-item>
        <button type="button" class="gradient-btn full-btn" @click="submitRegister">注 册</button>
      </el-form>
      <div class="bottom-link">
        <el-link type="primary" @click="$router.push('/login')">已有账号，去登录</el-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import { register } from '../../api/home';

const router = useRouter();
const formRef = ref();
const form = reactive({ userAccount: '', userPassword: '', confirmPassword: '', phone: '' });
const rules = {
  userAccount: [{ required: true, message: '请输入登录账号', trigger: 'blur' }],
  userPassword: [{ required: true, message: '请输入登录密码', trigger: 'blur' }, { min: 6, max: 20, message: '密码长度6-20位', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请输入确认密码', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }, { pattern: /^1\d{10}$/, message: '手机号格式错误', trigger: 'blur' }]
};

async function submitRegister() {
  await formRef.value.validate();
  if (form.userPassword !== form.confirmPassword) { ElMessage.error('两次输入密码不一致'); return; }
  await register(form);
  ElMessage.success('注册成功，请登录');
  router.push('/login');
}
</script>

<style scoped lang="less">
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #165dff 0%, #722ed1 50%, #5b2c9e 100%);
  position: relative;
  overflow: hidden;
}

.register-card {
  width: 440px;
  background: rgba(255, 255, 255, 0.97);
  border-radius: 16px;
  padding: 36px 32px 28px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  position: relative;
  z-index: 1;
}

.card-header {
  text-align: center;
  margin-bottom: 28px;

  h2 {
    font-size: 24px;
    margin: 0 0 8px;
  }

  .sub-title {
    color: var(--theme-text-muted);
    font-size: 14px;
    margin: 0;
  }
}

.full-btn {
  width: 100%;
  height: 44px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  letter-spacing: 4px;
}

.bottom-link {
  margin-top: 20px;
  text-align: center;
}
</style>
