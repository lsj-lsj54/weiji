<script setup lang="ts">
import { onMounted, ref } from 'vue'
import AppShell from '@/components/AppShell.vue'
import { ApiError } from '@/api/http'
import { bufferTask, completeTask, createTask, listTasks, skipTask } from '@/api/task'
import { CATEGORIES, SCENES, TASK_STATUS, type TaskItem } from '@/api/types'

const tasks = ref<TaskItem[]>([])
const error = ref('')
const title = ref('')
const duration = ref(15)
const scene = ref('HOME')
const category = ref('学习')

async function load() {
  error.value = ''
  try {
    tasks.value = await listTasks()
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : '任务加载失败'
  }
}

onMounted(load)

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
    await load()
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : '加不进去'
  }
}

async function act(kind: 'buffer' | 'done' | 'skip', id: number) {
  error.value = ''
  try {
    if (kind === 'buffer') await bufferTask(id)
    if (kind === 'done') await completeTask(id, 0)
    if (kind === 'skip') await skipTask(id)
    await load()
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : '操作失败'
  }
}
</script>

<template>
  <AppShell>
    <main class="screen">
      <p class="kicker">任务池</p>
      <h2 class="task-title" style="font-size: 22px">把能拆开的事放进来</h2>
      <p class="hint">时长要短过你手头的空闲，匹配才会选中它。</p>

      <form class="stack" style="margin: 20px 0" @submit.prevent="add">
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

      <p v-if="error" class="toast">{{ error }}</p>
      <p v-if="!tasks.length" class="banner">池子是空的。先放一条 15 分钟以内的。</p>

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
          <button class="btn btn--ghost" style="width: 100%; margin-top: 6px" type="button" @click="act('skip', t.id)">
            跳过（消耗跳过卡）
          </button>
        </article>
      </div>
    </main>
  </AppShell>
</template>
