<script setup lang="ts">
import { zhCN, dateZhCN } from 'naive-ui'
import DefaultLayout from '@/layouts/DefaultLayout.vue'

const appStore = useAppStore()
const route = useRoute()

const FULLSCREEN_ROUTES = new Set(['/', '/login/', '/[...all]', '/error/404'])
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
            <DefaultLayout v-if="!FULLSCREEN_ROUTES.has(String(route.name))">
              <RouterView />
            </DefaultLayout>
            <RouterView v-else />
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
</style>