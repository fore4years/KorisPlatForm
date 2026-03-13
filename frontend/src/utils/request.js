import axios from 'axios'
import { ElMessage } from 'element-plus'

const service = axios.create({
  baseURL: 'http://localhost:8081/api', // Adjust if needed
  timeout: 5000
})

// Request interceptor
service.interceptors.request.use(
  config => {
    // Add token if available
    const token = localStorage.getItem('token') || sessionStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }

    // Add X-User-ID if available
    const userStr = localStorage.getItem('user')
    if (userStr) {
        try {
            const user = JSON.parse(userStr)
            if (user.userid) {
                config.headers['X-User-ID'] = user.userid
            }
        } catch (e) {
            // ignore
        }
    }

    return config
  },
  error => {
    console.log(error)
    return Promise.reject(error)
  }
)

// Response interceptor
service.interceptors.response.use(
  response => {
    const res = response.data
    
    // Check for standard Result format
    if (res.code !== undefined) {
        if (res.code === 200) {
            return res.data
        } else {
            ElMessage({
                message: res.msg || 'Error',
                type: 'error',
                duration: 5 * 1000
            })
            return Promise.reject(new Error(res.msg || 'Error'))
        }
    }
    
    // Fallback for non-standard responses (if any)
    return res
  },
  error => {
    console.log('err' + error)
    let message = error.message
    if (error.response && error.response.data && error.response.data.message) {
      message = error.response.data.message
    } else if (error.response && error.response.status === 401) {
        message = 'Unauthorized'
    } else if (message === 'Network Error') {
        message = '网络错误，请检查后端服务是否启动'
    }
    
    ElMessage({
      message: message,
      type: 'error',
      duration: 5 * 1000
    })
    return Promise.reject(error)
  }
)

export default service
