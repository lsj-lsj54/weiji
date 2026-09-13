<script setup lang="ts">
import { theme, toggleTheme } from '@/composables/useTheme'
</script>

<template>
  <view class="theme-toggle" @click="toggleTheme">
    <text>{{ theme === 'light' ? '夜' : '昼' }}</text>
  </view>
</template>

<style scoped>
.theme-toggle {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--line);
  border-radius: 999px;
  font-size: 13px;
  color: var(--porcelain);
}
</style>
