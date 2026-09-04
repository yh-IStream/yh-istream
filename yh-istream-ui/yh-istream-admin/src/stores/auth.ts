import { login as loginApi, logout as logoutApi, getUserInfo } from '@/api/modules/auth'
import router from '@/router'

export interface UserInfo {
  id: string
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
  const menuPaths = ref<Set<string>>(new Set())

  const isLoggedIn = computed(() => !!token.value)
  const nickname = computed(() => userInfo.value?.nickname ?? userInfo.value?.username ?? '')
  const avatar = computed(() => userInfo.value?.avatar ?? '')

  function extractMenuPaths(menus: any[]): Set<string> {
    const paths = new Set<string>()
    function walk(items: any[]) {
      for (const item of items) {
        if (item.path) paths.add(item.path)
        if (item.children?.length) walk(item.children)
      }
    }
    walk(menus)
    return paths
  }

  /** 登录 */
  async function login(username: string, password: string, captchaKey: string, captchaCode: string) {
    const res = await loginApi({ username, password, captchaKey, captchaCode })
    const tokenVal = (res.data as any).token ?? (res.data as any).tokenValue
    token.value = tokenVal
    localStorage.setItem(TOKEN_KEY, tokenVal)
    try {
      await fetchUserInfo()
    } catch {
      token.value = ''
      localStorage.removeItem(TOKEN_KEY)
      throw new Error('获取用户信息失败，请重试')
    }
  }

  /** 获取用户信息 */
  async function fetchUserInfo() {
    const res = await getUserInfo()
    userInfo.value = res.data.user as unknown as UserInfo
    permissions.value = res.data.permissions ?? []
    roles.value = res.data.roles ?? []
    menuPaths.value = extractMenuPaths(res.data.menus ?? [])
  }

  /** 登出 */
  async function logout() {
    try {
      if (token.value) {
        await logoutApi()
      }
    } finally {
      token.value = ''
      userInfo.value = null
      permissions.value = []
      roles.value = []
      menuPaths.value = new Set()
      localStorage.removeItem(TOKEN_KEY)
      router.push('/login')
    }
  }

  return {
    token,
    userInfo,
    permissions,
    roles,
    menuPaths,
    isLoggedIn,
    nickname,
    avatar,
    login,
    fetchUserInfo,
    logout,
  }
})