import { ref } from 'vue'

export type Theme = 'light' | 'dark'

const STORAGE_KEY = 'weiji-theme'
export const theme = ref<Theme>('dark')

function readStored(): Theme {
  const value = uni.getStorageSync(STORAGE_KEY)
  return value === 'light' ? 'light' : 'dark'
}

export function applyTheme(next?: Theme) {
  theme.value = next || readStored()
  uni.setStorageSync(STORAGE_KEY, theme.value)
  // #ifdef H5
  if (typeof document !== 'undefined') {
    document.documentElement.dataset.theme = theme.value
    document.documentElement.style.colorScheme = theme.value
  }
  // #endif
}

export function toggleTheme() {
  applyTheme(theme.value === 'light' ? 'dark' : 'light')
}
