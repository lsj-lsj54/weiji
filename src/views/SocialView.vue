<script setup lang="ts">
import { onMounted, ref } from 'vue'
import AppShell from '@/components/AppShell.vue'
import { ApiError } from '@/api/http'
import {
  createDesk,
  createTeam,
  favoritePlaza,
  getDesk,
  handleFriendRequest,
  listFriends,
  listPlaza,
  postPlaza,
  reportDesk,
  sendFriendRequest,
  startDesk,
  teamCheckin,
  teamProgress,
  weekRank,
} from '@/api/social'
import type { DeskSession, FriendRow, PlazaPost, RankRow, TeamMember } from '@/api/types'

const TEAM_KEY = 'weiji.teamId'
const DESK_KEY = 'weiji.deskId'

const rows = ref<RankRow[]>([])
const friends = ref<FriendRow[]>([])
const plaza = ref<PlazaPost[]>([])
const progress = ref<TeamMember[]>([])
const desk = ref<DeskSession | null>(null)
const error = ref('')
const notice = ref('')

const friendId = ref('')
const requestId = ref('')
const plazaTitle = ref('')
const plazaBody = ref('')
const teamName = ref('')
const teamGoal = ref('')
const memberId = ref('')
const teamId = ref(Number(localStorage.getItem(TEAM_KEY) || 0) || 0)
const deskPeer = ref('')
const deskId = ref(Number(localStorage.getItem(DESK_KEY) || 0) || 0)
const deskReport = ref('')

function msg(e: unknown, fallback: string) {
  return e instanceof ApiError ? e.message : fallback
}

async function load() {
  error.value = ''
  try {
    ;[rows.value, friends.value, plaza.value] = await Promise.all([weekRank(), listFriends(), listPlaza()])
    if (teamId.value) {
      progress.value = await teamProgress(teamId.value)
    }
    if (deskId.value) {
      desk.value = await getDesk(deskId.value)
    }
  } catch (e) {
    error.value = msg(e, '社交加载失败')
  }
}

onMounted(load)

async function addFriend() {
  const id = Number(friendId.value)
  if (!id) return
  error.value = ''
  try {
    const req = await sendFriendRequest(id)
    notice.value = `申请已发出，编号 ${req.id}。对方用这个编号处理。`
    friendId.value = ''
  } catch (e) {
    error.value = msg(e, '加不了')
  }
}

async function handle(accept: boolean) {
  const id = Number(requestId.value)
  if (!id) return
  error.value = ''
  try {
    await handleFriendRequest(id, accept)
    requestId.value = ''
    notice.value = accept ? '已接受' : '已拒绝'
    await load()
  } catch (e) {
    error.value = msg(e, '处理失败')
  }
}

async function publish() {
  if (!plazaTitle.value.trim()) return
  error.value = ''
  try {
    await postPlaza(plazaTitle.value.trim(), plazaBody.value.trim())
    plazaTitle.value = ''
    plazaBody.value = ''
    notice.value = '已匿名贴出'
    await load()
  } catch (e) {
    error.value = msg(e, '发不出去')
  }
}

async function fav(id: number) {
  error.value = ''
  try {
    await favoritePlaza(id)
    notice.value = '已收藏成模板，去任务池套用'
    await load()
  } catch (e) {
    error.value = msg(e, '收藏失败')
  }
}

async function makeTeam() {
  if (!teamName.value.trim()) return
  error.value = ''
  try {
    const mid = memberId.value ? Number(memberId.value) : undefined
    const team = await createTeam(teamName.value.trim(), teamGoal.value.trim(), mid)
    teamId.value = team.id
    localStorage.setItem(TEAM_KEY, String(team.id))
    notice.value = `队伍 ${team.id} 已建`
    await load()
  } catch (e) {
    error.value = msg(e, '组队失败')
  }
}

async function checkin() {
  if (!teamId.value) return
  error.value = ''
  try {
    await teamCheckin(teamId.value)
    notice.value = '今日已打卡'
    await load()
  } catch (e) {
    error.value = msg(e, '打卡失败')
  }
}

async function makeDesk() {
  const uid = Number(deskPeer.value)
  if (!uid) return
  error.value = ''
  try {
    const session = await createDesk(uid)
    deskId.value = session.id
    localStorage.setItem(DESK_KEY, String(session.id))
    desk.value = session
    notice.value = `同桌 ${session.id}`
  } catch (e) {
    error.value = msg(e, '约不到同桌')
  }
}

async function goDesk() {
  if (!deskId.value) return
  error.value = ''
  try {
    desk.value = await startDesk(deskId.value)
    notice.value = '同桌开始了'
  } catch (e) {
    error.value = msg(e, '开始失败')
  }
}

async function doneDesk() {
  if (!deskId.value) return
  error.value = ''
  try {
    desk.value = await reportDesk(deskId.value, true, deskReport.value)
    notice.value = '已汇报'
  } catch (e) {
    error.value = msg(e, '汇报失败')
  }
}
</script>

<template>
  <AppShell>
    <main class="screen">
      <p class="kicker">一块过</p>
      <h2 class="task-title" style="font-size: 22px">和别人并排攒分钟</h2>

      <p class="kicker" style="margin-top: 18px">本周榜</p>
      <p v-if="!rows.length" class="banner">榜上还没有人。先自己入账几分钟。</p>
      <article v-for="r in rows" :key="r.userId" class="row panel" style="margin-top: 8px">
        <div>
          <div class="muted">{{ r.rank }}</div>
          <div>{{ r.nickname }}</div>
        </div>
        <div class="num">{{ r.minutes }}</div>
      </article>

      <p class="kicker" style="margin-top: 22px">好友</p>
      <p class="hint">用对方用户编号发出申请。收到申请的人填编号处理。你的编号在「我」页。</p>
      <form class="stack" @submit.prevent="addFriend">
        <input v-model="friendId" class="field" inputmode="numeric" placeholder="对方用户编号" />
        <button class="btn" type="submit">发出申请</button>
      </form>
      <form class="stack" style="margin-top: 10px" @submit.prevent="handle(true)">
        <input v-model="requestId" class="field" inputmode="numeric" placeholder="待处理的申请编号" />
        <div class="btn-row">
          <button class="btn btn--primary" type="submit">接受</button>
          <button class="btn" type="button" @click="handle(false)">拒绝</button>
        </div>
      </form>
      <p v-if="friends.length" class="muted" style="margin-top: 8px">
        已互关 {{ friends.length }} 人：{{ friends.map((f) => f.friendId).join('、') }}
      </p>

      <p class="kicker" style="margin-top: 22px">匿名广场</p>
      <form class="stack" @submit.prevent="publish">
        <input v-model="plazaTitle" class="field" placeholder="贴一条可被收藏的碎片任务" />
        <textarea v-model="plazaBody" class="field" placeholder="怎么拆、大概几分钟" />
        <button class="btn" type="submit">匿名贴出</button>
      </form>
      <article v-for="p in plaza" :key="p.id" class="panel" style="margin-top: 8px">
        <h3 class="task-title">{{ p.title }}</h3>
        <p class="muted">{{ p.content }}</p>
        <button class="btn" type="button" @click="fav(p.id)">收藏成模板</button>
      </article>

      <p class="kicker" style="margin-top: 22px">组队</p>
      <form class="stack" @submit.prevent="makeTeam">
        <input v-model="teamName" class="field" placeholder="队伍名" />
        <input v-model="teamGoal" class="field" placeholder="一起做成什么" />
        <input v-model="memberId" class="field" inputmode="numeric" placeholder="队友用户编号（可选）" />
        <button class="btn" type="submit">建队</button>
      </form>
      <p v-if="teamId" class="muted">当前队伍 {{ teamId }}</p>
      <button v-if="teamId" class="btn btn--primary" style="margin-top: 8px" type="button" @click="checkin">今日打卡</button>
      <p v-for="m in progress" :key="m.id" class="muted">成员 {{ m.userId }} · 完成 {{ m.finishedCount || 0 }}</p>

      <p class="kicker" style="margin-top: 22px">同桌</p>
      <form class="stack" @submit.prevent="makeDesk">
        <input v-model="deskPeer" class="field" inputmode="numeric" placeholder="对桌用户编号" />
        <button class="btn" type="submit">约同桌</button>
      </form>
      <p v-if="desk" class="muted">同桌 {{ desk.id }} · {{ desk.status }}</p>
      <div v-if="deskId" class="stack" style="margin-top: 8px">
        <button class="btn" type="button" @click="goDesk">开始</button>
        <input v-model="deskReport" class="field" placeholder="汇报一句" />
        <button class="btn" type="button" @click="doneDesk">写完了</button>
      </div>

      <p v-if="notice" class="banner" style="margin-top: 12px">{{ notice }}</p>
      <p v-if="error" class="toast">{{ error }}</p>
    </main>
  </AppShell>
</template>
