<template>
  <div class="admin-container">
    <el-header class="admin-header">
      <div class="header">
        <router-link to="/platform-admin" class="app-name">
          <span class="logo-text">二手书交易平台</span>
        </router-link>
        <span class="app-title">
          <span class="title-text">
            欢迎使用后台管理系统，<b>{{ admin.nickname }}</b>
          </span>
        </span>
        <div class="app-logout">
          <el-button type="primary" @click="logout" class="logout-btn">
            <el-icon class="logout-icon"><SwitchButton /></el-icon>
            退出账号
          </el-button>
        </div>
      </div>
    </el-header>

    <el-container class="main-container">
      <div class="main-body">
        <el-aside width="220px">
          <el-menu
            :default-active="activeMenu"
            class="el-menu-vertical"
            @select="handleSelect"
            :collapse="false"
            background-color="#fff"
            text-color="#2c3e50"
            active-text-color="#409EFF"
          >
            <el-menu-item index="1">
              <el-icon><Goods /></el-icon>
              <span>交易物品管理</span>
            </el-menu-item>
            <el-menu-item index="2">
              <el-icon><List /></el-icon>
              <span>订单信息管理</span>
            </el-menu-item>
            <el-menu-item index="3">
              <el-icon><User /></el-icon>
              <span>用户信息管理</span>
            </el-menu-item>
            <el-menu-item index="4">
              <el-icon><Collection /></el-icon>
              <span>书籍分类管理</span>
            </el-menu-item>
          </el-menu>
        </el-aside>

        <el-main class="main-content">
          <div class="content-wrapper">
            <component :is="currentComponent" />
          </div>
        </el-main>
      </div>
    </el-container>
  </div>
</template>

<script setup>
import { ref, shallowRef, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAdminStore } from '@/stores/admin'
import { Goods, List, User, Collection, SwitchButton } from '@element-plus/icons-vue'
import IdleGoods from './components/IdleGoods.vue'
import Orders from './components/Orders.vue'
import Users from './components/Users.vue'
import BookCategories from './components/BookCategories.vue'
import api from '@/api'

const router = useRouter()
const adminStore = useAdminStore()

const admin = ref({
  nickname: adminStore.adminInfo.adminName || '管理员'
})

const activeMenu = ref('1')
const currentComponent = shallowRef(IdleGoods)

// 检查管理员登录状态
const checkAdminAuth = () => {
  if (!adminStore.isAuthenticated) {
    router.replace('/login-admin')
  }
}

onMounted(() => {
  checkAdminAuth()
})

// 菜单切换
const handleSelect = (index) => {
  activeMenu.value = index
  switch (index) {
    case '1':
      currentComponent.value = IdleGoods
      break
    case '2':
      currentComponent.value = Orders
      break
    case '3':
      currentComponent.value = Users
      break
    case '4':
      currentComponent.value = BookCategories
      break
  }
}

// 退出登录
const logout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      type: 'warning'
    })
    
    const res = await api.adminLoginOut()
    if (res.status_code === 1) {
      adminStore.clearAdminInfo()
      router.replace('/login-admin')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('退出失败，请稍后重试')
    }
  }
}
</script>

<style scoped>
.admin-container {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.admin-header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  position: fixed;
  width: 100%;
  z-index: 1000;
  height: 60px;
  padding: 0;
}

.header {
  display: flex;
  align-items: center;
  height: 100%;
  padding: 0 20px;
}

.app-name {
  display: flex;
  align-items: center;
  text-decoration: none;
  color: #2c3e50;
  font-weight: 600;
  font-size: 18px;
  gap: 10px;
}

.logo-img {
  width: 32px;
  height: 32px;
  user-drag: none;
}

.logo-text {
  background: linear-gradient(45deg, #409EFF, #36cfc9);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.app-title {
  flex: 1;
  display: flex;
  justify-content: center;
}

.title-text {
  color: #2c3e50;
  font-size: 16px;
}

.app-logout {
  display: flex;
  align-items: center;
}

.logout-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  background: linear-gradient(135deg, #409EFF 0%, #36cfc9 100%);
  border: none;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.3s;
}

.logout-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.logout-icon {
  font-size: 16px;
}

.main-container {
  padding-top: 60px;
  min-height: calc(100vh - 60px);
}

.main-body {
  display: flex;
  width: 100%;
}

.el-aside {
  background-color: #fff;
  border-right: 1px solid #ebeef5;
  height: calc(100vh - 60px);
  position: fixed;
  left: 0;
  top: 60px;
  z-index: 999;
}

.el-menu-vertical {
  border-right: none;
}

.el-menu-item {
  display: flex;
  align-items: center;
  height: 56px;
  padding: 0 20px;
  font-size: 14px;
  transition: all 0.3s;
}

.el-menu-item:hover {
  background: linear-gradient(90deg, rgba(64, 158, 255, 0.1) 0%, transparent 100%);
}

.el-menu-item.is-active {
  background: linear-gradient(90deg, rgba(64, 158, 255, 0.1) 0%, transparent 100%);
  border-right: 3px solid #409EFF;
  color: #409EFF;
}

.el-menu-item .el-icon {
  margin-right: 10px;
  font-size: 18px;
}

.main-content {
  margin-left: 220px;
  padding: 20px;
  background-color: #f5f7fa;
}

.content-wrapper {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  padding: 20px;
  min-height: calc(100vh - 120px);
}
</style> 