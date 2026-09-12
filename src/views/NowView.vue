<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import AppShell from '@/components/AppShell.vue'
import FocusRing from '@/components/FocusRing.vue'
import IdleDial from '@/components/IdleDial.vue'
import { ApiError } from '@/api/http'
import { createTask, matchTasks, preloadTasks } from '@/api/task'
import { CATEGORIES, ENERGIES, SCENES, type FocusSession, type TaskItem } from '@/api/types'
import { useAuthStore } from '@/stores/auth'
import { useFocusStore } from '@/stores/focus'

const auth = useAuthStore()
const focus = useFocusStore()

const minutes = ref(15)
const scene = ref('HOME')
const category = ref('')
const filtersOpen = ref(false)
const matches = ref<TaskItem[]>([])
const error = ref('')
const notice = ref('')
const matching = ref(false)
const lastFinish = ref<FocusSession | null>(null)
const emptyTitle = ref('')
const remark = ref('')
const remarkOpen = ref(false)

const energy = computed(() => auth.preference?.energyStatus || 'ENERGETIC')
const energyLabel = computed(() => ENERGIES.find((e) => e.code === energy.value)?.label || '')
const primary = computed(() => matches.value[0] || null)

onMounted(async () => {
  if (!auth.user) {
    try {
      await auth.bootstrap()
    } catch {
      /* ignore */
    }
  }
  await focus.hydrate()
  if (!focus.session) {
    await preload()
  }
})

async function preload() {
  try {
    matches.value = await preloadTasks({ idleMinutes: minutes.value, sceneCode: scene.value })
  } catch {
    matches.value = []
  }
}

async function match() {
  error.value = ''
  notice.value = ''
  lastFinish.value = null
  matching.value = true
  try {
    matches.value = await matchTasks({
      idleMinutes: minutes.value,
      sceneCode: scene.value,
      category: category.value || undefined,
      energyStatus: energy.value,
    })
    if (!matches.value.length) {
      notice.value = '这段空闲塞不进现有待办。'
    }
  } catch (e) {
    matches.value = []
    error.value = e instanceof ApiError ? e.message : '匹配失败'
  } finally {
    matching.value = false
  }
}

async function addAndMatch() {
  if (!emptyTitle.value.trim()) return
  error.value = ''
  try {
    await createTask({
      title: emptyTitle.value.trim(),
      durationMinutes: minutes.value,
      sceneCode: scene.value,
      category: category.value || '生活',
      priority: 1,
    })
    emptyTitle.value = ''
    await match()
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : '加不进去'
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
    lastFinish.value = (await focus.finish(remark.value || undefined)) ?? null
    remark.value = ''
    matches.value = []
    await preload()
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
        <FocusRing :elapsed="focus.elapsedSeconds" :planned-minutes="focus.plannedMinutes" :live="focus.running" />
        <div class="btn-row">
          <button v-if="focus.running" class="btn" type="button" @click="focus.pause()">暂停</button>
          <button v-else class="btn" type="button" @click="focus.resume()">继续</button>
          <button class="btn btn--primary" type="button" @click="onFinish">入账</button>
        </div>
        <button class="linkish" style="margin-top: 12px" type="button" @click="remarkOpen = !remarkOpen">
          {{ remarkOpen ? '收起备注' : '加一句备注' }}
        </button>
        <input v-if="remarkOpen" v-model="remark" class="field" placeholder="可选" />
        <button class="btn btn--ghost" style="width: 100%; margin-top: 8px" type="button" @click="onAbandon">
          这段不算
        </button>
      </template>

      <template v-else>
        <IdleDial v-model="minutes" />
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
        <button class="linkish" style="margin: 12px 0" type="button" @click="filtersOpen = !filtersOpen">
          {{ filtersOpen ? '收起筛选' : '筛选类型' }}
        </button>
        <div v-if="filtersOpen" class="chips" style="margin-bottom: 12px">
          <button class="chip" :class="{ 'is-on': category === '' }" type="button" @click="category = ''">都可以</button>
          <button
            v-for="c in CATEGORIES"
            :key="c"
            class="chip"
            :class="{ 'is-on': category === c }"
            type="button"
            @click="category = c"
          >
            {{ c }}
          </button>
        </div>
        <button class="btn btn--primary" style="width: 100%" type="button" :disabled="matching" @click="match">
          找一条能塞进去的事
        </button>

        <div v-if="primary" class="panel" style="margin-top: 16px">
          <p class="kicker">匹配到</p>
          <h2 class="task-title">{{ primary.title }}</h2>
          <p class="muted">{{ primary.durationMinutes || '—' }} 分钟</p>
          <button class="btn btn--primary" style="width: 100%; margin-top: 12px" type="button" @click="startMatched(primary)">
            开始这段时间
          </button>
        </div>
        <button
          v-for="t in matches.slice(1)"
          :key="t.id"
          class="panel"
          style="text-align: left; width: 100%; margin-top: 8px"
          type="button"
          @click="startMatched(t)"
        >
          {{ t.title }} · {{ t.durationMinutes }} 分
        </button>

        <div v-if="notice || (!primary && !matching)" class="stack" style="margin-top: 16px">
          <p v-if="notice" class="banner">{{ notice }}</p>
          <input v-model="emptyTitle" class="field" placeholder="没有合适的？写一条短任务" />
          <button class="btn" type="button" @click="addAndMatch">放进池并匹配</button>
          <button class="btn btn--ghost" type="button" @click="startBare">空手计时</button>
        </div>
      </template>

      <p v-if="lastFinish" class="banner" style="margin-top: 16px">
        入账 {{ lastFinish.durationSeconds ? Math.floor(lastFinish.durationSeconds / 60) : 0 }} 分钟，
        {{ lastFinish.pointGranted || 0 }} 点。
        <router-link class="linkish" to="/points">去看积点</router-link>
      </p>
      <p v-if="error" class="banner banner--warn" style="margin-top: 12px">
        {{ error }}
        <button v-if="error.includes('休息日') || error.includes('免打扰')" class="linkish" type="button" @click="startBare">
          空手计时
        </button>
      </p>
    </main>
  </AppShell>
</template>
