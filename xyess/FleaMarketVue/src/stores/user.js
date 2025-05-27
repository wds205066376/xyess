import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: JSON.parse(localStorage.getItem('userInfo')) || {},
    isLogin: !!localStorage.getItem('userInfo')
  }),
  
  getters: {
    isAuthenticated() {
      return this.isLogin && !!this.userInfo.id && this.getCookie('shUserId') === this.userInfo.id.toString()
    }
  },
  
  actions: {
    setUserInfo(info) {
      this.userInfo = info
      this.isLogin = true
      localStorage.setItem('userInfo', JSON.stringify(info))
    },
    
    clearUserInfo() {
      this.userInfo = {}
      this.isLogin = false
      localStorage.removeItem('userInfo')
      document.cookie = 'shUserId=; path=/; expires=Thu, 01 Jan 1970 00:00:01 GMT;'
    },

    getCookie(name) {
      const value = `; ${document.cookie}`
      const parts = value.split(`; ${name}=`)
      if (parts.length === 2) return parts.pop().split(';').shift()
      return null
    }
  }
}) 