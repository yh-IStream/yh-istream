import { login as loginApi, logout as logoutApi, getUserInfo } from '@/api/modules/auth'
import type { SysMenu } from '@/api/modules/system'
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

export const TOKEN_KEY = 'yh-istream-token'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem(TOKEN_KEY) ?? '')
  const userInfo = ref<UserInfo | null>(null)
  const permissions = ref<string[]>([])
  const roles = ref<string[]>([])
  const menuPaths = ref<Set<string>>(new Set())
  const menus = ref<SysMenu[]>([])

  const isLoggedIn = computed(() => !!token.value)
  const nickname = computed(() => userInfo.value?.nickname ?? userInfo.value?.username ?? '')
  const avatar = computed(() => userInfo.value?.avatar ?? '')
  const firstMenuPath = computed(() => {
    const first = menuPaths.value.values().next().value
    return first ?? '/error/403'
  })

  function extractMenuPaths(menus: SysMenu[]): Set<string> {
    const paths = new Set<string>()
    function walk(items: SysMenu[]) {
      for (const item of items) {
        if (item.path) paths.add(item.path)
        if (item.children?.length) walk(item.children)
      }
    }
    walk(menus)
    return paths
  }

  function hasPathPermission(path: string): boolean {
    if (menuPaths.value.has(path)) return true
    for (const menuPath of menuPaths.value) {
      if (path.startsWith(menuPath + '/')) return true
    }
    return false
  }

  /** 登录 */
  async function login(username: string, password: string, captchaKey: string, captchaCode: string) {
    const res = await loginApi({ username, password, captchaKey, captchaCode })
    const loginData = res.data
    token.value = loginData.token
    localStorage.setItem(TOKEN_KEY, loginData.token)
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
    const infoData = res.data
    const user = infoData.user
    userInfo.value = {
      id: user.id,
      username: user.username,
      nickname: user.nickname,
      avatar: user.avatar,
      deptName: user.deptName,
      permissions: infoData.permissions ?? [],
      roles: infoData.roles ?? [],
    }
    permissions.value = infoData.permissions ?? []
    roles.value = infoData.roles ?? []
    menus.value = infoData.menus ?? []
    menuPaths.value = extractMenuPaths(menus.value)
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
      menus.value = []
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
    menus,
    firstMenuPath,
    hasPathPermission,
    isLoggedIn,
    nickname,
    avatar,
    login,
    fetchUserInfo,
    logout,
  }
})