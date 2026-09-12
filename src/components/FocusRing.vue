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
  return Math.min(1, props.elapsed / plan)
})

const dash = computed(() => {
  const c = 2 * Math.PI * 88
  return `${c * progress.value} ${c}`
})
</script>

<template>
  <div class="ring" :class="{ 'is-live': live }">
    <svg viewBox="0 0 200 200" aria-hidden="true">
      <circle cx="100" cy="100" r="88" class="track" />
      <circle cx="100" cy="100" r="88" class="fill" :stroke-dasharray="dash" />
    </svg>
    <div class="ring__label">
      <div class="num ring__time">{{ mmss }}</div>
      <div class="muted">{{ live ? '这段时间正在过' : '已暂停' }}</div>
    </div>
  </div>
</template>

<style scoped>
.ring {
  position: relative;
  width: 240px;
  height: 240px;
  margin: 8px auto 16px;
}
svg {
  width: 100%;
  height: 100%;
  transform: rotate(-90deg);
}
.track {
  fill: none;
  stroke: rgba(232, 238, 244, 0.08);
  stroke-width: 3;
}
.fill {
  fill: none;
  stroke: rgba(212, 162, 74, 0.35);
  stroke-width: 3;
  stroke-linecap: square;
}
.is-live .fill {
  stroke: var(--street-brass);
}
.ring__label {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.ring__time {
  font-size: 44px;
  font-weight: 600;
  letter-spacing: -0.03em;
}
</style>
