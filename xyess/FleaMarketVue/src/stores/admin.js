import { defineStore } from 'pinia'

export const useAdminStore = defineStore('admin', {
  state: () => ({
    adminInfo: JSON.parse(localStorage.getItem('adminInfo')) || {},
    isLogin: !!localStorage.getItem('adminInfo')
  }),
  
  getters: {
    isAuthenticated() {
      return this.isLogin && !!this.adminInfo.id
    }
  },
  
  actions: {
    setAdminInfo(info) {
      this.adminInfo = info
      this.isLogin = true
      localStorage.setItem('adminInfo', JSON.stringify(info))
    },
    
    clearAdminInfo() {
      this.adminInfo = {}
      this.isLogin = false
      localStorage.removeItem('adminInfo')
    }
  }
}) 