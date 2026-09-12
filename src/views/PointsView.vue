<script setup lang="ts">
import { onMounted, ref } from 'vue'
import AppShell from '@/components/AppShell.vue'
import SegmentBar from '@/components/SegmentBar.vue'
import { ApiError } from '@/api/http'
import {
  createReward,
  donatePoints,
  fetchAccount,
  fetchEveningSummary,
  fetchFocusBoard,
  fetchLedgers,
  fetchTimeBill,
  listBadges,
  listItems,
  listRewards,
  redeemReward,
} from '@/api/point'
import { delayCurve } from '@/api/task'
import {
  ITEM_LABEL,
  REWARD_LEVELS,
  type DelayCurve,
  type PointAccount,
  type PointLedger,
  type RewardItem,
  type TimeBill,
  type UserBadge,
  type UserItem,
} from '@/api/types'

const tab = ref('bill')
const account = ref<PointAccount | null>(null)
const bill = ref<TimeBill | null>(null)
const board = ref<TimeBill | null>(null)
const evening = ref<TimeBill | null>(null)
const ledgers = ref<PointLedger[]>([])
const rewards = ref<RewardItem[]>([])
const items = ref<UserItem[]>([])
const badges = ref<UserBadge[]>([])
const curve = ref<DelayCurve | null>(null)
const range = ref<'day' | 'week' | 'month'>('day')
const error = ref('')
const notice = ref('')

const rewardName = ref('')
const rewardCost = ref(30)
const rewardLevel = ref('NORMAL')
const itemCode = ref('CUSTOM')
const cooldown = ref(0)
const donate = ref(10)
const formOpen = ref(false)

function msg(e: unknown, fallback: string) {
  return e instanceof ApiError ? e.message : fallback
}

async function load() {
  error.value = ''
  try {
    ;[account.value, bill.value, board.value, evening.value, ledgers.value, rewards.value, items.value, badges.value, curve.value] =
      await Promise.all([
        fetchAccount(),
        fetchTimeBill(range.value),
        fetchFocusBoard(range.value === 'month' ? 'week' : range.value),
        fetchEveningSummary(),
        fetchLedgers(),
        listRewards(),
        listItems(),
        listBadges(),
        delayCurve(),
      ])
  } catch (e) {
    error.value = msg(e, '积点加载失败')
  }
}

onMounted(load)

async function setRange(next: 'day' | 'week' | 'month') {
  range.value = next
  await load()
}

async function addReward() {
  if (!rewardName.value.trim()) return
  try {
    await createReward({
      name: rewardName.value.trim(),
      pointCost: rewardCost.value,
      level: rewardLevel.value,
      itemCode: itemCode.value,
      lockMode: 1,
      cooldownHours: cooldown.value || undefined,
    })
    rewardName.value = ''
    notice.value = '已挂上'
    await load()
  } catch (e) {
    error.value = msg(e, '奖励没记下')
  }
}

async function redeem(id: number) {
  try {
    await redeemReward(id)
    notice.value = '已兑换'
    await load()
  } catch (e) {
    error.value = msg(e, '兑不了')
  }
}

async function give() {
  try {
    await donatePoints(donate.value)
    notice.value = `捐出 ${donate.value} 点`
    await load()
  } catch (e) {
    error.value = msg(e, '捐不出去')
  }
}

const maxDelay = () => Math.max(1, ...(curve.value?.points || [1]))
</script>

<template>
  <AppShell>
    <main class="screen">
      <p class="kicker">积点</p>
      <div class="num" style="font-size: 56px; line-height: 1; font-weight: 600">{{ account?.balance ?? '—' }}</div>
      <p class="muted">连续 {{ account?.streakDays ?? 0 }} 天 · 累计 {{ account?.totalEarned ?? 0 }}</p>
      <p v-if="evening?.text" class="hint" style="margin-top: 8px">{{ evening.text }}</p>

      <SegmentBar
        v-model="tab"
        style="margin-top: 16px"
        :items="[{ value: 'bill', label: '账单' }, { value: 'shop', label: '兑换' }, { value: 'flow', label: '流水' }]"
      />
      <p v-if="notice" class="banner" style="margin-top: 12px">{{ notice }}</p>
      <p v-if="error" class="toast">{{ error }}</p>

      <section v-if="tab === 'bill'" style="margin-top: 16px">
        <div class="chips">
          <button class="chip" :class="{ 'is-on': range === 'day' }" type="button" @click="setRange('day')">日</button>
          <button class="chip" :class="{ 'is-on': range === 'week' }" type="button" @click="setRange('week')">周</button>
          <button class="chip" :class="{ 'is-on': range === 'month' }" type="button" @click="setRange('month')">月</button>
        </div>
        <div class="panel" style="margin-top: 12px">
          <p class="num" style="font-size: 36px; margin: 0">{{ bill?.totalMinutes ?? 0 }}</p>
          <p class="muted">分钟 · 约 {{ bill?.eveningsEquivalent ?? 0 }} 个晚上</p>
          <p class="muted">目标 {{ board?.totalMinutes ?? 0 }} / {{ board?.targetMinutes ?? 0 }}（{{ board?.progress ?? 0 }}%）</p>
        </div>
        <p class="kicker" style="margin-top: 16px">拖延改善 {{ curve?.completionRate ?? 0 }}%</p>
        <div v-if="curve?.points?.length" style="display: flex; align-items: flex-end; gap: 3px; height: 48px">
          <span
            v-for="(p, i) in curve.points"
            :key="i"
            :style="{ display: 'block', width: '6px', height: Math.max(4, (p / maxDelay()) * 48) + 'px', background: 'var(--minute-cyan)' }"
          />
        </div>
        <p v-else class="hint">完成几条带延迟的任务后会出现刻度。</p>
      </section>

      <section v-else-if="tab === 'shop'" style="margin-top: 16px">
        <p v-if="items.length" class="hint">{{ items.map((it) => (ITEM_LABEL[it.itemCode] || it.itemCode) + ' ×' + it.quantity).join(' · ') }}</p>
        <p v-if="badges.length" class="hint">徽章：{{ badges.map((b) => b.name || b.badgeId).join('、') }}</p>
        <article v-for="r in rewards" :key="r.id" class="row panel" style="margin-top: 8px">
          <div>
            <div class="task-title">{{ r.name }}</div>
            <div class="muted">{{ r.pointCost }} 点 · {{ ITEM_LABEL[r.itemCode || 'CUSTOM'] }}</div>
          </div>
          <button class="btn btn--primary" type="button" @click="redeem(r.id)">兑换</button>
        </article>
        <button class="linkish" style="margin: 12px 0" type="button" @click="formOpen = !formOpen">挂一条奖励</button>
        <form v-if="formOpen" class="stack" @submit.prevent="addReward">
          <input v-model="rewardName" class="field" placeholder="奖励名" />
          <div class="chips">
            <button v-for="n in [10, 30, 80]" :key="n" class="chip" :class="{ 'is-on': rewardCost === n }" type="button" @click="rewardCost = n">
              {{ n }} 点
            </button>
          </div>
          <div class="chips">
            <button v-for="code in ['CUSTOM', 'SKIP_CARD', 'DAY_EXEMPT']" :key="code" class="chip" :class="{ 'is-on': itemCode === code }" type="button" @click="itemCode = code">
              {{ ITEM_LABEL[code] }}
            </button>
          </div>
          <div class="chips">
            <button v-for="lv in REWARD_LEVELS" :key="lv" class="chip" :class="{ 'is-on': rewardLevel === lv }" type="button" @click="rewardLevel = lv">
              {{ lv }}
            </button>
          </div>
          <div class="chips">
            <button v-for="h in [0, 24, 72]" :key="h" class="chip" :class="{ 'is-on': cooldown === h }" type="button" @click="cooldown = h">
              {{ h ? h + 'h 冷却' : '无冷却' }}
            </button>
          </div>
          <button class="btn" type="submit">挂上</button>
        </form>
        <div class="row" style="margin-top: 16px">
          <div class="chips">
            <button v-for="n in [5, 10, 20]" :key="'d' + n" class="chip" :class="{ 'is-on': donate === n }" type="button" @click="donate = n">
              {{ n }}
            </button>
          </div>
          <button class="btn" type="button" @click="give">公益</button>
        </div>
      </section>

      <section v-else style="margin-top: 16px">
        <p v-if="!ledgers.length" class="hint">还没有入账。</p>
        <article v-for="l in ledgers" :key="l.id" class="row panel" style="margin-bottom: 8px">
          <div>
            <div>{{ l.remark || l.bizType }}</div>
            <div class="muted" style="font-size: 13px">{{ l.createdAt }}</div>
          </div>
          <div class="num num--accent">{{ l.changeAmount > 0 ? '+' : '' }}{{ l.changeAmount }}</div>
        </article>
      </section>
    </main>
  </AppShell>
</template>
