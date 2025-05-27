import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
import api from '../api'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/index'
    },
    {
      path: '/index',
      component: () => import('../views/home/index.vue'),
      meta: { title: '二手物品交易平台' }
    },
    {
      path: '/search',
      component: () => import('../views/search/index.vue'),
      meta: { title: '闲置二手物品 | 二手物品交易平台' }
    },
    {
      path: '/me',
      component: () => import('../views/user/profile.vue'),
      meta: { title: '个人中心 | 二手物品交易平台' }
    },
    {
      path: '/message',
      name: 'message',
      component: () => import('../views/message/index.vue'),
      meta: { title: '消息 | 二手物品交易平台' }
    },
    {
      path: '/release',
      name: 'release',
      component: () => import('../views/release/index.vue'),
      meta: { title: '发布二手物品 | 二手物品交易平台' }
    },
    {
      path: '/details',
      component: () => import('../views/details/index.vue'),
      meta: { title: '二手物品详情 | 二手物品交易平台' }
    },
    {
      path: '/order',
      component: () => import('../views/order/index.vue'),
      meta: { title: '订单详情 | 二手物品交易平台' }
    },
    {
      path: '/login',
      component: () => import('../views/login/index.vue'),
      meta: { title: '登录 | 二手物品交易平台' }
    },
    {
      path: '/sign-in',
      component: () => import('../views/sign-in/index.vue'),
      meta: { title: '注册 | 二手物品交易平台' }
    },
    {
      path: '/login-admin',
      component: () => import('../views/admin/login.vue'),
      meta: { title: '后台登陆' }
    },
    {
      path: '/platform-admin',
      component: () => import('../views/admin/platform/index.vue'),
      meta: { title: '后台管理' }
    },
    {
      path: '/category/:id',
      name: 'CategoryView',
      component: () => import('@/views/categories/CategoryView.vue'),
      props: true
    },
    {
      path: '/categories',
      name: 'Categories',
      component: () => import('@/views/categories/index.vue')
    }
  ]
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  document.title = to.meta.title || '二手物品交易平台'

  const userStore = useUserStore()
  const userInfo = localStorage.getItem('userInfo')
  const adminInfo = localStorage.getItem('adminInfo')

  // 如果有用户信息但store未初始化，则恢复store
  if (userInfo && !userStore.isLogin) {
    userStore.setUserInfo(JSON.parse(userInfo))
  }

  // 需要登录的页面
  const requiresAuth = ['me', 'profile', 'message', 'release', 'order']
  // 需要管理员权限的页面
  const requiresAdmin = ['/platform-admin']

  if (requiresAdmin.includes(to.path)) {
    // 检查管理员登录状态
    if (!adminInfo) {
      next('/login-admin')
      return
    }
  } else if (requiresAuth.includes(to.name) && !userStore.isAuthenticated) {
    next('/login')
    return
  }
  
  next()
})

export default router
