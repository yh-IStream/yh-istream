<script setup lang="ts">
import type { MenuOption } from 'naive-ui'
import {
  HomeOutline,
  SettingsOutline,
  CodeSlashOutline,
  PersonOutline,
  LogOutOutline,
  MoonOutline,
  SunnyOutline,
  MenuOutline,
  CloudUploadOutline,
  ListOutline,
  DesktopOutline,
  DocumentTextOutline,
  LogInOutline,
  ClipboardOutline,
  FolderOpenOutline,
  GitBranchOutline,
  BookOutline,
  CogOutline,
  ShieldCheckmarkOutline,
} from '@vicons/ionicons5'
import { getUserMenuTree } from '@/api/modules/system'

const router = useRouter()
const route = useRoute()
const appStore = useAppStore()
const authStore = useAuthStore()

const pageReady = ref(false)

function renderIcon(icon: Component) {
  return () => h(NIcon, null, { default: () => h(icon) })
}

const iconMap: Record<string, Component> = {
  home: HomeOutline,
  system: SettingsOutline,
  monitor: DesktopOutline,
  generator: CodeSlashOutline,
  user: PersonOutline,
  role: ShieldCheckmarkOutline,
  menu: MenuOutline,
  dept: GitBranchOutline,
  config: CogOutline,
  dict: BookOutline,
  file: FolderOpenOutline,
  upload: CloudUploadOutline,
  list: ListOutline,
  log: DocumentTextOutline,
  operLog: ClipboardOutline,
  loginInfo: LogInOutline,
}

function buildMenuTree(menus: any[], parentPath: string = ''): MenuOption[] {
  return menus.map((menu: any) => {
    const rawPath = menu.path || ''
    const fullPath = rawPath ? (rawPath.startsWith('/') ? rawPath : (parentPath ? `${parentPath}/${rawPath}` : `/${rawPath}`)) : ''
    const option: MenuOption = {
      label: menu.menuName,
      key: fullPath || String(menu.id),
    }
    if (menu.icon && iconMap[menu.icon]) {
      option.icon = renderIcon(iconMap[menu.icon])
    }
    if (menu.children && menu.children.length > 0) {
      option.children = buildMenuTree(menu.children, fullPath)
    }
    return option
  })
}

const menuOptions = ref<MenuOption[]>([
  {
    label: '首页',
    key: '/dashboard',
    icon: renderIcon(HomeOutline),
  },
])

const menuPathMap = ref<Record<string, string>>({})

function buildPathMap(menus: any[], prefix: string = '') {
  for (const menu of menus) {
    const path = menu.path
    if (path) {
      const fullPath = path.startsWith('/') ? path : `/${path}`
      menuPathMap.value[fullPath] = menu.menuName
    }
    if (menu.children?.length) {
      buildPathMap(menu.children, menu.path || '')
    }
  }
}

async function fetchUserMenu() {
  try {
    const res: any = await getUserMenuTree()
    const menus = (res.data ?? []).filter((m: any) => m.path !== '/dashboard')
    menuPathMap.value = {}
    buildPathMap(menus)
    menuOptions.value = [
      {
        label: '首页',
        key: '/dashboard',
        icon: renderIcon(HomeOutline),
      },
      ...buildMenuTree(menus),
    ]
  } catch {
    /* ignore */
  }
}

fetchUserMenu()

const activeKey = ref<string>('')

const breadcrumbs = computed(() => {
  const path = route.path
  if (path === '/dashboard') return [{ label: '首页' }]
  const segments = path.split('/').filter(Boolean)
  if (segments.length === 0) return [{ label: '首页' }]
  const result: { label: string }[] = [{ label: '首页' }]
  let accumulated = ''
  for (const seg of segments) {
    accumulated += `/${seg}`
    result.push({ label: menuPathMap.value[accumulated] ?? seg })
  }
  return result
})
const dropdownOptions = [
  { label: '个人中心', key: 'profile', icon: renderIcon(PersonOutline) },
  { type: 'divider' as const, key: 'd1' },
  { label: '退出登录', key: 'logout', icon: renderIcon(LogOutOutline) },
]

function handleMenuUpdate(key: string, item: MenuOption) {
  if (item.children && item.children.length > 0) {
    return
  }
  router.push(key)
}

function handleDropdownSelect(key: string) {
  if (key === 'logout') {
    authStore.logout()
  }
}

watch(
  () => route.path,
  (path) => {
    activeKey.value = path
    pageReady.value = false
    requestAnimationFrame(() => {
      pageReady.value = true
    })
  },
  { immediate: true },
)
</script>

<template>
  <n-layout has-sider class="wh-full layout-root">
    <n-layout-sider
      :width="220"
      :collapsed-width="64"
      :collapsed="appStore.collapsed"
      collapse-mode="width"
      :native-scrollbar="false"
      show-trigger="bar"
      class="layout-sider"
      @collapse="appStore.collapsed = true"
      @expand="appStore.collapsed = false"
    >
      <div class="sider-logo">
        <div class="flex items-center gap-2">
          <div class="logo-mark">
            <svg viewBox="0 0 40 40" width="32" height="32">
              <defs>
                <linearGradient id="siderGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" style="stop-color:#14b8a6" />
                  <stop offset="100%" style="stop-color:#06b6d4" />
                </linearGradient>
              </defs>
              <path d="M8 20 Q14 10 20 20 Q26 30 32 20" stroke="url(#siderGrad)" stroke-width="3" fill="none" stroke-linecap="round" />
              <circle cx="8" cy="20" r="2" fill="#14b8a6" />
              <circle cx="32" cy="20" r="2" fill="#06b6d4" />
            </svg>
          </div>
          <transition name="fade">
            <span v-if="!appStore.collapsed" class="text-base font-bold whitespace-nowrap text-gray-700 dark:text-gray-200 tracking-wide">
              iStream
            </span>
          </transition>
        </div>
      </div>

      <div class="sider-glow-line" />

      <n-menu
        v-model:value="activeKey"
        :options="menuOptions"
        :collapsed="appStore.collapsed"
        :collapsed-width="64"
        :collapsed-icon-size="22"
        @update:value="handleMenuUpdate"
      />
    </n-layout-sider>

    <n-layout>
      <n-layout-header class="layout-header">
        <div class="header-scan-line" />

        <div class="flex items-center gap-3 relative z-1">
          <n-button quaternary circle size="small" class="menu-toggle-btn" @click="appStore.toggleCollapsed">
            <template #icon>
              <n-icon :component="MenuOutline" />
            </template>
          </n-button>
          <n-breadcrumb>
            <n-breadcrumb-item v-for="item in breadcrumbs" :key="item.label">
              {{ item.label }}
            </n-breadcrumb-item>
          </n-breadcrumb>
        </div>

        <div class="flex items-center gap-2 relative z-1">
          <n-button quaternary circle size="small" class="theme-toggle-btn" @click="appStore.toggleDark">
            <template #icon>
              <n-icon :component="appStore.darkMode ? SunnyOutline : MoonOutline" />
            </template>
          </n-button>

          <n-dropdown :options="dropdownOptions" @select="handleDropdownSelect">
            <div class="user-badge">
              <n-avatar round size="small" class="user-avatar">
                {{ authStore.nickname.charAt(0) }}
              </n-avatar>
              <span class="text-sm text-gray-600 dark:text-gray-300 font-medium">{{ authStore.nickname }}</span>
            </div>
          </n-dropdown>
        </div>
      </n-layout-header>

      <n-layout-content class="layout-content" :native-scrollbar="false">
        <transition name="page" mode="out-in">
          <div :key="route.path" class="page-wrapper">
            <slot />
          </div>
        </transition>
      </n-layout-content>
    </n-layout>
  </n-layout>
</template>

<style scoped>
.layout-root {
  background: #f8fafc;
}

html.dark .layout-root {
  background: #0f172a;
}

.layout-sider {
  border-right: 1px solid rgba(20, 184, 166, 0.08) !important;
  backdrop-filter: blur(12px);
  background: rgba(255, 255, 255, 0.7) !important;
}

html.dark .layout-sider {
  background: rgba(15, 23, 42, 0.85) !important;
  border-right: 1px solid rgba(45, 212, 191, 0.06) !important;
}

.sider-logo {
  display: flex;
  align-items: center;
  height: 60px;
  padding: 0 16px;
  border-bottom: 1px solid rgba(20, 184, 166, 0.08);
}

html.dark .sider-logo {
  border-bottom: 1px solid rgba(45, 212, 191, 0.06);
}

.sider-glow-line {
  position: relative;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(20, 184, 166, 0.3), rgba(6, 182, 212, 0.3), transparent);
  background-size: 200% 100%;
  animation: stream-flow 4s ease infinite;
  opacity: 0.6;
}

html.dark .sider-glow-line {
  background: linear-gradient(90deg, transparent, rgba(20, 184, 166, 0.4), rgba(6, 182, 212, 0.4), transparent);
  background-size: 200% 100%;
}

.logo-mark {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: linear-gradient(135deg, rgba(20,184,166,0.1), rgba(6,182,212,0.1));
  transition: all 0.3s ease;
}

.logo-mark:hover {
  background: linear-gradient(135deg, rgba(20,184,166,0.2), rgba(6,182,212,0.2));
  box-shadow: 0 0 12px rgba(20, 184, 166, 0.2);
}

html.dark .logo-mark {
  background: linear-gradient(135deg, rgba(20,184,166,0.15), rgba(6,182,212,0.15));
}

.layout-header {
  height: 60px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid rgba(20, 184, 166, 0.08);
  backdrop-filter: blur(12px);
  background: rgba(255, 255, 255, 0.7);
  position: relative;
  overflow: hidden;
}

html.dark .layout-header {
  background: rgba(15, 23, 42, 0.85);
  border-bottom: 1px solid rgba(45, 212, 191, 0.06);
}

.header-scan-line {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent 0%, rgba(20,184,166,0.4) 30%, rgba(6,182,212,0.5) 50%, rgba(20,184,166,0.4) 70%, transparent 100%);
  background-size: 200% 100%;
  animation: stream-flow 3s ease infinite;
  opacity: 0.5;
  z-index: 0;
}

.menu-toggle-btn,
.theme-toggle-btn {
  transition: all 0.3s ease !important;
}

.menu-toggle-btn:hover,
.theme-toggle-btn:hover {
  color: #14b8a6 !important;
}

.user-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 10px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.user-badge:hover {
  background: rgba(20, 184, 166, 0.08);
  box-shadow: 0 0 8px rgba(20, 184, 166, 0.1);
}

html.dark .user-badge:hover {
  background: rgba(45, 212, 191, 0.1);
}

.user-avatar {
  background: linear-gradient(135deg, #14b8a6, #06b6d4) !important;
  transition: transform 0.3s ease;
}

.user-badge:hover .user-avatar {
  transform: scale(1.1);
}

.layout-content {
  padding: 16px;
}

.page-enter-active {
  transition: all 0.35s cubic-bezier(0.16, 1, 0.3, 1);
}
.page-leave-active {
  transition: all 0.2s ease-in;
}
.page-enter-from {
  opacity: 0;
  transform: translateY(12px) scale(0.98);
}
.page-leave-to {
  opacity: 0;
  transform: translateY(-6px) scale(1.01);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

:deep(.n-menu-item-content) {
  border-radius: 8px !important;
  transition: all 0.25s ease !important;
}

:deep(.n-menu-item-content:hover) {
  background: rgba(20, 184, 166, 0.06) !important;
}

html.dark :deep(.n-menu-item-content:hover) {
  background: rgba(45, 212, 191, 0.08) !important;
}

:deep(.n-menu-item-content--selected::before) {
  background: linear-gradient(135deg, rgba(20,184,166,0.12), rgba(6,182,212,0.08)) !important;
}

html.dark :deep(.n-menu-item-content--selected::before) {
  background: linear-gradient(135deg, rgba(20,184,166,0.18), rgba(6,182,212,0.12)) !important;
}
</style>