import { post, get } from '@/api/request'
import type { ApiResponse } from '@/api/types'
import type { AxiosRequestConfig } from 'axios'
import type { SysMenu } from '@/api/modules/system'

export interface LoginParams {
  username: string
  password: string
  captchaKey: string
  captchaCode: string
}

export interface LoginResult {
  token: string
  tokenName: string
}

export interface UserInfoResult {
  user: {
    id: string
    username: string
    nickname: string
    avatar: string
    deptId: string
    deptName: string
    email: string
    phone: string
    gender: number
    status: number
    loginIp: string
    loginDate: string
    loginCount: number
    roleIds: string[]
    roleNames: string[]
  }
  permissions: string[]
  roles: string[]
  menus: SysMenu[]
}

export interface CaptchaResult {
  uuid: string
  image: string
}

/** 登录 */
export function login(data: LoginParams) {
  return post<ApiResponse<LoginResult>>('/auth/login', data)
}

/** 登出 */
export function logout() {
  return post('/auth/logout')
}

/** 获取用户信息 */
export function getUserInfo() {
  return get<ApiResponse<UserInfoResult>>('/auth/user-info')
}

/** 获取验证码 */
export function getCaptcha(config?: AxiosRequestConfig) {
  return get<ApiResponse<CaptchaResult>>('/auth/captcha', undefined, config)
}