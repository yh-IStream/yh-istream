import { get, post, put, del, upload } from '@/api/request'
import type { ApiResponse, PageParams, PageResult } from '@/api/types'
import type { AxiosRequestConfig } from 'axios'

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
  avatar: string
  status: number
  loginIp: string
  loginDate: string
  loginCount: number
  pwdResetTime: string
  roleIds: string[]
  roleNames: string[]
  remark: string
  createTime: string
}

export interface SysUserSave {
  id?: string
  username?: string
  nickname?: string
  password?: string
  deptId?: string
  email?: string
  phone?: string
  gender?: number
  status?: number
  roleIds?: string[]
  remark?: string
}

export function getUserList(params: PageParams, config?: AxiosRequestConfig) {
  return get<PageResult<SysUser>>('/system/user/list', params, config)
}

export function getUserById(id: string) {
  return get<ApiResponse<SysUser & { roleIds: string[] }>>(`/system/user/${id}`)
}

export function addUser(data: SysUserSave) {
  return post<ApiResponse<unknown>>('/system/user', data)
}

export function updateUser(data: SysUserSave) {
  return put<ApiResponse<unknown>>('/system/user', data)
}

export function assignUserRoles(userId: string, roleIds: string[]) {
  return put<ApiResponse<unknown>>(`/system/user/${userId}/roles`, roleIds)
}

export function deleteUser(ids: string | string[]) {
  return del<ApiResponse<unknown>>('/system/user', Array.isArray(ids) ? ids : [ids])
}

export function resetUserPwd(userId: string, password: string) {
  return put<ApiResponse<unknown>>(`/system/user/${userId}/reset-pwd`, { password })
}

export function changeUserStatus(userId: string, status: number) {
  return put<ApiResponse<unknown>>('/system/user/change-status', null, { params: { userId, status } })
}

export function exportUser(params?: Partial<PageParams>) {
  return get('/system/user/export', params, { responseType: 'blob' })
}

// ==================== 角色管理 ====================
export interface SysRole {
  id: string
  roleName: string
  roleKey: string
  roleSort: number
  dataScope: number
  status: number
  remark: string
  createTime: string
}

export interface SysRoleSave {
  id?: string
  roleName?: string
  roleKey?: string
  roleSort?: number
  dataScope?: number
  status?: number
  remark?: string
}

export function getRoleList(params: PageParams, config?: AxiosRequestConfig) {
  return get<PageResult<SysRole>>('/system/role/list', params, config)
}

export function getAllRoles() {
  return get<ApiResponse<SysRole[]>>('/system/role/all')
}

export function addRole(data: SysRoleSave) {
  return post<ApiResponse<string>>('/system/role', data)
}

export function updateRole(data: SysRoleSave) {
  return put<ApiResponse<unknown>>('/system/role', data)
}

export function deleteRole(ids: string | string[]) {
  return del<ApiResponse<unknown>>('/system/role', Array.isArray(ids) ? ids : [ids])
}

export function changeRoleStatus(roleId: string, status: number) {
  return put<ApiResponse<unknown>>('/system/role/change-status', null, { params: { roleId, status } })
}

export interface RoleMenuTreeResult {
  menus: SysMenu[]
  checkedKeys: string[]
}

export function getRoleMenuTree(roleId: string) {
  return get<ApiResponse<RoleMenuTreeResult>>(`/system/role/menu-tree/${roleId}`)
}

export function assignRoleMenu(roleId: string, menuIds: string[]) {
  return put<ApiResponse<unknown>>(`/system/role/${roleId}/menu-assign`, menuIds)
}

export function getRoleUsers(roleId: string) {
  return get<ApiResponse<SysUser[]>>(`/system/role/${roleId}/users`)
}

export function assignRoleUsers(roleId: string, userIds: string[]) {
  return put<ApiResponse<unknown>>(`/system/role/${roleId}/users`, userIds)
}

export function getRoleDeptIds(roleId: string) {
  return get<ApiResponse<string[]>>(`/system/role/${roleId}/depts`)
}

export function assignRoleDept(roleId: string, deptIds: string[]) {
  return put<ApiResponse<unknown>>(`/system/role/${roleId}/dept-assign`, deptIds)
}

export function exportRole() {
  return get('/system/role/export', undefined, { responseType: 'blob' })
}

// ==================== 菜单管理 ====================
export interface SysMenu {
  id: string
  parentId: string
  menuName: string
  menuType: string
  path: string
  component: string
  query: string
  permission: string
  icon: string
  orderNum: number
  visible: number
  status: number
  remark: string
  createTime: string
  children?: SysMenu[]
}

export interface SysMenuSave {
  id?: string
  parentId?: string
  menuName?: string
  menuType?: string
  path?: string
  component?: string
  query?: string
  permission?: string
  icon?: string
  orderNum?: number
  visible?: number
  status?: number
  remark?: string
}

export function getMenuTree(config?: AxiosRequestConfig) {
  return get<ApiResponse<SysMenu[]>>('/system/menu/tree', undefined, config)
}

export function addMenu(data: SysMenuSave) {
  return post<ApiResponse<unknown>>('/system/menu', data)
}

export function updateMenu(data: SysMenuSave) {
  return put<ApiResponse<unknown>>('/system/menu', data)
}

export function deleteMenu(ids: string | string[]) {
  return del<ApiResponse<unknown>>('/system/menu', Array.isArray(ids) ? ids : [ids])
}

// ==================== 部门管理 ====================
export interface SysDept {
  id: string
  parentId: string
  ancestors: string
  deptName: string
  orderNum: number
  leader: string
  phone: string
  email: string
  status: number
  remark: string
  createTime: string
  children?: SysDept[]
}

export interface SysDeptSave {
  id?: string
  parentId?: string
  deptName?: string
  orderNum?: number
  leader?: string
  phone?: string
  email?: string
  status?: number
  remark?: string
}

export function getDeptTree(config?: AxiosRequestConfig) {
  return get<ApiResponse<SysDept[]>>('/system/dept/tree', undefined, config)
}

export function addDept(data: SysDeptSave) {
  return post<ApiResponse<unknown>>('/system/dept', data)
}

export function updateDept(data: SysDeptSave) {
  return put<ApiResponse<unknown>>('/system/dept', data)
}

export function deleteDept(ids: string | string[]) {
  return del<ApiResponse<unknown>>('/system/dept', Array.isArray(ids) ? ids : [ids])
}

// ==================== 字典管理 ====================
export interface SysDictType {
  id: string
  dictName: string
  dictType: string
  status: number
  remark: string
  createTime: string
}

export interface SysDictTypeSave {
  id?: string
  dictName?: string
  dictType?: string
  status?: number
  remark?: string
}

export interface SysDictData {
  id: string
  dictType: string
  dictLabel: string
  dictValue: string
  cssClass: string
  listClass: string
  isDefault: number
  orderNum: number
  status: number
  remark: string
  createTime: string
}

export interface SysDictDataSave {
  id?: string
  dictType?: string
  dictLabel?: string
  dictValue?: string
  cssClass?: string
  listClass?: string
  isDefault?: number
  orderNum?: number
  status?: number
  remark?: string
}

export function getDictTypeList(params: PageParams, config?: AxiosRequestConfig) {
  return get<PageResult<SysDictType>>('/system/dict-type/list', params, config)
}

export function addDictType(data: SysDictTypeSave) {
  return post<ApiResponse<unknown>>('/system/dict-type', data)
}

export function updateDictType(data: SysDictTypeSave) {
  return put<ApiResponse<unknown>>('/system/dict-type', data)
}

export function deleteDictType(ids: string | string[]) {
  return del<ApiResponse<unknown>>('/system/dict-type', Array.isArray(ids) ? ids : [ids])
}

export function getDictDataList(params: PageParams, config?: AxiosRequestConfig) {
  return get<PageResult<SysDictData>>('/system/dict-data/list', params, config)
}

export function getDictDataByType(dictType: string) {
  return get<ApiResponse<SysDictData[]>>(`/system/dict-data/by-type/${dictType}`)
}

export function addDictData(data: SysDictDataSave) {
  return post<ApiResponse<unknown>>('/system/dict-data', data)
}

export function updateDictData(data: SysDictDataSave) {
  return put<ApiResponse<unknown>>('/system/dict-data', data)
}

export function deleteDictData(ids: string | string[]) {
  return del<ApiResponse<unknown>>('/system/dict-data', Array.isArray(ids) ? ids : [ids])
}

// ==================== 系统配置 ====================
export interface SysConfig {
  id: string
  configName: string
  configKey: string
  configValue: string
  configType: number
  remark: string
  createTime: string
}

export interface SysConfigSave {
  id?: string
  configName?: string
  configKey?: string
  configValue?: string
  configType?: number
  remark?: string
}

export function getConfigList(params: PageParams, config?: AxiosRequestConfig) {
  return get<PageResult<SysConfig>>('/system/config/list', params, config)
}

export function addConfig(data: SysConfigSave) {
  return post<ApiResponse<unknown>>('/system/config', data)
}

export function updateConfig(data: SysConfigSave) {
  return put<ApiResponse<unknown>>('/system/config', data)
}

export function deleteConfig(ids: string | string[]) {
  return del<ApiResponse<unknown>>('/system/config', Array.isArray(ids) ? ids : [ids])
}

// ==================== 文件管理 ====================
export interface SysFile {
  id: string
  fileName: string
  originalName: string
  filePath: string
  fileSize: number
  mimeType: string
  fileExt: string
  storageType: string
  storageUrl: string
  module: string
  createTime: string
}

export function getFileList(params: PageParams, config?: AxiosRequestConfig) {
  return get<PageResult<SysFile>>('/system/file/list', params, config)
}

export function uploadFile(formData: FormData, onProgress?: (percent: number) => void) {
  return upload('/system/file/upload', formData, onProgress)
}

export function downloadFile(id: string) {
  return get(`/system/file/download/${id}`, undefined, { responseType: 'blob' })
}

export function deleteFile(ids: string | string[]) {
  return del<ApiResponse<unknown>>('/system/file', Array.isArray(ids) ? ids : [ids])
}