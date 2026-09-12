import { ref } from 'vue'

export type Theme = 'light' | 'dark'

const STORAGE_KEY = 'weiji-theme'

function readStored(): Theme | null {
  try {
    const value = localStorage.getItem(STORAGE_KEY)
    if (value === 'light' || value === 'dark') return value
  } catch {
    /* private mode */
  }
  return null
}

function systemTheme(): Theme {
  return window.matchMedia('(prefers-color-scheme: light)').matches ? 'light' : 'dark'
}

function currentTheme(): Theme {
  const attr = document.documentElement.dataset.theme
  if (attr === 'light' || attr === 'dark') return attr
  return readStored() || systemTheme()
}

function apply(next: Theme) {
  document.documentElement.dataset.theme = next
  document.documentElement.style.colorScheme = next
}

const theme = ref<Theme>(typeof document === 'undefined' ? 'dark' : currentTheme())

if (typeof document !== 'undefined') {
  apply(theme.value)
}

export function useTheme() {
  function setTheme(next: Theme) {
    theme.value = next
    apply(next)
    try {
      localStorage.setItem(STORAGE_KEY, next)
    } catch {
      /* ignore */
    }
  }

  function toggle() {
    setTheme(theme.value === 'light' ? 'dark' : 'light')
  }

  return { theme, setTheme, toggle }
}
