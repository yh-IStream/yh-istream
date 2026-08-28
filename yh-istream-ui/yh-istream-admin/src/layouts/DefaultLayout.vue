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
  },
  { immediate: true },
)
</script>

<template>
  <n-layout has-sider class="wh-full">
    <n-layout-sider
      bordered
      :width="220"
      :collapsed-width="64"
      :collapsed="appStore.collapsed"
      collapse-mode="width"
      :native-scrollbar="false"
      show-trigger="bar"
      @collapse="appStore.collapsed = true"
      @expand="appStore.collapsed = false"
    >
      <div class="flex items-center h-60px px-16px border-b border-gray-200 dark:border-gray-700">
        <div class="flex items-center gap-8px">
          <div class="w-32px h-32px rounded-md bg-primary flex-center text-white text-16px font-bold">
            i
          </div>
          <transition name="fade">
            <span v-if="!appStore.collapsed" class="text-lg font-semibold text-gray-800 dark:text-gray-100 whitespace-nowrap">
              iStream
            </span>
          </transition>
        </div>
      </div>

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
      <n-layout-header bordered class="h-60px px-16px flex items-center justify-between">
        <div class="flex items-center gap-12px">
          <n-button quaternary circle size="small" @click="appStore.toggleCollapsed">
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

        <div class="flex items-center gap-8px">
          <n-button quaternary circle size="small" @click="appStore.toggleDark">
            <template #icon>
              <n-icon :component="appStore.darkMode ? SunnyOutline : MoonOutline" />
            </template>
          </n-button>

          <n-dropdown :options="dropdownOptions" @select="handleDropdownSelect">
            <div class="flex items-center gap-8px cursor-pointer px-8px py-4px rounded-md hover:bg-gray-100 dark:hover:bg-gray-800">
              <n-avatar round size="small" :style="{ backgroundColor: '#18a058' }">
                {{ authStore.nickname.charAt(0) }}
              </n-avatar>
              <span class="text-sm text-gray-700 dark:text-gray-300">{{ authStore.nickname }}</span>
            </div>
          </n-dropdown>
        </div>
      </n-layout-header>

      <n-layout-content class="p-16px" :native-scrollbar="false">
        <slot />
      </n-layout-content>
    </n-layout>
  </n-layout>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>