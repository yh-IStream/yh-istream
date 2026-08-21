import type { GlobalTheme } from 'naive-ui'

export const useAppStore = defineStore('app', () => {
  /** 暗色模式 */
  const darkMode = ref(false)
  /** 侧边栏折叠 */
  const collapsed = ref(false)
  /** 是否移动端 */
  const isMobile = ref(false)

  const theme = computed<GlobalTheme | null>(() => (darkMode.value ? null : null))

  const themeOverrides = computed(() => ({
    common: {
      primaryColor: '#18a058',
      primaryColorHover: '#36ad6a',
      primaryColorPressed: '#0c7a43',
      primaryColorSuppl: '#36ad6a',
      borderRadius: '6px',
    },
  }))

  function toggleDark() {
    darkMode.value = !darkMode.value
  }

  function toggleCollapsed() {
    collapsed.value = !collapsed.value
  }

  return {
    darkMode,
    collapsed,
    isMobile,
    theme,
    themeOverrides,
    toggleDark,
    toggleCollapsed,
  }
})