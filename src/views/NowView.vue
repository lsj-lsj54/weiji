<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import AppShell from '@/components/AppShell.vue'
import FocusRing from '@/components/FocusRing.vue'
import IdleDial from '@/components/IdleDial.vue'
import { ApiError } from '@/api/http'
import { matchTasks } from '@/api/task'
import { ENERGIES, SCENES, type FocusSession, type TaskItem } from '@/api/types'
import { useAuthStore } from '@/stores/auth'
import { useFocusStore } from '@/stores/focus'

const auth = useAuthStore()
const focus = useFocusStore()

const minutes = ref(15)
const scene = ref('HOME')
const energy = ref(auth.preference?.energyStatus || 'ENERGETIC')
const matches = ref<TaskItem[]>([])
const error = ref('')
const notice = ref('')
const matching = ref(false)
const lastFinish = ref<FocusSession | null>(null)

const energyLabel = computed(() => ENERGIES.find((e) => e.code === energy.value)?.label || '此刻')
const primary = computed(() => matches.value[0] || null)
const rest = computed(() => matches.value.slice(1))
const planned = computed(() => {
  if (primary.value?.durationMinutes) return primary.value.durationMinutes
  return minutes.value
})

onMounted(async () => {
  if (!auth.user) {
    try {
      await auth.bootstrap()
    } catch {
      /* token may be stale; router will bounce on next 401 */
    }
  }
  energy.value = auth.preference?.energyStatus || 'ENERGETIC'
  await focus.hydrate()
})

async function match() {
  error.value = ''
  notice.value = ''
  lastFinish.value = null
  matching.value = true
  try {
    matches.value = await matchTasks({
      idleMinutes: minutes.value,
      sceneCode: scene.value,
      energyStatus: energy.value,
    })
    if (!matches.value.length) {
      notice.value = '这段空闲塞不进现有待办。去任务池加一条短的，或直接空手计时。'
    }
  } catch (e) {
    matches.value = []
    error.value = e instanceof ApiError ? e.message : '匹配失败'
  } finally {
    matching.value = false
  }
}

async function startMatched(task: TaskItem) {
  error.value = ''
  try {
    await focus.startFromTask(task, 'MATCH')
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : '没法开始'
  }
}

async function startBare() {
  error.value = ''
  try {
    await focus.startFree('空闲专注', minutes.value)
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : '没法开始'
  }
}

async function onFinish() {
  error.value = ''
  try {
    lastFinish.value = (await focus.finish()) ?? null
    matches.value = []
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : '没法入账'
  }
}

async function onAbandon() {
  error.value = ''
  try {
    await focus.abandon()
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : '没法结束'
  }
}
</script>

<template>
  <AppShell :energy="energyLabel">
    <main class="screen">
      <template v-if="focus.session">
        <p class="kicker">{{ focus.session.title }}</p>
        <FocusRing :elapsed="focus.elapsedSeconds" :planned-minutes="planned" :live="focus.running" />
        <div class="btn-row">
          <button v-if="focus.running" class="btn" type="button" @click="focus.pause()">暂停</button>
          <button v-else class="btn" type="button" @click="focus.resume()">继续</button>
          <button class="btn btn--primary" type="button" @click="onFinish">入账</button>
        </div>
        <button class="btn btn--ghost" style="width: 100%; margin-top: 8px" type="button" @click="onAbandon">
          这段不算
        </button>
      </template>

      <template v-else>
        <IdleDial v-model="minutes" />

        <p class="kicker">你在哪</p>
        <div class="chips">
          <button
            v-for="s in SCENES"
            :key="s.code"
            class="chip"
            :class="{ 'is-on': scene === s.code }"
            type="button"
            @click="scene = s.code"
          >
            {{ s.label }}
          </button>
        </div>

        <p class="kicker" style="margin-top: 18px">精力</p>
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

        <button class="btn btn--primary" style="width: 100%; margin-top: 22px" type="button" :disabled="matching" @click="match">
          找一条能塞进去的事
        </button>

        <div v-if="primary" class="panel" style="margin-top: 16px">
          <p class="kicker">匹配到</p>
          <h2 class="task-title">{{ primary.title }}</h2>
          <p class="muted">{{ primary.durationMinutes || '—' }} 分钟 · {{ primary.category || '未分类' }}</p>
          <button class="btn btn--primary" style="width: 100%; margin-top: 12px" type="button" @click="startMatched(primary)">
            开始这段时间
          </button>
        </div>

        <div v-if="rest.length" class="stack" style="margin-top: 10px">
          <button
            v-for="t in rest"
            :key="t.id"
            class="panel"
            style="text-align: left; width: 100%"
            type="button"
            @click="startMatched(t)"
          >
            <div class="task-title">{{ t.title }}</div>
            <div class="muted">还能塞进 · {{ t.durationMinutes }} 分钟</div>
          </button>
        </div>

        <p v-if="notice" class="banner" style="margin-top: 16px">
          {{ notice }}
          <router-link class="linkish" to="/tasks">去任务池</router-link>
        </p>
        <button v-if="notice" class="btn" style="width: 100%; margin-top: 10px" type="button" @click="startBare">
          空手计时
        </button>
      </template>

      <p v-if="lastFinish" class="banner" style="margin-top: 16px">
        入账 {{ lastFinish.durationSeconds ? Math.floor(lastFinish.durationSeconds / 60) : 0 }} 分钟，
        {{ lastFinish.pointGranted || 0 }} 点。
      </p>
      <p v-if="error" class="banner banner--warn" style="margin-top: 12px">{{ error }}</p>
    </main>
  </AppShell>
</template>
