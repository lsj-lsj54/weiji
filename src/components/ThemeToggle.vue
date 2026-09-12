<script setup lang="ts">
import { useTheme } from '@/composables/useTheme'

const { theme, toggle } = useTheme()

function originFromEvent(event: MouseEvent) {
  const keyboard = event.detail === 0
  if (!keyboard) return { x: event.clientX, y: event.clientY }
  const rect = (event.currentTarget as HTMLElement).getBoundingClientRect()
  return { x: rect.left + rect.width / 2, y: rect.top + rect.height / 2 }
}

function onToggle(event: MouseEvent) {
  toggle(originFromEvent(event))
}
</script>

<template>
  <button
    class="theme-toggle"
    type="button"
    :aria-label="theme === 'light' ? '切到夜间' : '切到日间'"
    @click="onToggle"
  >
    <svg v-if="theme === 'light'" viewBox="0 0 24 24" aria-hidden="true">
      <circle cx="12" cy="12" r="3.6" fill="currentColor" />
      <g fill="none" stroke="currentColor" stroke-linecap="round" stroke-width="1.7">
        <path d="M12 3.2v2.1M12 18.7v2.1M3.2 12h2.1M18.7 12h2.1M5.7 5.7l1.5 1.5M16.8 16.8l1.5 1.5M5.7 18.3l1.5-1.5M16.8 7.2l1.5-1.5" />
      </g>
    </svg>
    <svg v-else viewBox="0 0 24 24" aria-hidden="true">
      <path
        fill="currentColor"
        d="M14.8 4.2A8.4 8.4 0 1 0 19.6 16 6.6 6.6 0 0 1 14.8 4.2Z"
      />
    </svg>
  </button>
</template>
