<script setup lang="ts">
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import PageShell from '@/components/PageShell.vue'
import { ApiError } from '@/api/http'
import { updatePreference, updateProfile } from '@/api/user'
import { ENERGIES, WEEKDAYS } from '@/api/types'
import { useAuthGuard } from '@/composables/useAuthGuard'
import { useAuthStore } from '@/stores/auth'

useAuthGuard()

const auth = useAuthStore()
const nickname = ref('')
const energy = ref('ENERGETIC')
const restSet = ref<number[]>([])
const dnd = ref('')
const daily = ref(30)
const weekly = ref(180)
const error = ref('')
const saved = ref('')

const restLabel = computed(() =>
  restSet.value.length ? restSet.value.map((v) => WEEKDAYS.find((d) => d.v === v)?.l).join('') : '无',
)

function parseRest(raw?: string) {
  if (!raw) return []
  try {
    const arr = JSON.parse(raw) as unknown
    if (Array.isArray(arr)) return arr.map(Number).filter((n) => n >= 0 && n <= 6)
  } catch {
    return []
  }
  return []
}

function toggleDay(v: number) {
  restSet.value = restSet.value.includes(v) ? restSet.value.filter((x) => x !== v) : [...restSet.value, v].sort()
}

function fillForm() {
  nickname.value = auth.user?.nickname || ''
  energy.value = auth.preference?.energyStatus || 'ENERGETIC'
  restSet.value = parseRest(auth.preference?.restDays)
  dnd.value = auth.preference?.dndPeriods || ''
  daily.value = auth.preference?.dailyFocusMinutes || 30
  weekly.value = auth.preference?.weeklyFocusMinutes || 180
}

onShow(async () => {
  if (!auth.user) {
    try {
      await auth.bootstrap()
    } catch {
      /* ignore */
    }
  }
  fillForm()
})

async function save() {
  error.value = ''
  saved.value = ''
  try {
    await updateProfile({ nickname: nickname.value.trim() })
    const pref = await updatePreference({
      energyStatus: energy.value,
      restDays: JSON.stringify(restSet.value),
      dndPeriods: dnd.value.trim(),
      dailyFocusMinutes: Number(daily.value) || 0,
      weeklyFocusMinutes: Number(weekly.value) || 0,
    })
    auth.setPreference(pref)
    if (auth.user) auth.user.nickname = nickname.value.trim()
    saved.value = '已记下'
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : '保存失败'
  }
}

async function out() {
  await auth.logout()
  uni.reLaunch({ url: '/pages/login/login' })
}
</script>

<template>
  <PageShell>
    <view class="task-title" style="font-size: 22px">{{ auth.user?.nickname || '未命名' }}</view>
    <view class="muted">{{ auth.user?.phone }}</view>

    <view class="stack" style="margin: 20px 0">
      <view class="panel" @click="uni.navigateTo({ url: '/pages/knowledge/knowledge' })">知识点</view>
      <view class="panel" @click="uni.navigateTo({ url: '/pages/social/social' })">一块过</view>
    </view>

    <view class="stack">
      <input v-model="nickname" class="field" placeholder="昵称" />
      <view class="chips">
        <view
          v-for="e in ENERGIES"
          :key="e.code"
          class="chip"
          :class="{ 'is-on': energy === e.code }"
          @click="energy = e.code"
        >
          {{ e.label }}
        </view>
      </view>
      <view class="kicker">休息日 · 周{{ restLabel }}</view>
      <view class="chips">
        <view
          v-for="d in WEEKDAYS"
          :key="d.v"
          class="chip"
          :class="{ 'is-on': restSet.includes(d.v) }"
          @click="toggleDay(d.v)"
        >
          {{ d.l }}
        </view>
      </view>
      <input v-model="dnd" class="field" placeholder="免打扰，空着则不限制。例 22:00-07:00" />
      <input v-model="daily" class="field" type="number" placeholder="每日分钟" />
      <input v-model="weekly" class="field" type="number" placeholder="每周分钟" />
      <view class="btn btn--primary" @click="save">保存</view>
    </view>
    <view v-if="saved" class="banner" style="margin-top: 12px">{{ saved }}</view>
    <view v-if="error" class="toast">{{ error }}</view>
    <view class="btn" style="margin-top: 24px" @click="out">退出</view>
  </PageShell>
</template>
