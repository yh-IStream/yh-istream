import {get, post} from '@/api/request'

export interface TableInfo {
  tableName: string
  tableComment: string
  className: string
  createTime: string
}

export interface ColumnInfo {
  columnName: string
  columnComment: string
  columnType: string
  javaType: string
  javaField: string
  isPk: boolean
  isRequired: boolean
  isInsert: boolean
  isEdit: boolean
  isList: boolean
  isQuery: boolean
  htmlType: string
  dictType: string
}

export interface GenRequest {
  tableNames?: string[]
  author?: string
  moduleName?: string
  packageName?: string
  controllerPackage?: string
  genMigration?: boolean
}

export function getTableList() {
  return get<TableInfo[]>('/generator/tables')
}

export function getTableColumns(tableName: string) {
  return get<ColumnInfo[]>(`/generator/columns/${tableName}`)
}

export function previewCode(tableName: string, data: GenRequest) {
  return post<Record<string, string>>(`/generator/preview/${tableName}`, data)
}

export function batchPreviewCode(data: GenRequest) {
  return post<Record<string, Record<string, string>>>('/generator/batch-preview', data)
}

export function downloadCode(tableName: string, data: GenRequest) {
  return post(`/generator/download/${tableName}`, data, { responseType: 'blob' })
}

export function batchDownloadCode(data: GenRequest) {
  return post('/generator/batch-download', data, { responseType: 'blob' })
}