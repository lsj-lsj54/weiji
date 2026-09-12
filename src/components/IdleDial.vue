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

function onInput(event: Event) {
  const raw = (event.target as HTMLInputElement).value
  if (raw === '') return
  minutes.value = clamp(Number(raw))
}

function onBlur() {
  minutes.value = clamp(minutes.value)
}
</script>

<template>
  <div class="dial">
    <div class="dial__core">
      <button class="ghost" type="button" aria-label="减少五分钟" @click="bump(-5)">−</button>
      <label class="dial__label">
        <input
          class="num dial__n"
          type="number"
          inputmode="numeric"
          :min="MIN"
          :max="MAX"
          :value="minutes"
          aria-label="空闲分钟"
          @input="onInput"
          @blur="onBlur"
        />
        <span class="muted">空闲分钟，自己写</span>
      </label>
      <button class="ghost" type="button" aria-label="增加五分钟" @click="bump(5)">+</button>
    </div>
  </div>
</template>

<style scoped>
.dial {
  padding: 8px 0 4px;
}
.dial__core {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
  min-height: 180px;
}
.dial__label {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}
.dial__n {
  width: 4.2ch;
  font-size: 88px;
  line-height: 0.9;
  font-weight: 600;
  text-align: center;
  letter-spacing: -0.04em;
  background: transparent;
  border: 0;
  border-bottom: 1px solid var(--line);
  padding: 0 0 8px;
  appearance: textfield;
}
.dial__n::-webkit-outer-spin-button,
.dial__n::-webkit-inner-spin-button {
  appearance: none;
  margin: 0;
}
.ghost {
  width: 40px;
  height: 40px;
  border: 1px solid var(--line);
  background: transparent;
  color: var(--mute-slate);
  font-size: 22px;
  border-radius: var(--radius-btn);
}
</style>
