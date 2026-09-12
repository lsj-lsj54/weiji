import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import {
  abandonFocus,
  finishFocus,
  liveFocus,
  pauseFocus,
  resumeFocus,
  startFocus,
} from '@/api/focus'
import type { FocusSession, TaskItem } from '@/api/types'

export const useFocusStore = defineStore('focus', () => {
  const session = ref<FocusSession | null>(null)
  const plannedMinutes = ref(25)
  const now = ref(Date.now())
  let timer: number | undefined

  const running = computed(() => session.value?.status === 0)
  const paused = computed(() => session.value?.status === 1)

  const elapsedSeconds = computed(() => {
    const s = session.value
    if (!s?.clientStartTs) return 0
    const pausedSec = s.pausedSeconds ?? 0
    if (s.status === 1 && s.lastPauseTs) {
      return Math.max(0, Math.floor((s.lastPauseTs - s.clientStartTs) / 1000) - pausedSec)
    }
    return Math.max(0, Math.floor((now.value - s.clientStartTs) / 1000) - pausedSec)
  })

  function tick() {
    now.value = Date.now()
  }

  function startClock() {
    stopClock()
    timer = window.setInterval(tick, 250)
  }

  function stopClock() {
    if (timer) window.clearInterval(timer)
    timer = undefined
  }

  async function hydrate() {
    session.value = await liveFocus()
    if (session.value && (session.value.status === 0 || session.value.status === 1)) {
      startClock()
    } else {
      stopClock()
    }
  }

  async function startFromTask(task: TaskItem, sourceType: 'MATCH' | 'FOCUS') {
    session.value = await startFocus({
      taskId: task.id,
      goalId: task.goalId,
      title: task.title,
      category: task.category,
      clientStartTs: Date.now(),
      sourceType,
    })
    plannedMinutes.value = task.durationMinutes || 25
    startClock()
  }

  async function startFree(title: string, minutesHint: number) {
    session.value = await startFocus({
      title,
      clientStartTs: Date.now(),
      sourceType: 'FOCUS',
    })
    plannedMinutes.value = minutesHint
    startClock()
  }

  async function pause() {
    if (!session.value) return
    session.value = await pauseFocus(session.value.id)
  }

  async function resume() {
    if (!session.value) return
    session.value = await resumeFocus(session.value.id)
  }

  async function finish(remark?: string) {
    if (!session.value) return
    const done = await finishFocus(session.value.id, Date.now(), remark)
    stopClock()
    session.value = null
    return done
  }

  async function abandon() {
    if (!session.value) return
    await abandonFocus(session.value.id)
    stopClock()
    session.value = null
  }

  return {
    session,
    plannedMinutes,
    running,
    paused,
    elapsedSeconds,
    hydrate,
    startFromTask,
    startFree,
    pause,
    resume,
    finish,
    abandon,
  }
})
