import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import '@/assets/css/index.css'
import '@/styles/pagination.css'
import App from './App.vue'
import router from './router'
import api from './api'
import axios from 'axios'

// 配置axios默认值
axios.defaults.withCredentials = true
axios.defaults.headers.common['Content-Type'] = 'application/json'

const app = createApp(App)
app.config.globalProperties.$api = api
app.config.globalProperties.$store = {
  state: {
    baseApi: 'http://localhost:9321'
  }
}

const pinia = createPinia()
app.use(pinia)
app.use(router)
app.use(ElementPlus)

app.mount('#app')
