<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  elapsed: number
  plannedMinutes?: number
  live?: boolean
}>()

const mmss = computed(() => {
  const s = Math.max(0, props.elapsed)
  const m = Math.floor(s / 60)
  const r = s % 60
  return `${String(m).padStart(2, '0')}:${String(r).padStart(2, '0')}`
})

const progress = computed(() => {
  const plan = (props.plannedMinutes || 25) * 60
  return Math.min(100, Math.round((props.elapsed / plan) * 100))
})
</script>

<template>
  <view class="ring">
    <text class="num ring__time">{{ mmss }}</text>
    <text class="muted">{{ live ? '这段时间正在过' : '已暂停' }}</text>
    <view class="bar">
      <view class="bar__fill" :class="{ 'is-live': live }" :style="{ width: progress + '%' }" />
    </view>
  </view>
</template>

<style scoped>
.ring {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 0 16px;
}
.ring__time {
  font-size: 44px;
  font-weight: 600;
  letter-spacing: -0.03em;
}
.bar {
  width: 100%;
  height: 3px;
  margin-top: 16px;
  background: var(--ring-track);
}
.bar__fill {
  height: 100%;
  background: var(--street-brass);
  opacity: 0.45;
}
.bar__fill.is-live {
  opacity: 1;
}
</style>
