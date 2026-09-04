import { post, get } from '@/api/request'

export interface LoginParams {
  username: string
  password: string
  captchaKey: string
  captchaCode: string
}

export interface LoginResult {
  tokenValue: string
  tokenName: string
}

export interface UserInfoResult {
  user: {
    id: string
    username: string
    nickname: string
    avatar: string
    deptName: string
    email: string
    phone: string
    status: number
  }
  permissions: string[]
  roles: string[]
  menus: any[]
}

export interface CaptchaResult {
  uuid: string
  image: string
}

/** 登录 */
export function login(data: LoginParams) {
  return post<{ code: number; data: LoginResult; msg: string }>('/auth/login', data)
}

/** 登出 */
export function logout() {
  return post('/auth/logout')
}

/** 获取用户信息 */
export function getUserInfo() {
  return get<{ code: number; data: UserInfoResult; msg: string }>('/auth/user-info')
}

/** 获取验证码 */
export function getCaptcha() {
  return get<{ code: number; data: CaptchaResult; msg: string }>('/auth/captcha')
}