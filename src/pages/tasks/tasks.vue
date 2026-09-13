<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import PageShell from '@/components/PageShell.vue'
import SegmentBar from '@/components/SegmentBar.vue'
import { errMsg } from '@/api/http'
import {
  applyTemplate,
  bufferTask,
  completeTask,
  createTask,
  expireTasks,
  importTasks,
  listTasks,
  listTemplates,
  saveTemplate,
  skipTask,
  updateTask,
} from '@/api/task'
import { createGoal, decomposeGoal, listGoals } from '@/api/user'
import { CATEGORIES, SCENES, TASK_STATUS, type TaskItem, type TaskTemplate, type UserGoal } from '@/api/types'
import { useAuthGuard } from '@/composables/useAuthGuard'

useAuthGuard()

const tab = ref('todo')
const tasks = ref<TaskItem[]>([])
const templates = ref<TaskTemplate[]>([])
const goals = ref<UserGoal[]>([])
const error = ref('')
const notice = ref('')
const details = ref(false)
const editing = ref<number | null>(null)
const editTitle = ref('')

const title = ref('')
const duration = ref(15)
const scene = ref('HOME')
const category = ref('学习')
const importText = ref('')
const importOpen = ref(false)

const tplName = ref('')
const goalTitle = ref('')
const goalMinutes = ref(120)
const chunk = ref(15)

async function load() {
  error.value = ''
  try {
    ;[tasks.value, templates.value, goals.value] = await Promise.all([listTasks(0), listTemplates(), listGoals()])
  } catch (e) {
    error.value = errMsg(e, '加载失败')
  }
}

onShow(load)

async function add() {
  if (!title.value.trim()) return
  try {
    await createTask({
      title: title.value.trim(),
      durationMinutes: duration.value,
      sceneCode: scene.value,
      category: category.value,
      priority: 1,
    })
    title.value = ''
    notice.value = '已放入'
    await load()
  } catch (e) {
    error.value = errMsg(e, '加不进去')
  }
}

async function doImport() {
  const lines = importText.value
    .split('\n')
    .map((s) => s.trim())
    .filter(Boolean)
  if (!lines.length) return
  try {
    await importTasks(
      lines.map((line) => ({
        title: line,
        durationMinutes: duration.value,
        sceneCode: scene.value,
        category: category.value,
      })),
    )
    importText.value = ''
    importOpen.value = false
    notice.value = `导入 ${lines.length} 条`
    await load()
  } catch (e) {
    error.value = errMsg(e, '导入失败')
  }
}

async function act(kind: 'buffer' | 'done' | 'skip' | 'expire', id: number) {
  try {
    if (kind === 'buffer') await bufferTask(id)
    if (kind === 'done') await completeTask(id, 0)
    if (kind === 'skip') await skipTask(id)
    if (kind === 'expire') await expireTasks([id])
    await load()
  } catch (e) {
    error.value = errMsg(e, '操作失败')
  }
}

async function saveEdit(task: TaskItem) {
  try {
    await updateTask({ ...task, title: editTitle.value.trim() || task.title })
    editing.value = null
    await load()
  } catch (e) {
    error.value = errMsg(e, '改不了')
  }
}

async function asTemplate(task: TaskItem) {
  try {
    await saveTemplate({
      name: task.title,
      content: task.content,
      category: task.category,
      durationMinutes: task.durationMinutes,
      sceneCode: task.sceneCode,
    })
    notice.value = '已存成模板'
    await load()
  } catch (e) {
    error.value = errMsg(e, '存不了模板')
  }
}

async function apply(id: number) {
  try {
    await applyTemplate(id)
    notice.value = '已入池'
    tab.value = 'todo'
    await load()
  } catch (e) {
    error.value = errMsg(e, '套用失败')
  }
}

async function addTpl() {
  if (!tplName.value.trim()) return
  try {
    await saveTemplate({
      name: tplName.value.trim(),
      durationMinutes: duration.value,
      sceneCode: scene.value,
      category: category.value,
    })
    tplName.value = ''
    await load()
  } catch (e) {
    error.value = errMsg(e, '模板没记下')
  }
}

async function addGoal() {
  if (!goalTitle.value.trim()) return
  try {
    await createGoal({ title: goalTitle.value.trim(), targetMinutes: goalMinutes.value, status: 0 })
    goalTitle.value = ''
    await load()
  } catch (e) {
    error.value = errMsg(e, '目标没记下')
  }
}

async function split(id: number) {
  try {
    const pieces = await decomposeGoal(id, chunk.value)
    notice.value = `拆成 ${pieces.length} 条`
    tab.value = 'todo'
    await load()
  } catch (e) {
    error.value = errMsg(e, '拆不开')
  }
}
</script>

<template>
  <PageShell>
    <SegmentBar
      v-model="tab"
      :items="[
        { value: 'todo', label: '待办' },
        { value: 'tpl', label: '模板' },
        { value: 'goal', label: '目标' },
      ]"
    />
    <view v-if="notice" class="banner" style="margin-top: 12px">{{ notice }}</view>
    <view v-if="error" class="toast">{{ error }}</view>

    <view v-if="tab === 'todo'" style="margin-top: 16px">
      <view class="stack">
        <input v-model="title" class="field" placeholder="一条短任务" />
        <view class="linkish" @click="details = !details">{{ details ? '收起时长' : '时长与场景' }}</view>
        <template v-if="details">
          <view class="chips">
            <view
              v-for="n in [5, 10, 15, 20, 30]"
              :key="n"
              class="chip"
              :class="{ 'is-on': duration === n }"
              @click="duration = n"
            >
              {{ n }} 分
            </view>
          </view>
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
          <view class="chips">
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
        </template>
        <view class="btn btn--primary" @click="add">放进池里</view>
      </view>
      <view class="linkish" style="margin: 12px 0" @click="importOpen = !importOpen">从文本导入</view>
      <view v-if="importOpen" class="stack">
        <textarea v-model="importText" class="field field-area" placeholder="一行一件事" />
        <view class="btn" @click="doImport">导入</view>
      </view>
      <view v-if="!tasks.length" class="banner" style="margin-top: 12px">池子是空的。</view>
      <view v-for="t in tasks" :key="t.id" class="panel" style="margin-top: 8px">
        <input v-if="editing === t.id" v-model="editTitle" class="field" @confirm="saveEdit(t)" />
        <view v-else class="task-title">{{ t.title }}</view>
        <view class="muted">{{ t.durationMinutes || '—' }} 分 · {{ TASK_STATUS[t.status ?? 0] }}</view>
        <view class="btn-row" style="margin-top: 8px">
          <view class="btn" @click="act('done', t.id)">完成</view>
          <view class="btn" @click="act('buffer', t.id)">先放一放</view>
        </view>
        <view class="chips" style="margin-top: 8px">
          <text class="linkish" @click=";(editing = t.id), (editTitle = t.title)">改名</text>
          <text class="linkish" @click="asTemplate(t)">存模板</text>
          <text class="linkish" @click="act('skip', t.id)">跳过</text>
          <text class="linkish" @click="act('expire', t.id)">过期</text>
        </view>
        <view v-if="editing === t.id" class="btn" style="margin-top: 8px" @click="saveEdit(t)">保存改名</view>
      </view>
    </view>

    <view v-else-if="tab === 'tpl'" style="margin-top: 16px">
      <view class="stack">
        <input v-model="tplName" class="field" placeholder="模板名" />
        <view class="btn" @click="addTpl">记下模板</view>
      </view>
      <view v-for="tpl in templates" :key="tpl.id" class="row panel" style="margin-top: 8px">
        <view>
          <view class="task-title">{{ tpl.name }}</view>
          <view class="muted">{{ tpl.durationMinutes || 15 }} 分</view>
        </view>
        <view class="btn" @click="apply(tpl.id)">入池</view>
      </view>
    </view>

    <view v-else style="margin-top: 16px">
      <view class="stack">
        <input v-model="goalTitle" class="field" placeholder="大目标" />
        <view class="chips">
          <view
            v-for="n in [60, 120, 300]"
            :key="n"
            class="chip"
            :class="{ 'is-on': goalMinutes === n }"
            @click="goalMinutes = n"
          >
            {{ n }} 分
          </view>
        </view>
        <view class="btn" @click="addGoal">记下</view>
      </view>
      <view class="chips" style="margin: 12px 0">
        <view
          v-for="n in [10, 15, 20]"
          :key="'c' + n"
          class="chip"
          :class="{ 'is-on': chunk === n }"
          @click="chunk = n"
        >
          拆成 {{ n }} 分
        </view>
      </view>
      <view v-for="g in goals" :key="g.id" class="panel" style="margin-top: 8px">
        <view class="task-title">{{ g.title }}</view>
        <view class="muted">{{ g.finishedMinutes || 0 }} / {{ g.targetMinutes || 0 }} 分</view>
        <view class="btn" @click="split(g.id)">拆进待办</view>
      </view>
    </view>
  </PageShell>
</template>
