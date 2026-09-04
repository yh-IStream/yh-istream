import { darkTheme, type GlobalTheme } from 'naive-ui'

export const useAppStore = defineStore('app', () => {
  const darkMode = ref(false)
  const collapsed = ref(false)
  const isMobile = ref(false)

  const theme = computed<GlobalTheme | null>(() => (darkMode.value ? darkTheme : null))

  const themeOverrides = computed(() => ({
    common: {
      primaryColor: '#14b8a6',
      primaryColorHover: '#2dd4bf',
      primaryColorPressed: '#0d9488',
      primaryColorSuppl: '#2dd4bf',
      borderRadius: '8px',
      borderRadiusSmall: '6px',
      inputColor: darkMode.value ? 'rgba(30,41,59,0.6)' : 'rgba(255,255,255,0.8)',
      cardColor: darkMode.value ? 'rgba(30,41,59,0.6)' : 'rgba(255,255,255,0.8)',
      modalColor: darkMode.value ? 'rgba(30,41,59,0.85)' : 'rgba(255,255,255,0.9)',
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