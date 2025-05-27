<template>
  <div class="sign-in-container">
    <div class="sign-in-card">
      <div class="sign-in-body">
        <div class="sign-in-title">
          <h1 class="title-text">注册账号</h1>
        </div>

        <el-form ref="formRef" :model="registerForm" :rules="rules" label-width="100px">
          <el-form-item label="头像"  prop="avatar">
            <el-upload
              class="avatar-uploader"
              action="http://localhost:9321/file/upload"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :before-upload="beforeAvatarUpload"
              accept="image/*"
            >
              <img v-if="registerForm.avatar" :src="getAvatarUrl(registerForm.avatar)" class="avatar-preview">
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
          </el-form-item>
          <el-form-item label="昵称" prop="nickname">
            <el-input
              v-model="registerForm.nickname"
              placeholder="请输入昵称"
            />
          </el-form-item>
          <el-form-item label="手机号" prop="accountNumber">
            <el-input
              v-model="registerForm.accountNumber"
              placeholder="请输入手机号码"
            />
          </el-form-item>

          <el-form-item label="密码" prop="userPassword">
            <el-input
              v-model="registerForm.userPassword"
              type="password"
              placeholder="请输入密码"
            />
          </el-form-item>

          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
            />
          </el-form-item>



          <div class="sign-in-submit">
            <el-button type="primary" @click="handleRegister">注册</el-button>
            <el-button @click="$router.push('/login')">返回登录</el-button>
          </div>
        </el-form>
      </div>
      <div class="sign-in-decoration"></div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import api from '@/api'
import { useGlobalStore } from '@/stores'

const router = useRouter()
const formRef = ref(null)
const store = useGlobalStore()

const registerForm = ref({
  accountNumber: '',
  userPassword: '',
  confirmPassword: '',
  nickname: '',
  avatar: '',
})

const getAvatarUrl = (avatar) => {
  if (!avatar) return ''
  return store.baseApi + avatar
}

const handleAvatarSuccess = (response) => {
  if (response.status_code === 1) {
    registerForm.value.avatar = response.data
  } else {
    ElMessage.error('头像上传失败')
  }
}

const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.value.userPassword) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const rules = {
  accountNumber: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  userPassword: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validatePass, trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    const res = await api.register({
      accountNumber: registerForm.value.accountNumber,
      userPassword: registerForm.value.userPassword,
      nickname: registerForm.value.nickname,
      avatar: registerForm.value.avatar || '/image?imageName=cat.jpg',
    })

    if (res.status_code === 1) {
      ElMessage.success('注册成功')
      router.push('/login')
    } else {
      ElMessage.error(res.msg || '注册失败')
    }
  } catch (error) {
    console.error('注册失败:', error)
    ElMessage.error('注册失败,请稍后重试')
  }
}


const toIndex = () => {
  router.push('/index')
}
</script>

<style scoped>
.sign-in-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: #f5f5f5;
}

.sign-in-card {
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

.sign-in-body {
  padding: 40px;
  width: 400px;
  position: relative;
  z-index: 1;
}

.logo-img {
  width: 40px;
  height: 40px;
  margin: 5px 5px -5px 0;
  user-drag: none;
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

.sign-in-title {
  padding-bottom: 30px;
  text-align: center;
}

.sign-in-submit {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 20px;
}

.avatar-uploader {
  display: flex;
  justify-content: center;
  margin-bottom: 10px;
}

.avatar-uploader-icon {
  font-size: 24px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  line-height: 120px;
  text-align: center;
  border: 2px dashed #d9d9d9;
  border-radius: 12px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
}

.avatar-uploader-icon:hover {
  border-color: #409EFF;
  color: #409EFF;
}

.avatar-preview {
  width: 120px;
  height: 120px;
  border-radius: 12px;
  object-fit: cover;
}

.sign-in-decoration {
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

.sign-in-submit .el-button {
  padding: 12px 30px;
  border-radius: 8px;
  transition: all 0.3s;
}

.sign-in-submit .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.sign-in-submit .el-button--primary {
  background: linear-gradient(135deg, #409EFF 0%, #36cfc9 100%);
  border: none;
}

:deep(.el-form-item__label) {
  color: #606266;
  font-weight: 500;
}
</style>
