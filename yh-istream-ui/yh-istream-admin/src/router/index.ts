import { createRouter, createWebHistory } from 'vue-router'
import { routes, handleHotUpdate } from 'vue-router/auto-routes'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

router.beforeEach((to, _from, next) => {
  const publicPaths = ['/login', '/dashboard', '/404', '/403']
  if (publicPaths.includes(to.path)) {
    return next()
  }
  const token = localStorage.getItem('yh-istream-token')
  if (!token) {
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }
  const authStore = useAuthStore()
  if (authStore.menuPaths.size > 0 && !authStore.menuPaths.has(to.path)) {
    return next({ path: '/403' })
  }
  next()
})

if (import.meta.hot) {
  handleHotUpdate(router)
}

export default router