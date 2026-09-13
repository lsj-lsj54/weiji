<script setup lang="ts">
import { computed } from 'vue'
import ThemeToggle from '@/components/ThemeToggle.vue'
import { theme } from '@/composables/useTheme'

defineProps<{ energy?: string }>()

const pageClass = computed(() => `page theme-${theme.value}`)
</script>

<template>
  <view :class="pageClass">
    <view class="shell-head">
      <text class="brand">微积</text>
      <view class="shell-tools">
        <text class="muted" style="font-size: 13px">{{ energy || '' }}</text>
        <ThemeToggle />
      </view>
    </view>
    <slot />
  </view>
</template>
