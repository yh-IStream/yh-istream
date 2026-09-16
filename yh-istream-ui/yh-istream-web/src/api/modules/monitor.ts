import { get, del } from '@/api/request'
import type { ApiResponse, PageParams, PageResult } from '@/api/types'
import type { AxiosRequestConfig } from 'axios'

// ==================== 操作日志 ====================
export interface SysOperLog {
  id: string
  title: string
  businessType: number
  method: string
  requestMethod: string
  operUrl: string
  operIp: string
  operLocation: string
  operParam: string
  jsonResult: string
  errorMsg: string
  status: number
  costTime: string
  operName: string
  operTime: string
}

export function getOperLogList(params: PageParams, config?: AxiosRequestConfig) {
  return get<PageResult<SysOperLog>>('/monitor/oper-log/list', params, config)
}

export function deleteOperLog(ids: string | string[]) {
  return del<ApiResponse<unknown>>('/monitor/oper-log', Array.isArray(ids) ? ids : [ids])
}

export function clearOperLog() {
  return del<ApiResponse<unknown>>('/monitor/oper-log/clear')
}

export function exportOperLog() {
  return get('/monitor/oper-log/export', undefined, { responseType: 'blob' })
}

// ==================== 登录日志 ====================
export interface SysLoginInfo {
  id: string
  username: string
  ipAddress: string
  loginLocation: string
  browser: string
  os: string
  status: number
  msg: string
  loginTime: string
}

export function getLoginInfoList(params: PageParams, config?: AxiosRequestConfig) {
  return get<PageResult<SysLoginInfo>>('/monitor/login-info/list', params, config)
}

export function deleteLoginInfo(ids: string | string[]) {
  return del<ApiResponse<unknown>>('/monitor/login-info', Array.isArray(ids) ? ids : [ids])
}

export function clearLoginInfo() {
  return del<ApiResponse<unknown>>('/monitor/login-info/clear')
}

export function exportLoginInfo() {
  return get('/monitor/login-info/export', undefined, { responseType: 'blob' })
}