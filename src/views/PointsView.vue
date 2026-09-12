<script setup lang="ts">
import { onMounted, ref } from 'vue'
import AppShell from '@/components/AppShell.vue'
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
import {
  ITEM_LABEL,
  REWARD_LEVELS,
  type PointAccount,
  type PointLedger,
  type RewardItem,
  type TimeBill,
  type UserBadge,
  type UserItem,
} from '@/api/types'

const account = ref<PointAccount | null>(null)
const bill = ref<TimeBill | null>(null)
const board = ref<TimeBill | null>(null)
const evening = ref<TimeBill | null>(null)
const ledgers = ref<PointLedger[]>([])
const rewards = ref<RewardItem[]>([])
const items = ref<UserItem[]>([])
const badges = ref<UserBadge[]>([])
const range = ref<'day' | 'week'>('day')
const error = ref('')
const notice = ref('')

const rewardName = ref('')
const rewardCost = ref(30)
const rewardLevel = ref('NORMAL')
const donate = ref(10)

function msg(e: unknown, fallback: string) {
  return e instanceof ApiError ? e.message : fallback
}

async function load() {
  error.value = ''
  try {
    ;[account.value, bill.value, board.value, evening.value, ledgers.value, rewards.value, items.value, badges.value] =
      await Promise.all([
        fetchAccount(),
        fetchTimeBill(range.value),
        fetchFocusBoard(range.value),
        fetchEveningSummary(),
        fetchLedgers(),
        listRewards(),
        listItems(),
        listBadges(),
      ])
  } catch (e) {
    error.value = msg(e, '积点加载失败')
  }
}

onMounted(load)

async function setRange(next: 'day' | 'week') {
  range.value = next
  await load()
}

async function addReward() {
  if (!rewardName.value.trim()) return
  error.value = ''
  try {
    await createReward({
      name: rewardName.value.trim(),
      pointCost: rewardCost.value,
      level: rewardLevel.value,
      itemCode: 'CUSTOM',
      lockMode: 1,
    })
    rewardName.value = ''
    notice.value = '奖励已挂上'
    await load()
  } catch (e) {
    error.value = msg(e, '奖励没记下')
  }
}

async function redeem(id: number) {
  error.value = ''
  try {
    await redeemReward(id)
    notice.value = '已兑换'
    await load()
  } catch (e) {
    error.value = msg(e, '兑不了')
  }
}

async function give() {
  error.value = ''
  try {
    await donatePoints(donate.value)
    notice.value = `捐出 ${donate.value} 点`
    await load()
  } catch (e) {
    error.value = msg(e, '捐不出去')
  }
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
        <p>{{ board?.totalMinutes ?? 0 }} / {{ board?.targetMinutes ?? 0 }} 分钟（{{ board?.progress ?? 0 }}%）</p>
      </section>

      <p v-if="evening?.text" class="banner" style="margin-top: 12px">{{ evening.text }}</p>

      <p class="kicker" style="margin-top: 22px">道具</p>
      <p v-if="!items.length" class="hint">还没有跳过卡或豁免。</p>
      <p v-for="it in items" :key="it.id" class="panel">
        {{ ITEM_LABEL[it.itemCode] || it.itemCode }} · {{ it.quantity }}
      </p>

      <p class="kicker" style="margin-top: 18px">徽章</p>
      <p v-if="!badges.length" class="hint">徽章会在通勤、拖延改善等节点自动发。</p>
      <p v-else class="muted">已拿到 {{ badges.length }} 枚。</p>

      <p class="kicker" style="margin-top: 18px">自建奖励</p>
      <form class="stack" @submit.prevent="addReward">
        <input v-model="rewardName" class="field" placeholder="例如：晚饭加份水果" />
        <div class="chips">
          <button
            v-for="n in [10, 30, 80]"
            :key="n"
            class="chip"
            :class="{ 'is-on': rewardCost === n }"
            type="button"
            @click="rewardCost = n"
          >
            {{ n }} 点
          </button>
        </div>
        <div class="chips">
          <button
            v-for="lv in REWARD_LEVELS"
            :key="lv"
            class="chip"
            :class="{ 'is-on': rewardLevel === lv }"
            type="button"
            @click="rewardLevel = lv"
          >
            {{ lv }}
          </button>
        </div>
        <button class="btn" type="submit">挂上奖励</button>
      </form>
      <article v-for="r in rewards" :key="r.id" class="row panel" style="margin-top: 8px">
        <div>
          <div class="task-title">{{ r.name }}</div>
          <div class="muted">{{ r.pointCost }} 点 · {{ r.level }}</div>
        </div>
        <button class="btn btn--primary" type="button" @click="redeem(r.id)">兑换</button>
      </article>

      <p class="kicker" style="margin-top: 18px">公益</p>
      <div class="row">
        <div class="chips">
          <button
            v-for="n in [5, 10, 20]"
            :key="'d' + n"
            class="chip"
            :class="{ 'is-on': donate === n }"
            type="button"
            @click="donate = n"
          >
            {{ n }}
          </button>
        </div>
        <button class="btn" type="button" @click="give">捐出</button>
      </div>

      <p v-if="notice" class="banner" style="margin-top: 12px">{{ notice }}</p>
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
