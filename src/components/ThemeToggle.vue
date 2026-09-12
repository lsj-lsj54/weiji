<script setup lang="ts">
import { useTheme } from '@/composables/useTheme'

const { theme, setTheme } = useTheme()
</script>

<template>
  <div class="chips theme-toggle" role="group" aria-label="外观">
    <button
      class="chip"
      :class="{ 'is-on': theme === 'light' }"
      type="button"
      :aria-pressed="theme === 'light'"
      @click="setTheme('light')"
    >
      日间
    </button>
    <button
      class="chip"
      :class="{ 'is-on': theme === 'dark' }"
      type="button"
      :aria-pressed="theme === 'dark'"
      @click="setTheme('dark')"
    >
      夜间
    </button>
  </div>
</template>
