<script setup lang="ts">
defineProps<{ energy?: string }>()
</script>

<template>
  <header class="row" style="margin-bottom: 18px">
    <h1 class="brand">微积</h1>
    <span class="muted" style="font-size: 13px">{{ energy || '此刻' }}</span>
  </header>
  <slot />
  <nav class="dock" aria-label="主导航">
    <router-link to="/now">此刻</router-link>
    <router-link to="/points">积点</router-link>
    <router-link to="/me">我</router-link>
  </nav>
</template>
