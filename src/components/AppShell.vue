<script setup lang="ts">
defineProps<{ energy?: string }>()
</script>

<template>
  <header class="row" style="margin-bottom: 12px; padding: 20px 20px 0">
    <router-link to="/now" class="brand">微积</router-link>
    <span class="muted" style="font-size: 13px">{{ energy || '' }}</span>
  </header>
  <slot />
  <nav class="dock" aria-label="主导航">
    <router-link to="/now">此刻</router-link>
    <router-link to="/tasks">待办</router-link>
    <router-link to="/points">积点</router-link>
    <router-link to="/me">我</router-link>
  </nav>
</template>
