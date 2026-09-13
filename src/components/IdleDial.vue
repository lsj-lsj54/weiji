<script setup lang="ts">
const minutes = defineModel<number>({ required: true })

const MIN = 1
const MAX = 180

function clamp(n: number) {
  if (!Number.isFinite(n)) return MIN
  return Math.min(MAX, Math.max(MIN, Math.round(n)))
}

function bump(delta: number) {
  minutes.value = clamp(minutes.value + delta)
}

function onInput(event: { detail: { value: string } }) {
  const raw = event.detail.value
  if (raw === '') return
  minutes.value = clamp(Number(raw))
}
</script>

<template>
  <view class="dial">
    <view class="ghost" @click="bump(-5)">−</view>
    <view class="dial__label">
      <input class="dial__n num" type="number" :value="String(minutes)" @input="onInput" />
      <text class="muted">空闲分钟</text>
    </view>
    <view class="ghost" @click="bump(5)">+</view>
  </view>
</template>

<style scoped>
.dial {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
  min-height: 160px;
}
.dial__label {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}
.dial__n {
  width: 160px;
  font-size: 72px;
  line-height: 0.95;
  font-weight: 600;
  text-align: center;
  background: transparent;
  border: 0;
  border-bottom: 1px solid var(--line);
  color: var(--porcelain);
  padding: 0 0 8px;
}
.ghost {
  width: 40px;
  height: 40px;
  border: 1px solid var(--line);
  color: var(--mute-slate);
  font-size: 22px;
  border-radius: 999px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
