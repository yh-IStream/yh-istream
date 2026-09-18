import { darkTheme, type GlobalTheme } from 'naive-ui'

const DARK_MODE_KEY = 'yh-istream-dark-mode'
const COLLAPSED_KEY = 'yh-istream-collapsed'

export const useAppStore = defineStore('app', () => {
  const darkMode = ref(localStorage.getItem(DARK_MODE_KEY) === 'true')
  const collapsed = ref(localStorage.getItem(COLLAPSED_KEY) === 'true')
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
    localStorage.setItem(DARK_MODE_KEY, String(darkMode.value))
  }

  function toggleCollapsed() {
    collapsed.value = !collapsed.value
    localStorage.setItem(COLLAPSED_KEY, String(collapsed.value))
  }

  if (typeof window !== 'undefined') {
    const mediaQuery = window.matchMedia('(max-width: 768px)')
    isMobile.value = mediaQuery.matches
    mediaQuery.addEventListener('change', (e) => {
      isMobile.value = e.matches
    })
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