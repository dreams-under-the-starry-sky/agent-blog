import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({ timeout: 20000 })

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('blog_admin_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (res) => res.data,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('blog_admin_token')
      localStorage.removeItem('blog_admin_username')
      if (!window.location.pathname.includes('/login')) {
        window.location.href = `${import.meta.env.BASE_URL}login`
      }
    }
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

export function put<T>(url: string, data?: object) {
  return http.put(url, data) as Promise<T>
}

export function del<T>(url: string) {
  return http.delete(url) as Promise<T>
}

export async function upload(file: File) {
  const form = new FormData()
  form.append('file', file)
  const data = await http.post('/api/admin/upload', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 60000,
  }) as { url: string; thumbnailUrl: string }
  ElMessage.success('图片上传成功')
  return data
}

export async function deleteUpload(url?: string, thumbnailUrl?: string) {
  if (!url && !thumbnailUrl) return
  await http.post('/api/admin/upload/delete', { url, thumbnailUrl })
  ElMessage.success('图片已删除')
}

export default http
