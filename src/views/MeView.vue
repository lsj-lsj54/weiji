<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppShell from '@/components/AppShell.vue'
import { ApiError } from '@/api/http'
import { updatePreference, updateProfile } from '@/api/user'
import { ENERGIES, WEEKDAYS } from '@/api/types'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const router = useRouter()
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

onMounted(async () => {
  if (!auth.user) {
    try {
      await auth.bootstrap()
    } catch {
      /* ignore */
    }
  }
  nickname.value = auth.user?.nickname || ''
  energy.value = auth.preference?.energyStatus || 'ENERGETIC'
  restSet.value = parseRest(auth.preference?.restDays)
  dnd.value = auth.preference?.dndPeriods || ''
  daily.value = auth.preference?.dailyFocusMinutes || 30
  weekly.value = auth.preference?.weeklyFocusMinutes || 180
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
      dailyFocusMinutes: daily.value,
      weeklyFocusMinutes: weekly.value,
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
  await router.replace('/login')
}
</script>

<template>
  <AppShell>
    <main class="screen">
      <h2 class="task-title" style="font-size: 22px">{{ auth.user?.nickname || '未命名' }}</h2>
      <p class="muted">{{ auth.user?.phone }}</p>

      <div class="stack" style="margin: 20px 0">
        <router-link class="panel" to="/knowledge">知识点</router-link>
        <router-link class="panel" to="/social">一块过</router-link>
      </div>

      <form class="stack" @submit.prevent="save">
        <input v-model="nickname" class="field" placeholder="昵称" />
        <div class="chips">
          <button
            v-for="e in ENERGIES"
            :key="e.code"
            class="chip"
            :class="{ 'is-on': energy === e.code }"
            type="button"
            @click="energy = e.code"
          >
            {{ e.label }}
          </button>
        </div>
        <p class="kicker">休息日 · 周{{ restLabel }}</p>
        <div class="chips">
          <button
            v-for="d in WEEKDAYS"
            :key="d.v"
            class="chip"
            :class="{ 'is-on': restSet.includes(d.v) }"
            type="button"
            @click="toggleDay(d.v)"
          >
            {{ d.l }}
          </button>
        </div>
        <input v-model="dnd" class="field" placeholder="免打扰，空着则不限制。例 22:00-07:00" />
        <input v-model.number="daily" class="field" type="number" min="0" placeholder="每日分钟" />
        <input v-model.number="weekly" class="field" type="number" min="0" placeholder="每周分钟" />
        <button class="btn btn--primary" type="submit">保存</button>
      </form>
      <p v-if="saved" class="banner" style="margin-top: 12px">{{ saved }}</p>
      <p v-if="error" class="toast">{{ error }}</p>
      <button class="btn" style="margin-top: 24px; width: 100%" type="button" @click="out">退出</button>
    </main>
  </AppShell>
</template>
