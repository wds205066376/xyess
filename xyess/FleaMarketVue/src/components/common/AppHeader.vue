<template>
  <div class="header">
    <div class="header-container">
      <!-- 左侧导航 -->
      <div class="nav-left">
        <router-link to="/" class="nav-item">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </router-link>
        <router-link :to="{ name: 'Categories' }" class="nav-item">
          <el-icon><Grid /></el-icon>
          <span>分类</span>
        </router-link>
        <router-link to="/release" class="nav-item publish-btn">
          <el-icon><Plus /></el-icon>
          <span>发布书籍</span>
        </router-link>
      </div>

      <!-- 右侧用户区域 -->
      <div class="nav-right">
        <template v-if="!isLogin">
          <router-link to="/login" class="nav-item">登录</router-link>
          <router-link to="/sign-in" class="nav-item">注册</router-link>
        </template>
        <template v-else>
          <div class="message-icon" @click="handleMessageClick">
            <el-badge :value="messageList.length" :hidden="messageList.length === 0">
              <el-icon><Bell /></el-icon>
            </el-badge>
          </div>
          <el-dropdown trigger="click">
            <div class="user-info">
              <el-avatar
                :size="32"
                :src="avatarUrl"
                @error="handleAvatarError"
              />
              <span class="nickname">{{ userStore.userInfo.nickname }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/me')">
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <div class="admin-link" @click="openAdminPanel">
          <el-icon><Setting /></el-icon>
          <span>后台管理</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, watch, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Bell, Setting, HomeFilled, Plus, Grid } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import api from '@/api'

const router = useRouter()
const userStore = useUserStore()
const isLogin = computed(() => userStore.isLogin)

// 添加时间戳引用，用于强制刷新头像
const avatarTimestamp = ref(Date.now())

// 从后端获取最新的用户信息和头像
const refreshUserInfo = async () => {
  if (!userStore.userInfo.id) return

  try {
    const res = await api.getUserInfo(userStore.userInfo.id)
    if (res.status_code === 1 && res.data) {
      userStore.setUserInfo(res.data)
      // 更新时间戳，强制刷新头像
      avatarTimestamp.value = Date.now()
    }
  } catch (error) {
    // 获取用户信息失败
  }
}

// 处理头像URL
const getAvatarUrl = (avatar) => {
  const userId = userStore.userInfo.id
  if (!userId) return ''

  const baseURL = import.meta.env.VITE_API_URL || 'http://localhost:9321'
  const timestamp = Date.now()

  // 直接使用用户ID获取头像
  return `${baseURL}/file/avatar/${userId}?t=${timestamp}`
}

// 计算头像URL
const avatarUrl = computed(() => getAvatarUrl(userStore.userInfo.avatar))

// 在组件挂载时刷新用户信息
onMounted(() => {
  refreshUserInfo()
})

// 监听路由变化
watch(() => router.currentRoute.value.path, () => {
  refreshUserInfo()
})

// 监听用户信息变化
watch(() => userStore.userInfo.avatar, () => {
  avatarTimestamp.value = Date.now()
}, { immediate: true })

// 添加头像加载错误处理
const handleAvatarError = () => {
  // 头像加载失败，重试
  avatarTimestamp.value = Date.now()
}

// 退出登录处理
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要退出登录吗?',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        customClass: 'custom-message-box',
        distinguishCancelAndClose: true,
        confirmButtonClass: 'custom-confirm-button',
        cancelButtonClass: 'custom-cancel-button',
        closeOnClickModal: false,
        showClose: false,
        center: true,
        roundButton: true,
      }
    )

    // 调用退出接口
    const res = await api.logout()

    // 无论后端返回什么状态,都清除本地状态并跳转
    userStore.clearUserInfo()
    sessionStorage.clear()

    // 清除所有cookie
    document.cookie.split(";").forEach(function(c) {
      document.cookie = c.replace(/^ +/, "").replace(/=.*/, "=;expires=" + new Date().toUTCString() + ";path=/")
    })

    if (res.status_code === 1) {
      ElMessage.success({
        message: '已安全退出',
        type: 'success',
        duration: 2000
      })
    }

    // 延迟跳转,确保状态清除完成
    setTimeout(() => {
      router.push('/login')
    }, 100)

  } catch (error) {
    // 用户取消操作
    if (error === 'cancel') return

    // 其他错误也清除本地状态
    userStore.clearUserInfo()
    sessionStorage.clear()
    document.cookie.split(";").forEach(function(c) {
      document.cookie = c.replace(/^ +/, "").replace(/=.*/, "=;expires=" + new Date().toUTCString() + ";path=/")
    })

    ElMessage.error({
      message: '已退出登录',
      type: 'success',
      duration: 2000
    })

    // 延迟跳转
    setTimeout(() => {
      router.push('/login')
    }, 100)
  }
}

// 跳转后台管理登录页面
const openAdminPanel = () => {
  window.open('/login-admin', '_blank')
}

const messageVisible = ref(false)
const messageList = ref([])
const messageLoading = ref(false)

// 获取所有留言
const getMessageList = async () => {
  if (!userStore.userInfo.id) return

  messageLoading.value = true
  try {
    const res = await api.getAllMyMessage(userStore.userInfo.id)
    if (res.status_code === 1) {
      messageList.value = res.data.map(msg => ({
        ...msg,
        createTime: formatTime(msg.createTime),
        type: msg.toMessage ? 'reply' : 'comment', // 回复或评论
        fromUser: msg.fromU?.nickname || '未知用户',
        idleTitle: msg.idle?.title || '未知商品'
      }))
    }
  } catch (error) {
    // 获取留言列表失败
  } finally {
    messageLoading.value = false
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  if (diff < 3600000) { // 1小时内
    return `${Math.floor(diff / 60000)}分钟前`
  } else if (diff < 86400000) { // 24小时内
    return `${Math.floor(diff / 3600000)}小时前`
  } else if (diff < 604800000) { // 7天内
    return `${Math.floor(diff / 86400000)}天前`
  } else {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
  }
}

// 点击消息图标
const handleMessageClick = () => {
  router.push('/message')
}

// 点击消息项
const handleMessageItemClick = (msg) => {
  // 跳转到相应的商品详情页
  router.push({
    path: '/details',
    query: { id: msg.idleId }
  })
  messageVisible.value = false
}

onMounted(() => {
  // 初始加载
  getMessageList()
})
</script>

<style>
/* 注意：这里不使用 scoped，以便样式可以全局生效 */
.custom-message-box {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95), rgba(255, 255, 255, 0.85)) !important;
  backdrop-filter: blur(10px) !important;
  border: 1px solid rgba(255, 255, 255, 0.8) !important;
  border-radius: 16px !important;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.05), 0 12px 28px rgba(0, 0, 0, 0.03) !important;
  padding: 24px !important;
  width: 380px !important;
}

.custom-message-box .el-message-box__header {
  padding: 0 !important;
  margin-bottom: 16px !important;
}

.custom-message-box .el-message-box__title {
  font-size: 20px !important;
  color: #2c3e50 !important;
  font-weight: 600 !important;
  line-height: 1.5 !important;
  text-align: center !important;
}

.custom-message-box .el-message-box__content {
  padding: 24px 0 !important;
  margin: 0 !important;
  font-size: 16px !important;
  color: #606266 !important;
  text-align: center !important;
}

.custom-message-box .el-message-box__btns {
  padding: 8px 0 0 0 !important;
  text-align: center !important;
}

.custom-message-box .custom-confirm-button {
  background: linear-gradient(135deg, #409EFF, #36cfc9) !important;
  border: none !important;
  padding: 12px 28px !important;
  font-size: 14px !important;
  font-weight: 500 !important;
  color: white !important;
  border-radius: 8px !important;
  margin-left: 16px !important;
  transition: all 0.3s ease !important;
}

.custom-message-box .custom-confirm-button:hover {
  transform: translateY(-1px) !important;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3) !important;
  background: linear-gradient(135deg, #66b1ff, #40d9d3) !important;
}

.custom-message-box .custom-cancel-button {
  background: transparent !important;
  border: 1px solid #dcdfe6 !important;
  padding: 12px 28px !important;
  font-size: 14px !important;
  color: #606266 !important;
  border-radius: 8px !important;
  transition: all 0.3s ease !important;
}

.custom-message-box .custom-cancel-button:hover {
  color: #409EFF !important;
  border-color: #409EFF !important;
  background: rgba(64, 158, 255, 0.1) !important;
  transform: translateY(-1px) !important;
}

/* 添加动画效果 */
.custom-message-box {
  animation: messageBoxIn 0.3s ease-out;
}

@keyframes messageBoxIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>

<style scoped>
/* 顶部导航栏样式 */
.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(235, 235, 235, 0.6);
  z-index: 1000;
}

.header-container {
  max-width: 1200px;
  height: 100%;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.nav-left, .nav-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.nav-item {
  color: #666;
  font-size: 14px;
  text-decoration: none;
  padding: 6px 16px;
  border-radius: 20px;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 6px;
  position: relative;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.9);
}

.nav-item:hover {
  color: #409EFF;
  background: rgba(64, 158, 255, 0.1);
  transform: translateY(-1px);
}

.nav-item .el-icon {
  font-size: 16px;
}

.publish-btn {
  background: linear-gradient(45deg, #36cfc9, #409EFF);
  color: white;
  font-weight: 500;
  box-shadow: 0 2px 6px rgba(64, 158, 255, 0.2);
}

.publish-btn:hover {
  color: white;
  background: linear-gradient(45deg, #40d9d3, #66b1ff);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.publish-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 6px rgba(64, 158, 255, 0.2);
}

.nav-item.router-link-active:not(.publish-btn) {
  color: #409EFF;
  background-color: rgba(64, 158, 255, 0.1);
  font-weight: 500;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 12px;
  border-radius: 20px;
  transition: all 0.3s;
  background: rgba(255, 255, 255, 0.9);
}

.user-info:hover {
  background: rgba(64, 158, 255, 0.1);
  transform: translateY(-1px);
}

.nickname {
  color: #666;
  font-size: 14px;
  font-weight: 500;
}

.admin-link {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s;
  background: linear-gradient(135deg, #409EFF 0%, #36cfc9 100%);
  color: white;
  font-size: 14px;
  border: none;
  box-shadow: 0 2px 6px rgba(64, 158, 255, 0.2);
}

.admin-link:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  background: linear-gradient(135deg, #66b1ff 0%, #40d9d3 100%);
}

.message-icon {
  cursor: pointer;
  margin: 0 20px;
}

.message-list {
  padding: 10px;
}

.message-item {
  padding: 15px;
  border-bottom: 1px solid #eee;
  cursor: pointer;
  transition: all 0.3s;
}

.message-item:hover {
  background-color: #f5f7fa;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.from-user {
  font-weight: bold;
  color: #409EFF;
}

.time {
  font-size: 12px;
  color: #999;
}

.message-content {
  margin-bottom: 8px;
}

.idle-title {
  color: #666;
  margin-bottom: 4px;
}

.content {
  color: #333;
}

.reply-info {
  font-size: 13px;
  color: #666;
  background-color: #f5f7fa;
  padding: 8px;
  border-radius: 4px;
}

.no-message {
  text-align: center;
  padding: 40px;
  color: #999;
}

:deep(.el-dropdown-menu) {
  border: none;
  border-radius: 12px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
  padding: 8px;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(10px);
}

:deep(.el-dropdown-menu__item) {
  padding: 8px 16px;
  border-radius: 8px;
  margin: 2px 0;
  font-size: 14px;
  transition: all 0.3s;
}

:deep(.el-dropdown-menu__item:hover) {
  background: rgba(64, 158, 255, 0.1);
  color: #409EFF;
}

:deep(.el-dropdown-menu__item.divided) {
  border-top: 1px solid rgba(235, 235, 235, 0.6);
  margin-top: 8px;
  padding-top: 8px;
}
</style>
