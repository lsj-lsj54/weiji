<script setup lang="ts">
import { onMounted, ref } from 'vue'
import AppShell from '@/components/AppShell.vue'
import { ApiError } from '@/api/http'
import { dueReview, listNotes } from '@/api/knowledge'
import type { KnowledgeNote, KnowledgeReview } from '@/api/types'

const notes = ref<KnowledgeNote[]>([])
const due = ref<KnowledgeReview | null>(null)
const error = ref('')

onMounted(async () => {
  try {
    notes.value = await listNotes()
    due.value = await dueReview()
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : '知识点加载失败'
  }
})
</script>

<template>
  <AppShell>
    <main class="screen">
      <p class="kicker">知识点</p>
      <h2 class="task-title" style="font-size: 22px">到期复习先放在这里</h2>
      <p class="hint">完整卡片编辑下一版再补。现在能看见到期项和最近笔记。</p>
      <p v-if="due" class="banner" style="margin-top: 16px">有一张到期复习（笔记 {{ due.noteId }}）。</p>
      <p v-else class="banner" style="margin-top: 16px">这会儿没有到期卡片。</p>
      <p v-if="error" class="toast">{{ error }}</p>
      <div class="stack" style="margin-top: 16px">
        <article v-for="n in notes" :key="n.id" class="panel">
          <h3 class="task-title">{{ n.title }}</h3>
          <p class="muted">{{ n.mastery || '未标掌握度' }}</p>
        </article>
      </div>
      <p v-if="!notes.length" class="hint">还没有笔记。</p>
    </main>
  </AppShell>
</template>
