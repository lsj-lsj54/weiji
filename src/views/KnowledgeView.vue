<script setup lang="ts">
import { onMounted, ref } from 'vue'
import AppShell from '@/components/AppShell.vue'
import { ApiError } from '@/api/http'
import {
  createNote,
  deleteNote,
  dueReview,
  exportNotes,
  getNote,
  listNotes,
  markReview,
  mergeNotes,
  updateNote,
  weeklySummary,
} from '@/api/knowledge'
import type { KnowledgeNote, KnowledgeReview } from '@/api/types'

const notes = ref<KnowledgeNote[]>([])
const due = ref<KnowledgeReview | null>(null)
const dueNote = ref<KnowledgeNote | null>(null)
const week = ref<{ count: number; byTag: Record<string, string[]> } | null>(null)
const error = ref('')
const notice = ref('')
const addOpen = ref(false)
const title = ref('')
const content = ref('')
const tags = ref('')
const tagFilter = ref('')
const starredOnly = ref(false)
const mergeFrom = ref<number | null>(null)
const editing = ref<number | null>(null)
const editTitle = ref('')
const editContent = ref('')

function msg(e: unknown, fallback: string) {
  return e instanceof ApiError ? e.message : fallback
}

async function load() {
  error.value = ''
  try {
    notes.value = await listNotes({
      tag: tagFilter.value || undefined,
      starred: starredOnly.value || undefined,
    })
    due.value = await dueReview()
    week.value = await weeklySummary()
    dueNote.value = null
    if (due.value?.noteId) {
      dueNote.value = notes.value.find((n) => n.id === due.value?.noteId) || (await getNote(due.value.noteId))
    }
  } catch (e) {
    error.value = msg(e, '加载失败')
  }
}

onMounted(load)

async function add() {
  if (!title.value.trim()) return
  const tagList = tags.value.split(/[,，\s]+/).map((s) => s.trim()).filter(Boolean)
  try {
    await createNote(title.value.trim(), content.value.trim(), tagList)
    title.value = ''
    content.value = ''
    addOpen.value = false
    notice.value = '已记下，任务池会多一条复习'
    await load()
  } catch (e) {
    error.value = msg(e, '没记下')
  }
}

async function mark(remembered: boolean) {
  if (!due.value) return
  try {
    await markReview(due.value.id, remembered)
    notice.value = remembered ? '间隔拉长' : '会再塞进待办'
    await load()
  } catch (e) {
    error.value = msg(e, '标记失败')
  }
}

async function star(n: KnowledgeNote) {
  try {
    await updateNote({ ...n, starred: n.starred ? 0 : 1 })
    await load()
  } catch (e) {
    error.value = msg(e, '标不了')
  }
}

async function saveEdit(n: KnowledgeNote) {
  try {
    await updateNote({ ...n, title: editTitle.value, content: editContent.value })
    editing.value = null
    await load()
  } catch (e) {
    error.value = msg(e, '改不了')
  }
}

async function merge(toId: number) {
  if (!mergeFrom.value || mergeFrom.value === toId) return
  try {
    await mergeNotes(mergeFrom.value, toId)
    mergeFrom.value = null
    notice.value = '已合并'
    await load()
  } catch (e) {
    error.value = msg(e, '合并不了')
  }
}

async function dump() {
  try {
    await navigator.clipboard.writeText((await exportNotes()) || '')
    notice.value = '已复制'
  } catch (e) {
    error.value = msg(e, '导出失败')
  }
}
</script>

<template>
  <AppShell>
    <main class="screen">
      <p class="kicker">知识点</p>
      <p v-if="week" class="muted">本周 {{ week.count }} 条</p>

      <div v-if="due" class="panel" style="margin: 16px 0">
        <p class="kicker">到期</p>
        <h3 class="task-title">{{ dueNote?.title || '笔记 ' + due.noteId }}</h3>
        <p class="muted">{{ dueNote?.content }}</p>
        <div class="btn-row" style="margin-top: 10px">
          <button class="btn btn--primary" type="button" @click="mark(true)">还记得</button>
          <button class="btn" type="button" @click="mark(false)">忘了</button>
        </div>
      </div>
      <p v-else class="banner" style="margin: 16px 0">没有到期卡片。</p>

      <button class="linkish" type="button" @click="addOpen = !addOpen">{{ addOpen ? '收起' : '记下一条' }}</button>
      <form v-if="addOpen" class="stack" style="margin-top: 8px" @submit.prevent="add">
        <input v-model="title" class="field" placeholder="标题" />
        <textarea v-model="content" class="field" placeholder="内容" />
        <input v-model="tags" class="field" placeholder="标签，逗号分隔" />
        <button class="btn btn--primary" type="submit">记下</button>
      </form>

      <div class="chips" style="margin-top: 16px">
        <button class="chip" :class="{ 'is-on': starredOnly }" type="button" @click="starredOnly = !starredOnly; load()">只要星标</button>
        <button class="linkish" type="button" @click="dump">复制导出</button>
      </div>
      <input v-model="tagFilter" class="field" placeholder="按标签筛，回车" @keyup.enter="load" />

      <p v-if="notice" class="banner" style="margin-top: 12px">{{ notice }}</p>
      <p v-if="error" class="toast">{{ error }}</p>

      <article v-for="n in notes" :key="n.id" class="panel" style="margin-top: 8px">
        <template v-if="editing === n.id">
          <input v-model="editTitle" class="field" />
          <textarea v-model="editContent" class="field" />
          <button class="btn" type="button" @click="saveEdit(n)">保存</button>
        </template>
        <template v-else>
          <h3 class="task-title">{{ n.title }} {{ n.starred ? '★' : '' }}</h3>
          <p class="muted">{{ n.mastery }}</p>
          <p v-if="n.content">{{ n.content }}</p>
        </template>
        <div class="chips" style="margin-top: 8px">
          <button class="linkish" type="button" @click="star(n)">星标</button>
          <button class="linkish" type="button" @click="editing = n.id; editTitle = n.title; editContent = n.content || ''">改</button>
          <button class="linkish" type="button" @click="mergeFrom = mergeFrom === n.id ? null : n.id">
            {{ mergeFrom === n.id ? '取消合并' : '并入其他' }}
          </button>
          <button v-if="mergeFrom && mergeFrom !== n.id" class="linkish" type="button" @click="merge(n.id)">并到这里</button>
          <button class="linkish" type="button" @click="deleteNote(n.id).then(load)">删</button>
        </div>
      </article>
    </main>
  </AppShell>
</template>
