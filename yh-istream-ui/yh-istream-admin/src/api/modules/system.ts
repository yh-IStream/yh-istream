import {get, post, put, del, upload} from '@/api/request'

export interface PageParams {
  pageNum: number
  pageSize: number
  [key: string]: unknown
}

export interface PageResult<T> {
  code: number
  data: {
    records: T[]
    total: number
    pages: number
    current: number
    size: number
  }
  message: string
}

// ==================== 用户管理 ====================
export interface SysUser {
  id: number
  username: string
  nickname: string
  deptId: number
  deptName: string
  email: string
  phone: string
  status: number
  createTime: string
}

export function getUserList(params: PageParams) {
  return get<PageResult<SysUser>>('/system/user/list', params)
}

export function getUserById(id: number) {
  return get(`/system/user/${id}`)
}

export function addUser(data: Record<string, unknown>) {
  return post('/system/user', data)
}

export function updateUser(data: Record<string, unknown>) {
  return put('/system/user', data)
}

export function deleteUser(id: number) {
  return del(`/system/user/${id}`)
}

export function batchDeleteUser(ids: number[]) {
  return del('/system/user/batch', ids)
}

export function resetUserPwd(userId: number, password: string) {
  return put('/system/user/reset-pwd', null, { params: { userId, password } })
}

export function changeUserStatus(userId: number, status: number) {
  return put('/system/user/change-status', null, { params: { userId, status } })
}

export function exportUser(params: Record<string, unknown>) {
  return get('/system/user/export', params, { responseType: 'blob' })
}

// ==================== 角色管理 ====================
export function getRoleList(params: PageParams) {
  return get<PageResult<Record<string, unknown>>>('/system/role/list', params)
}

export function getAllRoles() {
  return get('/system/role/all')
}

export function getRoleById(id: number) {
  return get(`/system/role/${id}`)
}

export function addRole(data: Record<string, unknown>) {
  return post('/system/role', data)
}

export function updateRole(data: Record<string, unknown>) {
  return put('/system/role', data)
}

export function deleteRole(id: number) {
  return del(`/system/role/${id}`)
}

export function getRoleMenuTree(roleId: number) {
  return get(`/system/role/menu-tree/${roleId}`)
}

export function assignRoleMenu(roleId: number, menuIds: number[]) {
  return put('/system/role/menu-assign', { roleId, menuIds })
}

// ==================== 菜单管理 ====================
export function getMenuTree() {
  return get('/system/menu/tree')
}

export function getMenuById(id: number) {
  return get(`/system/menu/${id}`)
}

export function addMenu(data: Record<string, unknown>) {
  return post('/system/menu', data)
}

export function updateMenu(data: Record<string, unknown>) {
  return put('/system/menu', data)
}

export function deleteMenu(id: number) {
  return del(`/system/menu/${id}`)
}

// ==================== 部门管理 ====================
export function getDeptTree() {
  return get('/system/dept/tree')
}

export function getDeptById(id: number) {
  return get(`/system/dept/${id}`)
}

export function addDept(data: Record<string, unknown>) {
  return post('/system/dept', data)
}

export function updateDept(data: Record<string, unknown>) {
  return put('/system/dept', data)
}

export function deleteDept(id: number) {
  return del(`/system/dept/${id}`)
}

// ==================== 字典管理 ====================
export function getDictTypeList(params: PageParams) {
  return get<PageResult<Record<string, unknown>>>('/system/dict-type/list', params)
}

export function getDictTypeById(id: number) {
  return get(`/system/dict-type/${id}`)
}

export function addDictType(data: Record<string, unknown>) {
  return post('/system/dict-type', data)
}

export function updateDictType(data: Record<string, unknown>) {
  return put('/system/dict-type', data)
}

export function deleteDictType(id: number) {
  return del(`/system/dict-type/${id}`)
}

export function getDictDataList(params: PageParams) {
  return get<PageResult<Record<string, unknown>>>('/system/dict-data/list', params)
}

export function getDictDataByType(dictType: string) {
  return get(`/system/dict-data/by-type/${dictType}`)
}

export function getDictDataMap() {
  return get('/system/dict-data/map')
}

export function addDictData(data: Record<string, unknown>) {
  return post('/system/dict-data', data)
}

export function updateDictData(data: Record<string, unknown>) {
  return put('/system/dict-data', data)
}

export function deleteDictData(id: number) {
  return del(`/system/dict-data/${id}`)
}

// ==================== 系统配置 ====================
export function getConfigList(params: PageParams) {
  return get<PageResult<Record<string, unknown>>>('/system/config/list', params)
}

export function getConfigByKey(configKey: string) {
  return get(`/system/config/key/${configKey}`)
}

export function addConfig(data: Record<string, unknown>) {
  return post('/system/config', data)
}

export function updateConfig(data: Record<string, unknown>) {
  return put('/system/config', data)
}

export function deleteConfig(id: number) {
  return del(`/system/config/${id}`)
}

// ==================== 文件管理 ====================
export function getFileList(params: PageParams) {
  return get<PageResult<Record<string, unknown>>>('/system/file/list', params)
}

export function uploadFile(formData: FormData, onProgress?: (percent: number) => void) {
  return upload('/system/file/upload', formData, onProgress)
}

export function downloadFile(id: number) {
  return get(`/system/file/download/${id}`, undefined, { responseType: 'blob' })
}

export function deleteFile(id: number) {
  return del(`/system/file/${id}`)
}

// ==================== 操作日志 ====================
export function getOperLogList(params: PageParams) {
  return get<PageResult<Record<string, unknown>>>('/monitor/oper-log/list', params)
}

export function deleteOperLog(id: number) {
  return del(`/monitor/oper-log/${id}`)
}

export function clearOperLog() {
  return del('/monitor/oper-log/clear')
}

// ==================== 登录日志 ====================
export function getLoginInfoList(params: PageParams) {
  return get<PageResult<Record<string, unknown>>>('/monitor/login-info/list', params)
}

// ==================== 代码生成器 ====================
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

export function deleteLoginInfo(id: number) {
  return del(`/monitor/login-info/${id}`)
}

export function clearLoginInfo() {
  return del('/monitor/login-info/clear')
}