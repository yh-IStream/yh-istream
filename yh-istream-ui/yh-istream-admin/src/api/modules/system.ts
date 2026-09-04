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
  msg: string
}

// ==================== 用户管理 ====================
export interface SysUser {
  id: string
  username: string
  nickname: string
  deptId: string
  deptName: string
  email: string
  phone: string
  gender: number
  status: number
  roleIds: string[]
  remark: string
  createTime: string
}

export function getUserList(params: PageParams) {
  return get<PageResult<SysUser>>('/system/user/list', params)
}

export function getUserById(id: string) {
  return get(`/system/user/${id}`)
}

export function addUser(data: Record<string, unknown>) {
  return post('/system/user', data)
}

export function updateUser(data: Record<string, unknown>) {
  return put('/system/user', data)
}

export function assignUserRoles(userId: string, roleIds: string[]) {
  return put(`/system/user/${userId}/roles`, { roleIds })
}

export function deleteUser(ids: string | string[]) {
  return del('/system/user', Array.isArray(ids) ? ids : [ids])
}

export function resetUserPwd(userId: string, password: string) {
  return put('/system/user/reset-pwd', null, { params: { userId, password } })
}

export function changeUserStatus(userId: string, status: number) {
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

export function getRoleById(id: string) {
  return get(`/system/role/${id}`)
}

export function addRole(data: Record<string, unknown>) {
  return post<{ data: string }>('/system/role', data)
}

export function updateRole(data: Record<string, unknown>) {
  return put('/system/role', data)
}

export function deleteRole(ids: string | string[]) {
  return del('/system/role', Array.isArray(ids) ? ids : [ids])
}

export function changeRoleStatus(roleId: string, status: number) {
  return put('/system/role/change-status', null, { params: { roleId, status } })
}

export function getRoleMenuTree(roleId: string) {
  return get(`/system/role/menu-tree/${roleId}`)
}

export function assignRoleMenu(roleId: string, menuIds: string[]) {
  return put('/system/role/menu-assign', { roleId, menuIds })
}

export function getRoleUsers(roleId: string) {
  return get(`/system/role/${roleId}/users`)
}

export function assignRoleUsers(roleId: string, userIds: string[]) {
  return put(`/system/role/${roleId}/users`, userIds)
}

export function getRoleDeptIds(roleId: string) {
  return get<{ data: string[] }>(`/system/role/${roleId}/depts`)
}

export function assignRoleDept(roleId: string, deptIds: string[]) {
  return put('/system/role/dept-assign', { roleId, deptIds })
}

export function exportRole() {
  return get('/system/role/export', undefined, { responseType: 'blob' })
}

// ==================== 菜单管理 ====================
export function getMenuTree() {
  return get('/system/menu/tree')
}

export function getUserMenuTree() {
  return get('/system/menu/user-tree')
}

export function getMenuById(id: string) {
  return get(`/system/menu/${id}`)
}

export function addMenu(data: Record<string, unknown>) {
  return post('/system/menu', data)
}

export function updateMenu(data: Record<string, unknown>) {
  return put('/system/menu', data)
}

export function deleteMenu(ids: string | string[]) {
  return del('/system/menu', Array.isArray(ids) ? ids : [ids])
}

// ==================== 部门管理 ====================
export function getDeptTree() {
  return get<{ data: any[] }>('/system/dept/tree')
}

export function getDeptById(id: string) {
  return get(`/system/dept/${id}`)
}

export function addDept(data: Record<string, unknown>) {
  return post('/system/dept', data)
}

export function updateDept(data: Record<string, unknown>) {
  return put('/system/dept', data)
}

export function deleteDept(ids: string | string[]) {
  return del('/system/dept', Array.isArray(ids) ? ids : [ids])
}

// ==================== 字典管理 ====================
export function getDictTypeList(params: PageParams) {
  return get<PageResult<Record<string, unknown>>>('/system/dict-type/list', params)
}

export function getDictTypeById(id: string) {
  return get(`/system/dict-type/${id}`)
}

export function addDictType(data: Record<string, unknown>) {
  return post('/system/dict-type', data)
}

export function updateDictType(data: Record<string, unknown>) {
  return put('/system/dict-type', data)
}

export function deleteDictType(ids: string | string[]) {
  return del('/system/dict-type', Array.isArray(ids) ? ids : [ids])
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

export function deleteDictData(ids: string | string[]) {
  return del('/system/dict-data', Array.isArray(ids) ? ids : [ids])
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

export function deleteConfig(ids: string | string[]) {
  return del('/system/config', Array.isArray(ids) ? ids : [ids])
}

// ==================== 文件管理 ====================
export function getFileList(params: PageParams) {
  return get<PageResult<Record<string, unknown>>>('/system/file/list', params)
}

export function uploadFile(formData: FormData, onProgress?: (percent: number) => void) {
  return upload('/system/file/upload', formData, onProgress)
}

export function downloadFile(id: string) {
  return get(`/system/file/download/${id}`, undefined, { responseType: 'blob' })
}

export function deleteFile(ids: string | string[]) {
  return del('/system/file', Array.isArray(ids) ? ids : [ids])
}