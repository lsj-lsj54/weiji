<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import PageShell from '@/components/PageShell.vue'
import SegmentBar from '@/components/SegmentBar.vue'
import { errMsg } from '@/api/http'
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
import { useAuthGuard } from '@/composables/useAuthGuard'

useAuthGuard()

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

async function load() {
  error.value = ''
  try {
    ;[
      account.value,
      bill.value,
      board.value,
      evening.value,
      ledgers.value,
      rewards.value,
      items.value,
      badges.value,
      curve.value,
    ] = await Promise.all([
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
    error.value = errMsg(e, '积点加载失败')
  }
}

onShow(load)

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
    error.value = errMsg(e, '奖励没记下')
  }
}

async function redeem(id: number) {
  try {
    await redeemReward(id)
    notice.value = '已兑换'
    await load()
  } catch (e) {
    error.value = errMsg(e, '兑不了')
  }
}

async function give() {
  try {
    await donatePoints(donate.value)
    notice.value = `捐出 ${donate.value} 点`
    await load()
  } catch (e) {
    error.value = errMsg(e, '捐不出去')
  }
}

const maxDelay = () => Math.max(1, ...(curve.value?.points || [1]))
</script>

<template>
  <PageShell>
    <view class="kicker">积点</view>
    <view class="num" style="font-size: 56px; line-height: 1; font-weight: 600">{{ account?.balance ?? '—' }}</view>
    <view class="muted">连续 {{ account?.streakDays ?? 0 }} 天 · 累计 {{ account?.totalEarned ?? 0 }}</view>
    <view v-if="evening?.text" class="hint" style="margin-top: 8px">{{ evening.text }}</view>

    <SegmentBar
      v-model="tab"
      style="margin-top: 16px"
      :items="[
        { value: 'bill', label: '账单' },
        { value: 'shop', label: '兑换' },
        { value: 'flow', label: '流水' },
      ]"
    />
    <view v-if="notice" class="banner" style="margin-top: 12px">{{ notice }}</view>
    <view v-if="error" class="toast">{{ error }}</view>

    <view v-if="tab === 'bill'" style="margin-top: 16px">
      <view class="chips">
        <view class="chip" :class="{ 'is-on': range === 'day' }" @click="setRange('day')">日</view>
        <view class="chip" :class="{ 'is-on': range === 'week' }" @click="setRange('week')">周</view>
        <view class="chip" :class="{ 'is-on': range === 'month' }" @click="setRange('month')">月</view>
      </view>
      <view class="panel" style="margin-top: 12px">
        <view class="num" style="font-size: 36px">{{ bill?.totalMinutes ?? 0 }}</view>
        <view class="muted">分钟 · 约 {{ bill?.eveningsEquivalent ?? 0 }} 个晚上</view>
        <view class="muted">目标 {{ board?.totalMinutes ?? 0 }} / {{ board?.targetMinutes ?? 0 }}（{{ board?.progress ?? 0 }}%）</view>
      </view>
      <view class="kicker" style="margin-top: 16px">拖延改善 {{ curve?.completionRate ?? 0 }}%</view>
      <view v-if="curve?.points?.length" class="curve">
        <view
          v-for="(p, i) in curve.points"
          :key="i"
          class="curve__bar"
          :style="{ height: Math.max(4, (p / maxDelay()) * 48) + 'px' }"
        />
      </view>
      <view v-else class="hint">完成几条带延迟的任务后会出现刻度。</view>
    </view>

    <view v-else-if="tab === 'shop'" style="margin-top: 16px">
      <view v-if="items.length" class="hint">
        {{ items.map((it) => (ITEM_LABEL[it.itemCode] || it.itemCode) + ' ×' + it.quantity).join(' · ') }}
      </view>
      <view v-if="badges.length" class="hint">徽章：{{ badges.map((b) => b.name || b.badgeId).join('、') }}</view>
      <view v-for="r in rewards" :key="r.id" class="row panel" style="margin-top: 8px">
        <view>
          <view class="task-title">{{ r.name }}</view>
          <view class="muted">{{ r.pointCost }} 点 · {{ ITEM_LABEL[r.itemCode || 'CUSTOM'] }}</view>
        </view>
        <view class="btn btn--primary" @click="redeem(r.id)">兑换</view>
      </view>
      <view class="linkish" style="margin: 12px 0" @click="formOpen = !formOpen">挂一条奖励</view>
      <view v-if="formOpen" class="stack">
        <input v-model="rewardName" class="field" placeholder="奖励名" />
        <view class="chips">
          <view
            v-for="n in [10, 30, 80]"
            :key="n"
            class="chip"
            :class="{ 'is-on': rewardCost === n }"
            @click="rewardCost = n"
          >
            {{ n }} 点
          </view>
        </view>
        <view class="chips">
          <view
            v-for="code in ['CUSTOM', 'SKIP_CARD', 'DAY_EXEMPT']"
            :key="code"
            class="chip"
            :class="{ 'is-on': itemCode === code }"
            @click="itemCode = code"
          >
            {{ ITEM_LABEL[code] }}
          </view>
        </view>
        <view class="chips">
          <view
            v-for="lv in REWARD_LEVELS"
            :key="lv"
            class="chip"
            :class="{ 'is-on': rewardLevel === lv }"
            @click="rewardLevel = lv"
          >
            {{ lv }}
          </view>
        </view>
        <view class="chips">
          <view
            v-for="h in [0, 24, 72]"
            :key="h"
            class="chip"
            :class="{ 'is-on': cooldown === h }"
            @click="cooldown = h"
          >
            {{ h ? h + 'h 冷却' : '无冷却' }}
          </view>
        </view>
        <view class="btn" @click="addReward">挂上</view>
      </view>
      <view class="row" style="margin-top: 16px">
        <view class="chips">
          <view
            v-for="n in [5, 10, 20]"
            :key="'d' + n"
            class="chip"
            :class="{ 'is-on': donate === n }"
            @click="donate = n"
          >
            {{ n }}
          </view>
        </view>
        <view class="btn" @click="give">公益</view>
      </view>
    </view>

    <view v-else style="margin-top: 16px">
      <view v-if="!ledgers.length" class="hint">还没有入账。</view>
      <view v-for="l in ledgers" :key="l.id" class="row panel" style="margin-bottom: 8px">
        <view>
          <view>{{ l.remark || l.bizType }}</view>
          <view class="muted" style="font-size: 13px">{{ l.createdAt }}</view>
        </view>
        <view class="num num--accent">{{ l.changeAmount > 0 ? '+' : '' }}{{ l.changeAmount }}</view>
      </view>
    </view>
  </PageShell>
</template>

<style scoped>
.curve {
  display: flex;
  align-items: flex-end;
  gap: 3px;
  height: 48px;
}
.curve__bar {
  width: 6px;
  background: var(--minute-cyan);
}
</style>
