import { createRouter, createWebHistory } from 'vue-router'
import { routes, handleHotUpdate } from 'vue-router/auto-routes'
import { useAuthStore, TOKEN_KEY } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

router.beforeEach(async (to) => {
  const publicPaths = ['/', '/login', '/404', '/403']
  if (publicPaths.includes(to.path)) {
    return
  }
  const token = localStorage.getItem(TOKEN_KEY)
  if (!token) {
    return { path: '/login', query: { redirect: to.fullPath } }
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
  if (authStore.menuPaths.size > 0 && !authStore.menuPaths.has(to.path)) {
    return { path: '/403' }
  }
})

if (import.meta.hot) {
  handleHotUpdate(router)
}

export default router