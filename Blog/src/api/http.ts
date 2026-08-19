import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({ timeout: 15000 })

http.interceptors.response.use(
  (res) => res.data,
  (err) => {
    ElMessage.error(err.response?.data?.message || err.message || '网络错误')
    return Promise.reject(err)
  },
)

export function get<T>(url: string, params?: object) {
  return http.get(url, { params }) as Promise<T>
}

export function post<T>(url: string, data?: object) {
  return http.post(url, data) as Promise<T>
}
