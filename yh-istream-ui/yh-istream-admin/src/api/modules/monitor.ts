import {get, del} from '@/api/request'
import type {PageParams, PageResult} from '@/api/modules/system'

// ==================== 操作日志 ====================
export function getOperLogList(params: PageParams) {
  return get<PageResult<Record<string, unknown>>>('/monitor/oper-log/list', params)
}

export function deleteOperLog(ids: string | string[]) {
  return del('/monitor/oper-log', Array.isArray(ids) ? ids : [ids])
}

export function clearOperLog() {
  return del('/monitor/oper-log/clear')
}

export function exportOperLog() {
  return get('/monitor/oper-log/export', undefined, { responseType: 'blob' })
}

// ==================== 登录日志 ====================
export function getLoginInfoList(params: PageParams) {
  return get<PageResult<Record<string, unknown>>>('/monitor/login-info/list', params)
}

export function deleteLoginInfo(ids: string | string[]) {
  return del('/monitor/login-info', Array.isArray(ids) ? ids : [ids])
}

export function clearLoginInfo() {
  return del('/monitor/login-info/clear')
}

export function exportLoginInfo() {
  return get('/monitor/login-info/export', undefined, { responseType: 'blob' })
}