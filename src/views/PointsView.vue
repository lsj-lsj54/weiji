<script setup lang="ts">
import { onMounted, ref } from 'vue'
import AppShell from '@/components/AppShell.vue'
import { ApiError } from '@/api/http'
import { fetchAccount, fetchEveningSummary, fetchFocusBoard, fetchLedgers, fetchTimeBill } from '@/api/point'
import type { PointAccount, PointLedger, TimeBill } from '@/api/types'

const account = ref<PointAccount | null>(null)
const bill = ref<TimeBill | null>(null)
const board = ref<TimeBill | null>(null)
const evening = ref<TimeBill | null>(null)
const ledgers = ref<PointLedger[]>([])
const range = ref<'day' | 'week'>('day')
const error = ref('')

async function load() {
  error.value = ''
  try {
    ;[account.value, bill.value, board.value, evening.value, ledgers.value] = await Promise.all([
      fetchAccount(),
      fetchTimeBill(range.value),
      fetchFocusBoard(range.value),
      fetchEveningSummary(),
      fetchLedgers(),
    ])
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : '积点加载失败'
  }
}

onMounted(load)

async function setRange(next: 'day' | 'week') {
  range.value = next
  await load()
}
</script>

<template>
  <AppShell>
    <main class="screen">
      <p class="kicker">积点</p>
      <div class="num" style="font-size: 64px; line-height: 1; font-weight: 600">
        {{ account?.balance ?? '—' }}
      </div>
      <p class="muted">余额 · 连续 {{ account?.streakDays ?? 0 }} 天 · 累计 {{ account?.totalEarned ?? 0 }}</p>

      <div class="chips" style="margin: 18px 0 12px">
        <button class="chip" :class="{ 'is-on': range === 'day' }" type="button" @click="setRange('day')">今天</button>
        <button class="chip" :class="{ 'is-on': range === 'week' }" type="button" @click="setRange('week')">本周</button>
      </div>

      <section class="panel">
        <p class="kicker">时间账单</p>
        <p class="num" style="font-size: 36px; margin: 0">{{ bill?.totalMinutes ?? 0 }}</p>
        <p class="muted">专注分钟 · 约 {{ bill?.eveningsEquivalent ?? 0 }} 个完整晚上</p>
      </section>

      <section class="panel" style="margin-top: 10px">
        <p class="kicker">目标进度</p>
        <p>
          {{ board?.totalMinutes ?? 0 }} / {{ board?.targetMinutes ?? 0 }} 分钟
          （{{ board?.progress ?? 0 }}%）
        </p>
      </section>

      <p v-if="evening?.text" class="banner" style="margin-top: 12px">{{ evening.text }}</p>
      <p v-if="error" class="toast">{{ error }}</p>

      <p class="kicker" style="margin-top: 22px">流水</p>
      <p v-if="!ledgers.length" class="hint">还没有入账。去此刻走完一次专注。</p>
      <article v-for="l in ledgers" :key="l.id" class="row panel" style="margin-bottom: 8px">
        <div>
          <div>{{ l.remark || l.bizType }}</div>
          <div class="muted" style="font-size: 13px">{{ l.createdAt }}</div>
        </div>
        <div class="num" style="color: var(--minute-cyan)">{{ l.changeAmount > 0 ? '+' : '' }}{{ l.changeAmount }}</div>
      </article>
    </main>
  </AppShell>
</template>
