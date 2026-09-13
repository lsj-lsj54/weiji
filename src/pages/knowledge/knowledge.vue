<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import PageShell from '@/components/PageShell.vue'
import { errMsg } from '@/api/http'
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
import { useAuthGuard } from '@/composables/useAuthGuard'

useAuthGuard()

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
    error.value = errMsg(e, '加载失败')
  }
}

onShow(load)

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
    error.value = errMsg(e, '没记下')
  }
}

async function mark(remembered: boolean) {
  if (!due.value) return
  try {
    await markReview(due.value.id, remembered)
    notice.value = remembered ? '间隔拉长' : '会再塞进待办'
    await load()
  } catch (e) {
    error.value = errMsg(e, '标记失败')
  }
}

async function star(n: KnowledgeNote) {
  try {
    await updateNote({ ...n, starred: n.starred ? 0 : 1 })
    await load()
  } catch (e) {
    error.value = errMsg(e, '标不了')
  }
}

async function saveEdit(n: KnowledgeNote) {
  try {
    await updateNote({ ...n, title: editTitle.value, content: editContent.value })
    editing.value = null
    await load()
  } catch (e) {
    error.value = errMsg(e, '改不了')
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
    error.value = errMsg(e, '合并不了')
  }
}

async function dump() {
  try {
    const text = (await exportNotes()) || ''
    uni.setClipboardData({ data: text })
    notice.value = '已复制'
  } catch (e) {
    error.value = errMsg(e, '导出失败')
  }
}

async function remove(id: number) {
  try {
    await deleteNote(id)
    await load()
  } catch (e) {
    error.value = errMsg(e, '删不了')
  }
}
</script>

<template>
  <PageShell>
    <view class="kicker">知识点</view>
    <view v-if="week" class="muted">本周 {{ week.count }} 条</view>

    <view v-if="due" class="panel" style="margin: 16px 0">
      <view class="kicker">到期</view>
      <view class="task-title">{{ dueNote?.title || '笔记 ' + due.noteId }}</view>
      <view class="muted">{{ dueNote?.content }}</view>
      <view class="btn-row" style="margin-top: 10px">
        <view class="btn btn--primary" @click="mark(true)">还记得</view>
        <view class="btn" @click="mark(false)">忘了</view>
      </view>
    </view>
    <view v-else class="banner" style="margin: 16px 0">没有到期卡片。</view>

    <view class="linkish" @click="addOpen = !addOpen">{{ addOpen ? '收起' : '记下一条' }}</view>
    <view v-if="addOpen" class="stack" style="margin-top: 8px">
      <input v-model="title" class="field" placeholder="标题" />
      <textarea v-model="content" class="field field-area" placeholder="内容" />
      <input v-model="tags" class="field" placeholder="标签，逗号分隔" />
      <view class="btn btn--primary" @click="add">记下</view>
    </view>

    <view class="chips" style="margin-top: 16px">
      <view class="chip" :class="{ 'is-on': starredOnly }" @click=";(starredOnly = !starredOnly), load()">只要星标</view>
      <text class="linkish" @click="dump">复制导出</text>
    </view>
    <input v-model="tagFilter" class="field" placeholder="按标签筛，回车" @confirm="load" />

    <view v-if="notice" class="banner" style="margin-top: 12px">{{ notice }}</view>
    <view v-if="error" class="toast">{{ error }}</view>

    <view v-for="n in notes" :key="n.id" class="panel" style="margin-top: 8px">
      <template v-if="editing === n.id">
        <input v-model="editTitle" class="field" />
        <textarea v-model="editContent" class="field field-area" />
        <view class="btn" @click="saveEdit(n)">保存</view>
      </template>
      <template v-else>
        <view class="task-title">{{ n.title }} {{ n.starred ? '★' : '' }}</view>
        <view class="muted">{{ n.mastery }}</view>
        <view v-if="n.content">{{ n.content }}</view>
      </template>
      <view class="chips" style="margin-top: 8px">
        <text class="linkish" @click="star(n)">星标</text>
        <text
          class="linkish"
          @click=";(editing = n.id), (editTitle = n.title), (editContent = n.content || '')"
        >
          改
        </text>
        <text class="linkish" @click="mergeFrom = mergeFrom === n.id ? null : n.id">
          {{ mergeFrom === n.id ? '取消合并' : '并入其他' }}
        </text>
        <text v-if="mergeFrom && mergeFrom !== n.id" class="linkish" @click="merge(n.id)">并到这里</text>
        <text class="linkish" @click="remove(n.id)">删</text>
      </view>
    </view>
  </PageShell>
</template>
