<script setup lang="ts">
import ThemeToggle from '@/components/ThemeToggle.vue'

defineProps<{ energy?: string }>()
</script>

<template>
  <header class="row shell-head">
    <router-link to="/now" class="brand">微积</router-link>
    <div class="shell-tools">
      <span class="muted" style="font-size: 13px">{{ energy || '' }}</span>
      <ThemeToggle />
    </div>
  </header>
  <slot />
  <nav class="dock" aria-label="主导航">
    <router-link to="/now">此刻</router-link>
    <router-link to="/tasks">待办</router-link>
    <router-link to="/points">积点</router-link>
    <router-link to="/me">我</router-link>
  </nav>
</template>
