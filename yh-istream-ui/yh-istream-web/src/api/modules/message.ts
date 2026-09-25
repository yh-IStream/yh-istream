import { get, put } from '@/api/request'
import type { ApiResponse, PageParams, PageResult } from '@/api/types'
import type { AxiosRequestConfig } from 'axios'

export interface SysMessage {
  id: string
  title: string
  content: string
  eventType: string
  entityType: string
  entityId: string
  senderId: string
  senderName: string
  createTime: string
  isRead: number
}

export interface SysMessageQuery extends Partial<PageParams> {
  eventType?: string
  unreadOnly?: boolean
}

export interface UnreadCount {
  count: number
}

/** 分页查询消息列表 */
export function getMessageList(params: SysMessageQuery, config?: AxiosRequestConfig) {
  return get<PageResult<SysMessage>>('/message/list', params, config)
}

/** 获取未读消息数 */
export function getUnreadCount(config?: AxiosRequestConfig) {
  return get<ApiResponse<UnreadCount>>('/message/unread-count', undefined, config)
}

/** 标记单条消息已读 */
export function markAsRead(messageId: string) {
  return put<ApiResponse<unknown>>(`/message/${messageId}/read`)
}

/** 全部标记已读 */
export function markAllAsRead() {
  return put<ApiResponse<unknown>>('/message/read-all')
}

export interface SseTicket {
  ticket: string
}

/** 获取 SSE 一次性连接凭证（用主 Token 换取短时效 ticket，避免主 Token 暴露在 URL 中） */
export function getSseTicket() {
  return get<ApiResponse<SseTicket>>('/sse/ticket')
}