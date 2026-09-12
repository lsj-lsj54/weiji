<script setup lang="ts">
import { onMounted, ref } from 'vue'
import AppShell from '@/components/AppShell.vue'
import { ApiError } from '@/api/http'
import { createNote, deleteNote, dueReview, exportNotes, listNotes, markReview, weeklySummary } from '@/api/knowledge'
import type { KnowledgeNote, KnowledgeReview } from '@/api/types'

const notes = ref<KnowledgeNote[]>([])
const due = ref<KnowledgeReview | null>(null)
const dueNote = ref<KnowledgeNote | null>(null)
const week = ref<{ count: number; byTag: Record<string, string[]> } | null>(null)
const error = ref('')
const notice = ref('')
const title = ref('')
const content = ref('')
const tags = ref('英语')

function msg(e: unknown, fallback: string) {
  return e instanceof ApiError ? e.message : fallback
}

async function load() {
  error.value = ''
  try {
    notes.value = await listNotes()
    due.value = await dueReview()
    week.value = await weeklySummary()
    dueNote.value = due.value ? notes.value.find((n) => n.id === due.value?.noteId) || null : null
  } catch (e) {
    error.value = msg(e, '知识点加载失败')
  }
}

onMounted(load)

async function add() {
  if (!title.value.trim()) return
  error.value = ''
  try {
    const tagList = tags.value
      .split(/[,，\s]+/)
      .map((s) => s.trim())
      .filter(Boolean)
    await createNote(title.value.trim(), content.value.trim(), tagList)
    title.value = ''
    content.value = ''
    notice.value = '已记下，并会生成一条复习任务'
    await load()
  } catch (e) {
    error.value = msg(e, '没记下')
  }
}

async function mark(remembered: boolean) {
  if (!due.value) return
  error.value = ''
  try {
    await markReview(due.value.id, remembered)
    notice.value = remembered ? '记住了，间隔拉长' : '没记住，会再塞进任务池'
    await load()
  } catch (e) {
    error.value = msg(e, '标记失败')
  }
}

async function remove(id: number) {
  error.value = ''
  try {
    await deleteNote(id)
    await load()
  } catch (e) {
    error.value = msg(e, '删不掉')
  }
}

async function dump() {
  error.value = ''
  try {
    const text = await exportNotes()
    await navigator.clipboard.writeText(text || '')
    notice.value = '已复制到剪贴板'
  } catch (e) {
    error.value = msg(e, '导出失败')
  }
}
</script>

<template>
  <AppShell>
    <main class="screen">
      <p class="kicker">知识点</p>
      <h2 class="task-title" style="font-size: 22px">记下来，到期再见面</h2>
      <p class="hint">新建会按艾宾浩斯排复习，并在任务池生成「复习：…」。</p>

      <div v-if="due" class="panel" style="margin: 16px 0">
        <p class="kicker">到期一张</p>
        <h3 class="task-title">{{ dueNote?.title || '笔记 ' + due.noteId }}</h3>
        <p class="muted">{{ dueNote?.content || '打开这条，看看还记不记得。' }}</p>
        <div class="btn-row" style="margin-top: 10px">
          <button class="btn btn--primary" type="button" @click="mark(true)">还记得</button>
          <button class="btn" type="button" @click="mark(false)">忘了</button>
        </div>
      </div>
      <p v-else class="banner" style="margin: 16px 0">这会儿没有到期卡片。</p>

      <form class="stack" @submit.prevent="add">
        <input v-model="title" class="field" placeholder="标题，比如不规则动词" />
        <textarea v-model="content" class="field" placeholder="内容，给以后的自己看" />
        <input v-model="tags" class="field" placeholder="标签，逗号分隔" />
        <button class="btn btn--primary" type="submit">记下</button>
      </form>

      <p v-if="week" class="muted" style="margin-top: 16px">本周 {{ week.count }} 条</p>
      <button class="btn btn--ghost" style="margin-top: 8px" type="button" @click="dump">复制导出文本</button>

      <p v-if="notice" class="banner" style="margin-top: 12px">{{ notice }}</p>
      <p v-if="error" class="toast">{{ error }}</p>

      <div class="stack" style="margin-top: 16px">
        <article v-for="n in notes" :key="n.id" class="panel">
          <h3 class="task-title">{{ n.title }}</h3>
          <p class="muted">{{ n.mastery || '未标掌握度' }}</p>
          <p v-if="n.content">{{ n.content }}</p>
          <button class="btn btn--ghost" type="button" @click="remove(n.id)">删掉</button>
        </article>
      </div>
      <p v-if="!notes.length" class="hint">还没有笔记。</p>
    </main>
  </AppShell>
</template>
