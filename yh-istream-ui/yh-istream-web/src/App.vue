<script setup lang="ts">
import { zhCN, dateZhCN } from 'naive-ui'
import DefaultLayout from '@/layouts/DefaultLayout.vue'

const appStore = useAppStore()
const route = useRoute()
const hasRenderError = ref(false)

const FULLSCREEN_ROUTES = new Set(['/login', '/error/403', '/error/404'])

onErrorCaptured((err, instance, info) => {
  console.error('组件渲染异常:', err, info)
  hasRenderError.value = true
  return false
})

function handleReload() {
  hasRenderError.value = false
  window.location.reload()
}
</script>

<template>
  <NConfigProvider
    :theme="appStore.theme"
    :theme-overrides="appStore.themeOverrides"
    :locale="zhCN"
    :date-locale="dateZhCN"
    class="wh-full"
  >
    <NMessageProvider>
      <NDialogProvider>
        <NNotificationProvider>
          <NLoadingBarProvider>
            <div v-if="hasRenderError" class="flex flex-col items-center justify-center wh-full">
              <h2 class="text-lg font-semibold text-red-500">页面渲染异常</h2>
              <p class="mt-2 text-sm text-gray-500">请刷新页面重试，若问题持续请联系管理员</p>
              <NButton class="mt-4" type="primary" @click="handleReload">刷新页面</NButton>
            </div>
            <template v-else>
              <DefaultLayout v-if="!FULLSCREEN_ROUTES.has(route.path)">
                <RouterView />
              </DefaultLayout>
              <RouterView v-else />
            </template>
          </NLoadingBarProvider>
        </NNotificationProvider>
      </NDialogProvider>
    </NMessageProvider>
  </NConfigProvider>
</template>

<style>
html,
body,
#app {
  height: 100%;
  margin: 0;
  padding: 0;
}

body {
  background: #f8fafc;
}

html.dark body {
  background: #0f172a;
}
</style>