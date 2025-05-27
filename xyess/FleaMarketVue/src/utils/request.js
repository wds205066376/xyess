import axios from 'axios';
import { ElMessage } from 'element-plus';
import { useUserStore } from '@/stores/user';

const service = axios.create({
    baseURL: '/api',
    timeout: 5000,
    withCredentials: true,
    headers: {
        'Content-Type': 'application/json'
    }
});

service.interceptors.request.use(
    config => {
        const userStore = useUserStore();
        
        // 确保所有请求都带上 withCredentials
        config.withCredentials = true;
        
        // 如果是文件上传，不修改 Content-Type
        if (config.data instanceof FormData) {
            return config;
        }

        // 如果已经设置了 Content-Type，就不要修改它
        if (config.headers && config.headers['Content-Type']) {
            return config;
        }

        // 默认的 POST 请求处理
        if (config.method === 'post') {
            config.headers['Content-Type'] = 'application/json';
        }

        return config;
    },
    error => {
        return Promise.reject(error);
    }
);

service.interceptors.response.use(
    response => {
        if (response.status === 200) {
            return response.data;
        }
        return Promise.reject(new Error(response.statusText || 'Error'));
    },
    error => {
        ElMessage.error(error.message || '请求失败');
        return Promise.reject(error);
    }
);

export default service;
