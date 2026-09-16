import axios, { type AxiosInstance, type AxiosRequestConfig, type InternalAxiosRequestConfig } from 'axios'
import { useAuthStore } from '@/stores/auth'

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api/v1'

const instance: AxiosInstance = axios.create({
  baseURL: BASE_URL,
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' },
})

function handleUnauthorized(url?: string) {
  if (!url?.endsWith('/auth/logout')) {
    const authStore = useAuthStore()
    authStore.logout()
  }
  return Promise.reject(new Error('登录已过期'))
}

// 请求拦截器
instance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers.Authorization = authStore.token
    }
    return config
  },
  (error) => Promise.reject(error),
)

// 响应拦截器
instance.interceptors.response.use(
  (response) => {
    if (response.config.responseType === 'blob') {
      return response
    }

    const { code, msg } = response.data ?? {}

    if (code === 200) {
      return response.data
    }

    if (code === 401) {
      return handleUnauthorized(response.config.url)
    }

    if (code >= 50001 && code <= 50099) {
      return Promise.reject(new Error(msg || '租户访问异常'))
    }

    return Promise.reject(new Error(msg || '请求失败'))
  },
  (error) => {
    if (error.response?.status === 401) {
      return handleUnauthorized(error.config?.url)
    }
    if (error.response?.status === 403) {
      window.location.href = '/error/403'
      return Promise.reject(new Error('权限不足'))
    }
    return Promise.reject(error)
  },
)

export function get<T = unknown>(url: string, params?: Record<string, unknown>, config?: AxiosRequestConfig) {
  return instance.get<T, T>(url, { params, ...config })
}

export function post<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig) {
  return instance.post<T, T>(url, data, config)
}

export function put<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig) {
  return instance.put<T, T>(url, data, config)
}

export function del<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig) {
  return instance.delete<T, T>(url, { data, ...config })
}

export function createAbortController() {
  return new AbortController()
}

export function isAbortError(e: unknown): boolean {
  return e instanceof Error && e.name === 'AbortError'
}

export function upload<T = unknown>(url: string, formData: FormData, onProgress?: (percent: number) => void) {
  return instance.post<T, T>(url, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    onUploadProgress: (e) => {
      if (e.total && onProgress) {
        onProgress(Math.round((e.loaded * 100) / e.total))
      }
    },
  })
}

export default instance