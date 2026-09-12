<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppShell from '@/components/AppShell.vue'
import { ApiError } from '@/api/http'
import { updatePreference, updateProfile } from '@/api/user'
import { ENERGIES } from '@/api/types'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const router = useRouter()
const nickname = ref('')
const energy = ref('ENERGETIC')
const restDays = ref('[0]')
const dnd = ref('22:00-07:00')
const daily = ref(30)
const weekly = ref(180)
const error = ref('')
const saved = ref('')

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
  restDays.value = auth.preference?.restDays || '[0]'
  dnd.value = auth.preference?.dndPeriods || '22:00-07:00'
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
      restDays: restDays.value,
      dndPeriods: dnd.value,
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
      <p class="kicker">我</p>
      <h2 class="task-title" style="font-size: 22px">{{ auth.user?.nickname || '未命名' }}</h2>
      <p class="muted">{{ auth.user?.phone }}</p>

      <form class="stack" style="margin-top: 24px" @submit.prevent="save">
        <input v-model="nickname" class="field" placeholder="昵称" />
        <p class="kicker">默认精力</p>
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
        <label class="hint">休息日 JSON，如 [0] 表示周日</label>
        <input v-model="restDays" class="field" />
        <label class="hint">免打扰，例如 22:00-07:00</label>
        <input v-model="dnd" class="field" />
        <label class="hint">每日专注目标（分钟）</label>
        <input v-model.number="daily" class="field" type="number" min="0" />
        <label class="hint">每周专注目标（分钟）</label>
        <input v-model.number="weekly" class="field" type="number" min="0" />
        <button class="btn btn--primary" type="submit">保存偏好</button>
      </form>
      <p v-if="saved" class="banner" style="margin-top: 12px">{{ saved }}</p>
      <p v-if="error" class="toast">{{ error }}</p>

      <div class="stack" style="margin-top: 28px">
        <router-link class="panel" to="/tasks">任务池</router-link>
        <router-link class="panel" to="/knowledge">知识点（轻量）</router-link>
        <router-link class="panel" to="/social">本周榜（轻量）</router-link>
        <button class="btn" type="button" @click="out">退出</button>
      </div>
    </main>
  </AppShell>
</template>
