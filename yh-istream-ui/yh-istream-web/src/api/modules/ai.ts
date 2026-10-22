import { get, post, put, del } from '@/api/request'
import type { ApiResponse } from '@/api/types'

export interface ActionSuggestion {
  id: string
  alertId: string
  entityType: string
  entityId: string
  action: string
  params: Record<string, unknown>
  confidence: number
  conclusion: string
  suggestTime: string
  status: number
  confirmedBy: string | null
  confirmedTime: string | null
  tenantId: string | null
}

export function getPendingSuggestions() {
  return get<ApiResponse<ActionSuggestion[]>>('/ai/suggestion/pending')
}

export function getSuggestionDetail(id: string) {
  return get<ApiResponse<ActionSuggestion>>(`/ai/suggestion/${id}`)
}

export function confirmSuggestion(id: string) {
  return post(`/ai/suggestion/${id}/confirm`)
}

export function rejectSuggestion(id: string) {
  return post(`/ai/suggestion/${id}/reject`)
}

export function getMcpTools() {
  return get<ApiResponse<ToolDefinition[]>>('/ai/suggestion/tools')
}

export interface ToolDefinition {
  beanName: string
  methodName: string
  description: string
}

export function triggerDemoDetection() {
  return post<ApiResponse<DemoTriggerResult>>('/ai/demo/trigger')
}

export function triggerDemoDetectionBatch() {
  return post<ApiResponse<DemoTriggerResult[]>>('/ai/demo/trigger-batch')
}

export interface GuardResult {
  action: string
  conclusion: string
  confidence: number
  entityType: string
  entityId: number
  timestamp: string
}

export interface DemoTriggerResult extends GuardResult {}

export function guardOrder(params: {
  orderId?: number
  orderAmount?: number
  userId?: number
  creditScore?: number
  deliveryAddress?: string
  ordersInLastHour?: number
}) {
  return post('/ai/guard/order', null, { params })
}

export function guardConfig(params: {
  configId?: number
  configKey?: string
  changeDetail?: string
  operator?: string
  recentChangesInMinutes?: number
}) {
  return post('/ai/guard/config', null, { params })
}

export function guardExport(params: {
  userId?: number
  dataType?: string
  recordCount?: number
  dailyAvgRecords?: number
  containsPii?: boolean
}) {
  return post('/ai/guard/export', null, { params })
}

export function guardLogin(params: {
  userId?: number
  loginCity?: string
  usualCity?: string
  recentLoginCities?: number
  secondsSinceLastLogin?: number
}) {
  return post('/ai/guard/login', null, { params })
}

export interface AiAlertRecipient {
  id: string
  userId: string
  userName: string
  tenantId: string
  enabled: boolean
  createBy: string | null
  createTime: string
  updateBy: string | null
  updateTime: string
}

export function getAlertRecipients() {
  return get<ApiResponse<AiAlertRecipient[]>>('/ai/recipient/list')
}

export function addAlertRecipient(recipient: { userId: string; userName?: string; enabled?: boolean }) {
  return post<ApiResponse<AiAlertRecipient>>('/ai/recipient/add', recipient)
}

export function updateAlertRecipient(id: string, data: { enabled?: boolean; userName?: string }) {
  return put<ApiResponse<AiAlertRecipient>>(`/ai/recipient/${id}`, data)
}

export function deleteAlertRecipient(id: string) {
  return del(`/ai/recipient/${id}`)
}