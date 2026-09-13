<script setup lang="ts">
import { computed, ref } from 'vue'
import { onHide, onShow, onUnload } from '@dcloudio/uni-app'
import FocusRing from '@/components/FocusRing.vue'
import IdleDial from '@/components/IdleDial.vue'
import PageShell from '@/components/PageShell.vue'
import { ApiError } from '@/api/http'
import { createTask, matchTasks, preloadTasks } from '@/api/task'
import { CATEGORIES, ENERGIES, SCENES, type FocusSession, type TaskItem } from '@/api/types'
import { useAuthGuard } from '@/composables/useAuthGuard'
import { useAuthStore } from '@/stores/auth'
import { useFocusStore } from '@/stores/focus'

useAuthGuard()

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

onShow(async () => {
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

onHide(() => focus.stopClock())
onUnload(() => focus.stopClock())

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

function goPoints() {
  uni.switchTab({ url: '/pages/points/points' })
}

function goMe() {
  uni.switchTab({ url: '/pages/me/me' })
}
</script>

<template>
  <PageShell :energy="energyLabel">
    <view v-if="focus.session">
      <view class="kicker">{{ focus.session.title }}</view>
      <FocusRing :elapsed="focus.elapsedSeconds" :planned-minutes="focus.plannedMinutes" :live="focus.running" />
      <view class="btn-row">
        <view v-if="focus.running" class="btn" @click="focus.pause()">暂停</view>
        <view v-else class="btn" @click="focus.resume()">继续</view>
        <view class="btn btn--primary" @click="onFinish">入账</view>
      </view>
      <view class="linkish" style="margin-top: 12px" @click="remarkOpen = !remarkOpen">
        {{ remarkOpen ? '收起备注' : '加一句备注' }}
      </view>
      <input v-if="remarkOpen" v-model="remark" class="field" placeholder="可选" />
      <view class="btn btn--ghost" style="margin-top: 8px" @click="onAbandon">这段不算</view>
    </view>

    <view v-else>
      <IdleDial v-model="minutes" />
      <view class="chips">
        <view
          v-for="s in SCENES"
          :key="s.code"
          class="chip"
          :class="{ 'is-on': scene === s.code }"
          @click="scene = s.code"
        >
          {{ s.label }}
        </view>
      </view>
      <view class="linkish" style="margin: 12px 0" @click="filtersOpen = !filtersOpen">
        {{ filtersOpen ? '收起筛选' : '筛选类型' }}
      </view>
      <view v-if="filtersOpen" class="chips" style="margin-bottom: 12px">
        <view class="chip" :class="{ 'is-on': category === '' }" @click="category = ''">都可以</view>
        <view
          v-for="c in CATEGORIES"
          :key="c"
          class="chip"
          :class="{ 'is-on': category === c }"
          @click="category = c"
        >
          {{ c }}
        </view>
      </view>
      <view class="btn btn--primary" :class="{ 'is-disabled': matching }" @click="match">找一条能塞进去的事</view>

      <view v-if="primary" class="panel" style="margin-top: 16px">
        <view class="kicker">匹配到</view>
        <view class="task-title">{{ primary.title }}</view>
        <view class="muted">{{ primary.durationMinutes || '—' }} 分钟</view>
        <view class="btn btn--primary" style="margin-top: 12px" @click="startMatched(primary)">开始这段时间</view>
      </view>
      <view
        v-for="t in matches.slice(1)"
        :key="t.id"
        class="panel"
        style="margin-top: 8px"
        @click="startMatched(t)"
      >
        {{ t.title }} · {{ t.durationMinutes }} 分
      </view>

      <view v-if="notice || (!primary && !matching)" class="stack" style="margin-top: 16px">
        <view v-if="notice" class="banner">{{ notice }}</view>
        <input v-model="emptyTitle" class="field" placeholder="没有合适的？写一条短任务" />
        <view class="btn" @click="addAndMatch">放进池并匹配</view>
        <view class="btn btn--ghost" @click="startBare">空手计时</view>
      </view>
    </view>

    <view v-if="lastFinish" class="banner" style="margin-top: 16px">
      入账 {{ lastFinish.durationSeconds ? Math.floor(lastFinish.durationSeconds / 60) : 0 }} 分钟，
      {{ lastFinish.pointGranted || 0 }} 点。
      <text class="linkish" @click="goPoints">去看积点</text>
    </view>
    <view v-if="error" class="banner banner--warn" style="margin-top: 12px">
      {{ error }}
      <text v-if="error.includes('免打扰') || error.includes('休息日')" class="linkish" @click="goMe">去偏好里改</text>
      <text v-if="error.includes('免打扰') || error.includes('休息日')" class="linkish" @click="startBare">空手计时</text>
    </view>
  </PageShell>
</template>
