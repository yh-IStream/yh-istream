import { get } from '@/api/request'
import type { ApiResponse } from '@/api/types'
import type { AxiosRequestConfig } from 'axios'

export interface DashboardStats {
  userCount: number
  roleCount: number
  todayOperCount: number
  onlineCount: number
}

export function getDashboardStats(config?: AxiosRequestConfig) {
  return get<ApiResponse<DashboardStats>>('/dashboard/stats', undefined, config)
}