<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-body">
        <div class="login-title">
          <h1 class="title-text">二手书后台管理系统</h1>
        </div>

        <el-form ref="formRef" :model="loginForm" :rules="rules">
          <el-form-item prop="accountNumber">
            <el-input 
              v-model="loginForm.accountNumber"
              :prefix-icon="User"
              placeholder="请输入管理员账号"
            />
          </el-form-item>

          <el-form-item prop="adminPassword">
            <el-input
              v-model="loginForm.adminPassword"
              :prefix-icon="Lock"
              type="password"
              placeholder="请输入管理员密码"
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <div class="login-submit">
            <el-button type="primary" @click="handleLogin">登录</el-button>
          </div>
        </el-form>
      </div>
      <div class="login-decoration"></div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useAdminStore } from '@/stores/admin'
import api from '@/api'

const router = useRouter()
const adminStore = useAdminStore()
const formRef = ref(null)

const loginForm = ref({
  accountNumber: '',
  adminPassword: ''
})

const rules = {
  accountNumber: [
    { required: true, message: '请输入管理员账号', trigger: 'blur' }
  ],
  adminPassword: [
    { required: true, message: '请输入管理员密码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    const res = await api.adminLogin({
      accountNumber: loginForm.value.accountNumber,
      adminPassword: loginForm.value.adminPassword
    })
    
    if (res.status_code === 1) {
      adminStore.setAdminInfo(res.data)
      router.replace('/platform-admin')
    } else {
      ElMessage.error('登录失败，账号或密码错误！')
    }
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error('登录失败，请稍后重试')
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: #f5f5f5;
}

.login-card {
  background: linear-gradient(
    135deg,
    rgba(255, 255, 255, 0.95) 0%,
    rgba(255, 255, 255, 0.85) 100%
  );
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 24px;
  box-shadow: 
    0 8px 20px rgba(0, 0, 0, 0.05),
    0 12px 28px rgba(0, 0, 0, 0.03);
  overflow: hidden;
  position: relative;
}

.login-body {
  padding: 30px;
  width: 300px;
  position: relative;
  z-index: 1;
}

.title-text {
  color: #2c3e50;
  display: inline-block;
  margin-bottom: 20px;
  font-size: 28px;
  background: linear-gradient(45deg, #409EFF, #36cfc9);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  font-weight: 600;
}

.login-title {
  padding-bottom: 30px;
  text-align: center;
}

.login-submit {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.login-decoration {
  position: absolute;
  top: -50%;
  right: -10%;
  width: 600px;
  height: 600px;
  background: radial-gradient(
    circle,
    rgba(64, 158, 255, 0.1) 0%,
    rgba(54, 207, 201, 0.05) 50%,
    transparent 70%
  );
  border-radius: 50%;
  animation: float 8s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(5deg);
  }
}

:deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(4px);
  border-radius: 8px;
  border: 1px solid rgba(200, 200, 200, 0.3);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.02);
  transition: all 0.3s;
}

:deep(.el-input__wrapper:hover) {
  border-color: #409EFF;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

:deep(.el-input__wrapper.is-focus) {
  border-color: #409EFF;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
}

.login-submit .el-button {
  padding: 12px 30px;
  border-radius: 8px;
  transition: all 0.3s;
  width: 100%;
}

.login-submit .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.login-submit .el-button--primary {
  background: linear-gradient(135deg, #409EFF 0%, #36cfc9 100%);
  border: none;
}
</style> 