<script setup lang="ts">
import { onMounted, ref } from 'vue'
import AppShell from '@/components/AppShell.vue'
import { ApiError } from '@/api/http'
import {
  applyTemplate,
  bufferTask,
  completeTask,
  createTask,
  delayCurve,
  expireTasks,
  listTasks,
  listTemplates,
  skipTask,
} from '@/api/task'
import { createGoal, decomposeGoal, listGoals } from '@/api/user'
import {
  CATEGORIES,
  SCENES,
  TASK_STATUS,
  type DelayCurve,
  type TaskItem,
  type TaskTemplate,
  type UserGoal,
} from '@/api/types'

const tasks = ref<TaskItem[]>([])
const templates = ref<TaskTemplate[]>([])
const goals = ref<UserGoal[]>([])
const curve = ref<DelayCurve | null>(null)
const error = ref('')
const notice = ref('')
const filter = ref<number | undefined>(0)

const title = ref('')
const duration = ref(15)
const scene = ref('HOME')
const category = ref('学习')

const goalTitle = ref('')
const goalMinutes = ref(120)
const chunk = ref(15)

function msg(e: unknown, fallback: string) {
  return e instanceof ApiError ? e.message : fallback
}

async function load() {
  error.value = ''
  try {
    ;[tasks.value, templates.value, goals.value, curve.value] = await Promise.all([
      listTasks(filter.value),
      listTemplates(),
      listGoals(),
      delayCurve(),
    ])
  } catch (e) {
    error.value = msg(e, '任务加载失败')
  }
}

onMounted(load)

async function setFilter(next?: number) {
  filter.value = next
  await load()
}

async function add() {
  if (!title.value.trim()) return
  error.value = ''
  try {
    await createTask({
      title: title.value.trim(),
      durationMinutes: duration.value,
      sceneCode: scene.value,
      category: category.value,
      priority: 1,
    })
    title.value = ''
    notice.value = '已放进池里'
    await load()
  } catch (e) {
    error.value = msg(e, '加不进去')
  }
}

async function act(kind: 'buffer' | 'done' | 'skip' | 'expire', id: number) {
  error.value = ''
  try {
    if (kind === 'buffer') await bufferTask(id)
    if (kind === 'done') await completeTask(id, 0)
    if (kind === 'skip') await skipTask(id)
    if (kind === 'expire') await expireTasks([id])
    await load()
  } catch (e) {
    error.value = msg(e, '操作失败')
  }
}

async function apply(id: number) {
  error.value = ''
  try {
    await applyTemplate(id)
    notice.value = '模板已入池'
    await load()
  } catch (e) {
    error.value = msg(e, '套用失败')
  }
}

async function addGoal() {
  if (!goalTitle.value.trim()) return
  error.value = ''
  try {
    await createGoal({ title: goalTitle.value.trim(), targetMinutes: goalMinutes.value, status: 0 })
    goalTitle.value = ''
    await load()
  } catch (e) {
    error.value = msg(e, '目标没记下')
  }
}

async function split(id: number) {
  error.value = ''
  try {
    const pieces = await decomposeGoal(id, chunk.value)
    notice.value = `拆成 ${pieces.length} 条短任务`
    await load()
  } catch (e) {
    error.value = msg(e, '拆不开')
  }
}
</script>

<template>
  <AppShell>
    <main class="screen">
      <p class="kicker">任务池</p>
      <h2 class="task-title" style="font-size: 22px">把能拆开的事放进来</h2>
      <p v-if="curve" class="muted">完成率 {{ curve.completionRate }}%</p>

      <div class="chips" style="margin: 14px 0">
        <button class="chip" :class="{ 'is-on': filter === 0 }" type="button" @click="setFilter(0)">待做</button>
        <button class="chip" :class="{ 'is-on': filter === undefined }" type="button" @click="setFilter(undefined)">
          全部
        </button>
        <button class="chip" :class="{ 'is-on': filter === 2 }" type="button" @click="setFilter(2)">已完成</button>
      </div>

      <form class="stack" style="margin: 8px 0 20px" @submit.prevent="add">
        <input v-model="title" class="field" placeholder="例如：背 20 个词" />
        <div class="chips">
          <button
            v-for="n in [5, 10, 15, 20, 30]"
            :key="n"
            class="chip"
            :class="{ 'is-on': duration === n }"
            type="button"
            @click="duration = n"
          >
            {{ n }} 分
          </button>
        </div>
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
        <div class="chips">
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
        <button class="btn btn--primary" type="submit">放进池里</button>
      </form>

      <p class="kicker">模板</p>
      <p v-if="!templates.length" class="hint">还没有模板。广场收藏后会出现。</p>
      <div class="stack" style="margin-bottom: 20px">
        <article v-for="tpl in templates" :key="tpl.id" class="row panel">
          <div>
            <div class="task-title">{{ tpl.name }}</div>
            <div class="muted">{{ tpl.durationMinutes || 15 }} 分钟</div>
          </div>
          <button class="btn" type="button" @click="apply(tpl.id)">入池</button>
        </article>
      </div>

      <p class="kicker">大目标</p>
      <form class="stack" style="margin-bottom: 12px" @submit.prevent="addGoal">
        <input v-model="goalTitle" class="field" placeholder="例如：读完那本薄书" />
        <div class="chips">
          <button
            v-for="n in [60, 120, 300]"
            :key="n"
            class="chip"
            :class="{ 'is-on': goalMinutes === n }"
            type="button"
            @click="goalMinutes = n"
          >
            {{ n }} 分
          </button>
        </div>
        <button class="btn" type="submit">记下目标</button>
      </form>
      <div class="chips" style="margin-bottom: 10px">
        <span class="muted" style="align-self: center">拆成</span>
        <button
          v-for="n in [10, 15, 20]"
          :key="'ck' + n"
          class="chip"
          :class="{ 'is-on': chunk === n }"
          type="button"
          @click="chunk = n"
        >
          {{ n }} 分一块
        </button>
      </div>
      <article v-for="g in goals" :key="g.id" class="panel" style="margin-bottom: 8px">
        <h3 class="task-title">{{ g.title }}</h3>
        <p class="muted">{{ g.finishedMinutes || 0 }} / {{ g.targetMinutes || 0 }} 分钟</p>
        <button class="btn" type="button" @click="split(g.id)">拆进任务池</button>
      </article>

      <p v-if="notice" class="banner" style="margin: 12px 0">{{ notice }}</p>
      <p v-if="error" class="toast">{{ error }}</p>
      <p v-if="!tasks.length" class="banner">这一栏是空的。</p>

      <p class="kicker" style="margin-top: 18px">条目</p>
      <div class="stack">
        <article v-for="t in tasks" :key="t.id" class="panel">
          <h3 class="task-title">{{ t.title }}</h3>
          <p class="muted">
            {{ t.durationMinutes || '—' }} 分钟 · {{ TASK_STATUS[t.status ?? 0] }} ·
            {{ t.category || '未分类' }}
          </p>
          <div class="btn-row" style="margin-top: 10px">
            <button class="btn" type="button" @click="act('buffer', t.id)">先放一放</button>
            <button class="btn" type="button" @click="act('done', t.id)">记完成</button>
          </div>
          <div class="btn-row" style="margin-top: 6px">
            <button class="btn btn--ghost" type="button" @click="act('skip', t.id)">跳过</button>
            <button class="btn btn--ghost" type="button" @click="act('expire', t.id)">过期</button>
          </div>
        </article>
      </div>
    </main>
  </AppShell>
</template>
