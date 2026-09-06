import { get, post } from '@/api/request'
import type { ApiResponse } from '@/api/types'
import type { AxiosRequestConfig } from 'axios'

export interface TableInfo {
  tableName: string
  tableComment: string
  className: string
  columns: ColumnInfo[]
  createTime: string
}

export interface ColumnInfo {
  columnName: string
  columnComment: string
  javaType: string
  javaField: string
  isPk: boolean
  isRequired: boolean
  isBaseField: boolean
  sqlType: string
  isIndexable: boolean
}

export interface GenRequest {
  tableNames?: string[]
  author?: string
  moduleName?: string
  packageName?: string
  controllerPackage?: string
  genMigration?: boolean
}

export function getTableList(config?: AxiosRequestConfig) {
  return get<ApiResponse<TableInfo[]>>('/generator/tables', undefined, config)
}

export function getTableColumns(tableName: string) {
  return get<ApiResponse<ColumnInfo[]>>(`/generator/columns/${tableName}`)
}

export function previewCode(tableName: string, data: GenRequest) {
  return post<ApiResponse<Record<string, string>>>(`/generator/preview/${tableName}`, data)
}

export function batchPreviewCode(data: GenRequest) {
  return post<ApiResponse<Record<string, Record<string, string>>>>('/generator/batch-preview', data)
}

export function downloadCode(tableName: string, data: GenRequest) {
  return post(`/generator/download/${tableName}`, data, { responseType: 'blob' })
}

export function batchDownloadCode(data: GenRequest) {
  return post('/generator/batch-download', data, { responseType: 'blob' })
}