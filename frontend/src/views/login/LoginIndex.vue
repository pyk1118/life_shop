<template>
  <div class="login-page">
    <div class="login-card animate-fade-in-up">
      <div class="card-header">
        <div class="logo-icon">
          <img :src="logoSrc" alt="Logo" class="logo-img" @error="(e) => { e.target.style.display = 'none'; e.target.nextElementSibling.style.display = 'inline'; }" />
          <span style="display: none; font-size: 40px;">🛒</span>
        </div>
        <h2 class="gradient-text">优品汇</h2>
      </div>
      <el-tabs v-model="activeRole" stretch class="login-tabs">
        <el-tab-pane label="游客访问" name="GUEST">
          <div class="guest-area">
            <p class="guest-tip">无需登录，直接浏览平台商品</p>
            <button class="gradient-btn full-btn" @click="goGuest">直接进入首页</button>
          </div>
        </el-tab-pane>
        <el-tab-pane label="用户登录" name="USER">
          <el-form :model="form" :rules="rules" ref="formRef" label-width="0" size="large">
            <el-form-item prop="account">
              <el-input v-model="form.account" placeholder="请输入用户账号" prefix-icon="User" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" prefix-icon="Lock" />
            </el-form-item>
            <button type="button" class="gradient-btn full-btn" @click="submitLogin">登 录</button>
            <div class="link-row">
              <el-link type="primary" @click="$router.push('/register')">没有账号？去注册</el-link>
            </div>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="商家登录" name="MERCHANT">
          <el-form :model="form" :rules="rules" ref="formRefMerchant" label-width="0" size="large">
            <el-form-item prop="account">
              <el-input v-model="form.account" placeholder="请输入商家账号" prefix-icon="User" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" prefix-icon="Lock" />
            </el-form-item>
            <button type="button" class="gradient-btn full-btn" @click="submitLogin">登 录</button>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="开发者登录" name="ADMIN">
          <el-form :model="form" :rules="rules" ref="formRefAdmin" label-width="0" size="large">
            <el-form-item prop="account">
              <el-input v-model="form.account" placeholder="请输入开发者账号" prefix-icon="User" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" prefix-icon="Lock" />
            </el-form-item>
            <button type="button" class="gradient-btn full-btn" @click="submitLogin">登 录</button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import { login } from '../../api/home';
import { useUserStore } from '../../store/user';
import logoSrc from '../../assets/LifeShop_logo.png';

const router = useRouter();
const userStore = useUserStore();
const activeRole = ref('GUEST');
const formRef = ref();
const formRefMerchant = ref();
const formRefAdmin = ref();
const form = reactive({ account: '', password: '' });
const rules = {
  account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
};

const roleRouteMap = { USER: '/home', MERCHANT: '/merchant', ADMIN: '/admin' };

function goGuest() {
  userStore.clearAuth();
  router.push('/home');
}

async function submitLogin() {
  if (activeRole.value === 'GUEST') { goGuest(); return; }
  const formInstance = activeRole.value === 'USER' ? formRef.value : activeRole.value === 'MERCHANT' ? formRefMerchant.value : formRefAdmin.value;
  await formInstance.validate();
  const res = await login({ role: activeRole.value, account: form.account, password: form.password });
  userStore.setAuth(res.data);
  ElMessage.success('登录成功');
  router.push(roleRouteMap[activeRole.value] || '/home');
}
</script>

<style scoped lang="less">
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #165dff 0%, #722ed1 50%, #5b2c9e 100%);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    width: 600px;
    height: 600px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.05);
    top: -200px;
    right: -100px;
  }

  &::after {
    content: '';
    position: absolute;
    width: 400px;
    height: 400px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.03);
    bottom: -150px;
    left: -50px;
  }
}

.login-card {
  width: 440px;
  min-height: 480px;
  background: rgba(255, 255, 255, 0.97);
  border-radius: 16px;
  padding: 36px 32px 28px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  position: relative;
  z-index: 1;
  backdrop-filter: blur(10px);
}

.card-header {
  text-align: center;
  margin-bottom: 24px;

  .logo-icon {
    font-size: 40px;
    margin-bottom: 8px;

    .logo-img {
      height: 48px;
      vertical-align: middle;
    }
  }

  h2 {
    font-size: 30px;
    margin: 0;
    font-family: "Segoe UI", "Microsoft YaHei", "PingFang SC", sans-serif;
  }
}

.login-tabs {
  :deep(.el-tabs__nav-wrap::after) {
    height: 1px;
    background: var(--theme-border);
  }

  :deep(.el-tabs__active-bar) {
    background: var(--theme-primary-gradient);
    height: 3px;
    border-radius: 2px;
  }

  :deep(.el-tabs__item.is-active) {
    color: var(--theme-primary);
    font-weight: 600;
  }

  :deep(.el-tabs__item:hover) {
    color: var(--theme-secondary);
  }

  // 固定所有Tab内容区域的高度，防止切换时跳动
  :deep(.el-tab-pane) {
    min-height: 260px; // 统一高度，保证按钮对齐
  }

  // 修复自动填充时的淡蓝色背景
  :deep(.el-input__wrapper) {
    background-color: transparent !important;
    border: 1px solid var(--theme-border);

    &:focus-within {
      box-shadow: none !important;
      border-color: var(--theme-primary);
    }

    // 覆盖浏览器自动填充的默认样式
    &:-webkit-autofill,
    &:-webkit-autofill:hover,
    &:-webkit-autofill:focus,
    &:-webkit-autofill:active {
      background-color: transparent !important;
      box-shadow: none !important;
      transition: none;
      border-color: var(--theme-primary);
    }
  }
}

.guest-area {
  text-align: center;
  display: flex;
  flex-direction: column;
  padding-top: 20px;

  .guest-tip {
    color: var(--theme-text-muted);
    margin-bottom: 72px;
    font-size: 14px;
    line-height: 1.5;
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

.link-row {
  margin-top: 16px;
  text-align: center;
}
</style>
