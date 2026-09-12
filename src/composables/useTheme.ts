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

const SWITCH_MS = 200

let switchTimer: number | undefined

function prefersReducedMotion() {
  return window.matchMedia('(prefers-reduced-motion: reduce)').matches
}

function apply(next: Theme) {
  document.documentElement.dataset.theme = next
  document.documentElement.style.colorScheme = next
}

function markSwitching() {
  if (prefersReducedMotion()) return
  const root = document.documentElement
  root.classList.add('is-theme-switching')
  if (switchTimer !== undefined) window.clearTimeout(switchTimer)
  switchTimer = window.setTimeout(() => {
    root.classList.remove('is-theme-switching')
    switchTimer = undefined
  }, SWITCH_MS)
}

const theme = ref<Theme>(typeof document === 'undefined' ? 'dark' : currentTheme())

if (typeof document !== 'undefined') {
  apply(theme.value)
}

export function useTheme() {
  function setTheme(next: Theme) {
    if (next === theme.value) return
    markSwitching()
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
