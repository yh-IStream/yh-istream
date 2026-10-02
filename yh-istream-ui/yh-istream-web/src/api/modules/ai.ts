import request from '../request'

export interface ActionSuggestion {
  id: number
  alertId: number
  entityType: string
  entityId: number
  action: string
  params: Record<string, unknown>
  confidence: number
  conclusion: string
  suggestTime: string
  status: number
  confirmedBy: number | null
  confirmedTime: string | null
  tenantId: number | null
}

export function getPendingSuggestions() {
  return request.get<ActionSuggestion[]>('/ai/suggestion/pending')
}

export function getSuggestionDetail(id: number) {
  return request.get<ActionSuggestion>(`/ai/suggestion/${id}`)
}

export function confirmSuggestion(id: number) {
  return request.post(`/ai/suggestion/${id}/confirm`)
}

export function rejectSuggestion(id: number) {
  return request.post(`/ai/suggestion/${id}/reject`)
}

export function getMcpTools() {
  return request.get<ToolDefinition[]>('/ai/suggestion/tools')
}

export interface ToolDefinition {
  beanName: string
  methodName: string
  description: string
}

export interface DemoTriggerResult {
  action: string
  conclusion: string
  confidence: number
  entityType: string
  entityId: number
  timestamp: string
}

export function triggerDemoDetection() {
  return request.post<DemoTriggerResult>('/ai/demo/trigger')
}

export function triggerDemoDetectionBatch() {
  return request.post<DemoTriggerResult[]>('/ai/demo/trigger-batch')
}

export function guardOrder(params: {
  orderId?: number
  orderAmount?: number
  userId?: number
  creditScore?: number
  deliveryAddress?: string
  ordersInLastHour?: number
}) {
  return request.post('/ai/guard/order', null, { params })
}

export function guardConfig(params: {
  configId?: number
  configKey?: string
  changeDetail?: string
  operator?: string
  recentChangesInMinutes?: number
}) {
  return request.post('/ai/guard/config', null, { params })
}

export function guardExport(params: {
  userId?: number
  dataType?: string
  recordCount?: number
  dailyAvgRecords?: number
  containsPii?: boolean
}) {
  return request.post('/ai/guard/export', null, { params })
}

export function guardLogin(params: {
  userId?: number
  loginCity?: string
  usualCity?: string
  recentLoginCities?: number
  secondsSinceLastLogin?: number
}) {
  return request.post('/ai/guard/login', null, { params })
}

export interface AiAlertRecipient {
  id: number
  userId: number
  userName: string
  tenantId: number
  enabled: number
  createBy: number | null
  createTime: string
  updateBy: number | null
  updateTime: string
}

export function getAlertRecipients() {
  return request.get<AiAlertRecipient[]>('/ai/recipient/list')
}

export function addAlertRecipient(recipient: { userId: number; userName?: string; enabled?: number }) {
  return request.post<AiAlertRecipient>('/ai/recipient/add', recipient)
}

export function updateAlertRecipient(id: number, data: { enabled?: number; userName?: string }) {
  return request.put<AiAlertRecipient>(`/ai/recipient/${id}`, data)
}

export function deleteAlertRecipient(id: number) {
  return request.delete(`/ai/recipient/${id}`)
}