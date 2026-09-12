import { ref } from 'vue'

export type Theme = 'light' | 'dark'

export type ThemeOrigin = { x: number; y: number }

const STORAGE_KEY = 'weiji-theme'
const SPREAD_MS = 400

type ViewTransition = {
  ready: Promise<void>
}

type DocumentWithViewTransition = Document & {
  startViewTransition?: (update: () => void) => ViewTransition
}

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

function prefersReducedMotion() {
  return window.matchMedia('(prefers-reduced-motion: reduce)').matches
}

function apply(next: Theme) {
  document.documentElement.dataset.theme = next
  document.documentElement.style.colorScheme = next
}

function persist(next: Theme) {
  theme.value = next
  apply(next)
  try {
    localStorage.setItem(STORAGE_KEY, next)
  } catch {
    /* ignore */
  }
}

function canAnimate() {
  const doc = document as DocumentWithViewTransition
  return !prefersReducedMotion() && typeof doc.startViewTransition === 'function'
}

function spreadFrom(origin: ThemeOrigin) {
  const { x, y } = origin
  const endRadius = Math.hypot(Math.max(x, innerWidth - x), Math.max(y, innerHeight - y))
  document.documentElement.animate(
    {
      clipPath: [`circle(0px at ${x}px ${y}px)`, `circle(${endRadius}px at ${x}px ${y}px)`],
    },
    {
      duration: SPREAD_MS,
      easing: 'ease-in-out',
      pseudoElement: '::view-transition-new(root)',
    },
  )
}

const theme = ref<Theme>(typeof document === 'undefined' ? 'dark' : currentTheme())

if (typeof document !== 'undefined') {
  apply(theme.value)
}

export function useTheme() {
  function setTheme(next: Theme, origin?: ThemeOrigin) {
    if (next === theme.value) return
    const commit = () => persist(next)
    const doc = document as DocumentWithViewTransition
    if (!origin || !canAnimate() || !doc.startViewTransition) {
      commit()
      return
    }
    const transition = doc.startViewTransition(commit)
    transition.ready.then(() => spreadFrom(origin)).catch(() => {
      /* aborted */
    })
  }

  function toggle(origin?: ThemeOrigin) {
    setTheme(theme.value === 'light' ? 'dark' : 'light', origin)
  }

  return { theme, setTheme, toggle }
}
