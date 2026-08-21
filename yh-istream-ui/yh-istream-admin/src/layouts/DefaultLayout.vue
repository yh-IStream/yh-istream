<script setup lang="ts">
import type { MenuOption } from 'naive-ui'
import {
  HomeOutline,
  SettingsOutline,
  PulseOutline,
  CodeSlashOutline,
  PersonOutline,
  LogOutOutline,
  MoonOutline,
  SunnyOutline,
  MenuOutline,
} from '@vicons/ionicons5'

const router = useRouter()
const route = useRoute()
const appStore = useAppStore()
const authStore = useAuthStore()

function renderIcon(icon: Component) {
  return () => h(NIcon, null, { default: () => h(icon) })
}

const menuOptions: MenuOption[] = [
  {
    label: '首页',
    key: 'dashboard',
    icon: renderIcon(HomeOutline),
  },
  {
    label: '系统管理',
    key: 'system',
    icon: renderIcon(SettingsOutline),
    children: [
      { label: '用户管理', key: 'system-user' },
      { label: '角色管理', key: 'system-role' },
      { label: '菜单管理', key: 'system-menu' },
      { label: '部门管理', key: 'system-dept' },
      { label: '系统配置', key: 'system-config' },
      { label: '字典管理', key: 'system-dict' },
      { label: '文件管理', key: 'system-file' },
    ],
  },
  {
    label: '监控管理',
    key: 'monitor',
    icon: renderIcon(PulseOutline),
    children: [
      { label: '操作日志', key: 'monitor-oper-log' },
      { label: '登录日志', key: 'monitor-login-info' },
    ],
  },
  {
    label: '代码生成器',
    key: 'generator',
    icon: renderIcon(CodeSlashOutline),
  },
]

const activeKey = ref<string>('')
const dropdownOptions = [
  { label: '个人中心', key: 'profile', icon: renderIcon(PersonOutline) },
  { type: 'divider' as const, key: 'd1' },
  { label: '退出登录', key: 'logout', icon: renderIcon(LogOutOutline) },
]

function handleMenuUpdate(key: string) {
  const routeMap: Record<string, string> = {
    dashboard: '/dashboard',
    'system-user': '/system/user',
    'system-role': '/system/role',
    'system-menu': '/system/menu',
    'system-dept': '/system/dept',
    'system-config': '/system/config',
    'system-dict': '/system/dict',
    'system-file': '/system/file',
    'monitor-oper-log': '/monitor/oper-log',
    'monitor-login-info': '/monitor/login-info',
    generator: '/generator',
  }
  const path = routeMap[key]
  if (path) router.push(path)
}

function handleDropdownSelect(key: string) {
  if (key === 'logout') {
    authStore.logout()
  }
}

watch(
  () => route.path,
  (path) => {
    const parts = path.split('/').filter(Boolean)
    if (parts.length >= 2) {
      activeKey.value = `${parts[0]}-${parts[1]}`
    } else if (parts.length === 1) {
      activeKey.value = parts[0]
    }
  },
  { immediate: true },
)
</script>

<template>
  <n-layout class="wh-full">
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
            <n-breadcrumb-item v-for="item in []" :key="item">
              {{ item }}
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