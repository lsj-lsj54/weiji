<script setup lang="ts">
import { onMounted, ref } from 'vue'
import AppShell from '@/components/AppShell.vue'
import { ApiError } from '@/api/http'
import { weekRank } from '@/api/social'
import type { RankRow } from '@/api/types'

const rows = ref<RankRow[]>([])
const error = ref('')

onMounted(async () => {
  try {
    rows.value = await weekRank()
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : '周榜加载失败'
  }
})
</script>

<template>
  <AppShell>
    <main class="screen">
      <p class="kicker">本周</p>
      <h2 class="task-title" style="font-size: 22px">和好友比的是专注分钟</h2>
      <p class="hint">组队、广场、同桌下一版再补。这里只看周榜。</p>
      <p v-if="error" class="toast">{{ error }}</p>
      <p v-if="!rows.length" class="banner" style="margin-top: 16px">榜上还没有人。先自己入账几分钟。</p>
      <article v-for="r in rows" :key="r.userId" class="row panel" style="margin-top: 8px">
        <div>
          <div class="muted">{{ r.rank }}</div>
          <div>{{ r.nickname }}</div>
        </div>
        <div class="num">{{ r.minutes }}</div>
      </article>
    </main>
  </AppShell>
</template>
