import { createRouter, createWebHistory, type RouteLocationRaw } from 'vue-router'
import { routes, handleHotUpdate } from 'vue-router/auto-routes'
import { useAuthStore, TOKEN_KEY } from '@/stores/auth'

const PUBLIC_PATHS = new Set(['/login', '/error/404', '/error/403'])

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

async function ensureAuthenticated(redirectOnLogin?: string): Promise<RouteLocationRaw | undefined> {
  const token = localStorage.getItem(TOKEN_KEY)
  if (!token) {
    return redirectOnLogin
      ? { path: '/login', query: { redirect: redirectOnLogin } }
      : { path: '/login' }
  }
  const authStore = useAuthStore()
  if (!authStore.userInfo) {
    try {
      await authStore.fetchUserInfo()
    } catch {
      authStore.logout()
      return { path: '/login' }
    }
  }
}

router.beforeEach(async (to) => {
  if (PUBLIC_PATHS.has(to.path)) {
    return
  }

  if (to.path === '/') {
    const redirect = await ensureAuthenticated()
    if (redirect) return redirect
    return { path: useAuthStore().firstMenuPath }
  }

  const redirect = await ensureAuthenticated(to.fullPath)
  if (redirect) return redirect
  const authStore = useAuthStore()
  if (authStore.menuPaths.size > 0 && !authStore.hasPathPermission(to.path)) {
    return { path: '/error/403' }
  }
})

router.afterEach((to) => {
  const appTitle = import.meta.env.VITE_APP_TITLE || 'iStream'
  const pageTitle = to.meta?.title as string | undefined
  document.title = pageTitle ? `${pageTitle} - ${appTitle}` : appTitle
})

if (import.meta.hot) {
  handleHotUpdate(router)
}

export default router