<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-body">
        <div class="login-title">
          <b class="title-text" @click="$router.push('/index')">校园二手书交易平台</b>
        </div>

        <el-form
          ref="formRef"
          :model="loginForm"
          :rules="rules"
          @submit.prevent="handleLogin"
        >
          <el-form-item prop="accountNumber">
            <el-input
              v-model.trim="loginForm.accountNumber"
              placeholder="请输入手机号码"
              :prefix-icon="User"
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <el-form-item prop="userPassword">
            <el-input
              v-model.trim="loginForm.userPassword"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              @keyup.enter="handleLogin"
              show-password
            />
          </el-form-item>

          <div class="login-submit">
            <el-button
              type="primary"
              :loading="loading"
              @click="handleLogin"
            >
              {{ loading ? '登录中...' : '登录' }}
            </el-button>
            <el-button @click="$router.push('/sign-in')">注册</el-button>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import api from '@/api'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const loginForm = ref({
  accountNumber: '',
  userPassword: ''
})

// 表单验证规则
const rules = {
  accountNumber: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  userPassword: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ]
}

// 登录处理
const handleLogin = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    loading.value = true

    const res = await api.login({
      accountNumber: loginForm.value.accountNumber,
      userPassword: loginForm.value.userPassword
    })

    if (res.status_code === 1) {
      // 保存用户信息到 store
      userStore.setUserInfo(res.data)

      // 设置 cookie
      document.cookie = `shUserId=${res.data.id}; path=/`

      ElMessage.success('登录成功')
      router.push('/')
    } else {
      ElMessage.error(res.msg || '登录失败')
    }
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error('登录失败，请稍后重试')
  } finally {
    loading.value = false
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

.title-text {
  color: #2c3e50;
  display: inline-block;
  margin-bottom: 8px;
  font-size: 28px;
  background: linear-gradient(45deg, #409EFF, #36cfc9);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  font-weight: 600;
}

.subtitle {
  font-size: 16px;
  color: #666;
  margin-bottom: 24px;
}

.login-title {
  padding-bottom: 30px;
  text-align: center;
  font-weight: 600;
  color: #409EFF;
  cursor: pointer;
}

.login-submit {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 20px;
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
