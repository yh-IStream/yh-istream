import { login as loginApi, logout as logoutApi, getUserInfo } from '@/api/modules/auth'
import router from '@/router'

export interface UserInfo {
  id: number
  username: string
  nickname: string
  avatar: string
  deptName: string
  permissions: string[]
  roles: string[]
}

const TOKEN_KEY = 'yh-istream-token'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem(TOKEN_KEY) ?? '')
  const userInfo = ref<UserInfo | null>(null)
  const permissions = ref<string[]>([])
  const roles = ref<string[]>([])

  const isLoggedIn = computed(() => !!token.value)
  const nickname = computed(() => userInfo.value?.nickname ?? userInfo.value?.username ?? '')
  const avatar = computed(() => userInfo.value?.avatar ?? '')

  /** 登录 */
  async function login(username: string, password: string, captchaKey: string, captchaCode: string) {
    const res = await loginApi({ username, password, captchaKey, captchaCode })
    token.value = res.data.tokenValue
    localStorage.setItem(TOKEN_KEY, res.data.tokenValue)
    await fetchUserInfo()
  }

  /** 获取用户信息 */
  async function fetchUserInfo() {
    const res = await getUserInfo()
    userInfo.value = res.data.user
    permissions.value = res.data.permissions ?? []
    roles.value = res.data.roles ?? []
  }

  /** 登出 */
  async function logout() {
    try {
      await logoutApi()
    } finally {
      token.value = ''
      userInfo.value = null
      permissions.value = []
      roles.value = []
      localStorage.removeItem(TOKEN_KEY)
      router.push('/login')
    }
  }

  return {
    token,
    userInfo,
    permissions,
    roles,
    isLoggedIn,
    nickname,
    avatar,
    login,
    fetchUserInfo,
    logout,
  }
})