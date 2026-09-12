<script setup lang="ts">
const minutes = defineModel<number>({ required: true })

const ticks = [5, 10, 15, 20, 25, 30, 45, 60]

function bump(delta: number) {
  minutes.value = Math.min(90, Math.max(5, minutes.value + delta))
}
</script>

<template>
  <div class="dial">
    <div class="dial__ring" aria-hidden="true">
      <span
        v-for="t in ticks"
        :key="t"
        class="tick"
        :class="{ 'is-on': minutes === t }"
        :style="{ transform: `rotate(${(t / 60) * 360 - 90}deg)` }"
      />
    </div>
    <div class="dial__core">
      <button class="ghost" type="button" aria-label="减少五分钟" @click="bump(-5)">−</button>
      <div>
        <div class="num dial__n">{{ minutes }}</div>
        <div class="muted" style="text-align: center">空闲分钟</div>
      </div>
      <button class="ghost" type="button" aria-label="增加五分钟" @click="bump(5)">+</button>
    </div>
    <div class="chips" style="justify-content: center; margin-top: 18px">
      <button
        v-for="t in ticks"
        :key="'c' + t"
        class="chip"
        :class="{ 'is-on': minutes === t }"
        type="button"
        @click="minutes = t"
      >
        {{ t }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.dial {
  position: relative;
  padding: 8px 0 4px;
}
.dial__ring {
  position: absolute;
  inset: 0 40px 70px;
  pointer-events: none;
}
.tick {
  position: absolute;
  left: 50%;
  top: 50%;
  width: 2px;
  height: 10px;
  background: rgba(126, 200, 195, 0.28);
  transform-origin: 0 86px;
}
.tick.is-on {
  background: var(--street-brass);
  height: 14px;
}
.dial__core {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
  min-height: 180px;
}
.dial__n {
  font-size: 88px;
  line-height: 0.9;
  font-weight: 600;
  text-align: center;
  letter-spacing: -0.04em;
}
.ghost {
  width: 40px;
  height: 40px;
  border: 1px solid var(--line);
  background: transparent;
  color: var(--mute-slate);
  font-size: 22px;
}
</style>
