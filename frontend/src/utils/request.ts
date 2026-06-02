import axios from 'axios'
import router from '@/router'

// VITE_API_BASE_URL 用于生产环境指向后端完整地址（如 http://172.17.120.133:8080/api）
// 开发环境走 Vite proxy，不设置此变量即可
const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'

const request = axios.create({
  baseURL,
  timeout: 10000,
})

// 请求拦截器 — 自动携带 token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器 — 统一错误处理
request.interceptors.response.use(
  (response) => {
    const { code, message, data } = response.data
    if (code === 200) {
      return data
    }
    // token 过期或无效，跳转登录
    if (code === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      router.push('/login')
      message.error('登录已过期，请重新登录')
      return Promise.reject(new Error(message))
    }
    message.error(message || '请求失败')
    return Promise.reject(new Error(message))
  },
  (error) => {
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
